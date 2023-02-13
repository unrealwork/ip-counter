package com.ecwid.dev.ipcounter;

import com.ecwid.dev.ipcounter.intset.IntSet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

final class UniqueIpCounter implements Counter {
    private final Path p;

    UniqueIpCounter(Path path) {
        this.p = path;
    }

    @Override
    public long count() {
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(
                p, StandardOpenOption.READ)) {
            IntSet ipSet = IntSet.withCapacity(fileChannel.size() / ChunkIterator.IP_RECORD_BYTES + 1);
            IteratorPublisher<ByteBuffer> submitablePublisher = new IteratorPublisher<>(() -> new ChunkIterator(fileChannel));
            submitablePublisher.subscribe(new IpScanByteBufferSubscriber(ipSet::add));
            submitablePublisher.block();
            return ipSet.size();
        } catch (IOException | ExecutionException e) {
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException();
        }
    }

    public static void main(String[] args) {
        UniqueIpCounter ipCounter = new UniqueIpCounter(Path.of(args[0]));
        System.out.println(ipCounter.count());
    }
}
