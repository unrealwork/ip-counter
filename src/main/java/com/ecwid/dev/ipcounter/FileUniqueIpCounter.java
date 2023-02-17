package com.ecwid.dev.ipcounter;

import com.ecwid.dev.ipcounter.intset.IntSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;

final class FileUniqueIpCounter implements Counter {
    private final Path p;

    FileUniqueIpCounter(Path path) {
        this.p = path;
    }

    @Override
    public long count() {
        try (FileChannel fc = (FileChannel) Files.newByteChannel(
                p, StandardOpenOption.READ)) {
            IntSet ipSet = IntSet.withCapacity(fc.size() / ChunkIterator.IP_RECORD_BYTES + 1);
            CountDownLatch latch = new CountDownLatch(1);
            IteratorPublisher<ByteBuffer> chunkPublisher = new IteratorPublisher<>(() -> new ChunkIterator(fc));
            chunkPublisher.doFinally(latch::countDown);
            chunkPublisher.subscribe(new IpScanByteBufferSubscriber(ipSet::add));
            latch.await();
            return ipSet.size();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
    }
}
