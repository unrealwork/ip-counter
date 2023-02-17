package com.ecwid.dev.ipcounter;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

final class IpScanByteBufferSubscriber implements Flow.Subscriber<ByteBuffer> {
    private static final char DOT = '.';
    private static final long BUFFER_SIZE = Runtime.getRuntime().availableProcessors();
    public static final int SEGMENT_BIT_SIZE = 8;
    private final AtomicLong buffered = new AtomicLong();
    public static final int LAST_SEGMENT = 3;

    private final IntConsumer ipConsumer;
    private Flow.Subscription subsctiption;

    public IpScanByteBufferSubscriber(IntConsumer ipConsumer) {
        this.ipConsumer = ipConsumer;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subsctiption = subscription;
        buffered.set(BUFFER_SIZE);
        subscription.request(BUFFER_SIZE);
    }

    @Override
    public void onNext(ByteBuffer item) {
        scanChunk(item);
        subsctiption.request(1);
    }


    @Override
    public void onError(Throwable throwable) {
        throw new IllegalStateException(throwable);
    }

    @Override
    public void onComplete() {
        // NOOP
    }

    private void scanChunk(ByteBuffer b) {
        int curIp = 0;
        int curSegment = 0;
        int segmentNumber = 0;
        while (b.hasRemaining()) {
            char c = (char) b.get();
            if (isDigit(c)) {
                int d = toDigit(c);
                curSegment = curSegment * 10 + d;
            } else {
                if (c == DOT) {
                    curIp |= (curSegment << (SEGMENT_BIT_SIZE * segmentNumber));
                    curSegment = 0;
                    segmentNumber++;
                } else {
                    if (segmentNumber == LAST_SEGMENT) {
                        curIp |= (curSegment << (SEGMENT_BIT_SIZE * segmentNumber));
                        ipConsumer.accept(curIp);
                        curSegment = 0;
                        segmentNumber = 0;
                        curIp = 0;
                    }
                }
            }
        }
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static int toDigit(char c) {
        return c - '0';
    }
}
