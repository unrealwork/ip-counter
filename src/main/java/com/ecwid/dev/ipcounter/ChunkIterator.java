package uniqueip;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

final class ChunkIterator implements Iterator<ByteBuffer> {
    static final int IP_RECORD_BYTES = 16;
    private static final int MIN_CHUNK_SIZE = 1 << 10;
    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors();
    private final long size;
    private final FileChannel fileChannel;
    private final ByteBuffer bb;
    private long lastEnd;
    private final int chunkSize;

    ChunkIterator(FileChannel fileChannel) {
        try {
            this.size = fileChannel.size();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        this.fileChannel = fileChannel;
        this.lastEnd = 0;
        this.chunkSize = calcChunkSize();
        this.bb = ByteBuffer.allocate(IP_RECORD_BYTES);
    }

    private int calcChunkSize() {
        if (MIN_CHUNK_SIZE >= size) {
            return (int) size;
        } else {
            return (int) Math.min(size / PARALLELISM, Integer.MAX_VALUE >> 4);
        }
    }

    @Override
    public boolean hasNext() {
        return lastEnd < size;
    }

    @Override
    public ByteBuffer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        long nextEnd = lastEnd + chunkSize;
        if (nextEnd >= size) {
            nextEnd = size;
        } else {
            nextEnd = findLastIpEnd(nextEnd);
        }
        try {
            long readSize = nextEnd - lastEnd;
            final ByteBuffer byteBuffer = fileChannel.map(READ_ONLY, lastEnd, readSize);
            lastEnd = nextEnd;
            return byteBuffer;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private long findLastIpEnd(long nextEnd) {
        long pos = nextEnd - IP_RECORD_BYTES;
        try {
            bb.clear();
            fileChannel.read(bb, pos);
            for (int i = 0; i < bb.capacity(); i++) {
                char c = (char) bb.get(i);
                if (c == '\n') {
                    return pos + i;
                }
            }
            throw new IllegalStateException("Invalid IP format");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
