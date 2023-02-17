package com.ecwid.dev.ipcounter;

import java.nio.ByteBuffer;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;

final class IpScanByteBufferSubscriber implements Flow.Subscriber<ByteBuffer> {
    private static final char DOT = '.';
    private static final long BUFFER_SIZE = Runtime.getRuntime().availableProcessors();
    public static final int SEGMENT_BIT_SIZE = 8;
    public static final char ZERO = '0';
    public static final char NINE = '9';
    private final AtomicLong buffered = new AtomicLong();
    public static final int LAST_SEGMENT = 3;

    private final IntConsumer ipConsumer;
    private Flow.Subscription subsctiption;

    private static final int[][] MULT_CACHE = preCalcMultiplication();

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
                curSegment = MULT_CACHE[curSegment][c];
            } else {
                if (c == DOT) {
                    curIp <<= SEGMENT_BIT_SIZE;
                    curIp |= curSegment;
                    curSegment = 0;
                    segmentNumber++;
                } else {
                    if (segmentNumber == LAST_SEGMENT) {
                        curIp <<= SEGMENT_BIT_SIZE;
                        curIp |= curSegment;
                        ipConsumer.accept(curIp);
                        curSegment = 0;
                        segmentNumber = 0;
                        curIp = 0;
                    }
                }
            }
        }
    }

    private static int[][] preCalcMultiplication() {
        int[][] multCache = new int[1 << 8][1 << 8];
        for (int i = 0; i < multCache.length; i++) {
            for (int d = ZERO; d <= NINE; d++) {
                multCache[i][d] = i * 10 + (d - ZERO);
            }
        }
        return multCache;
    }

    private static boolean isDigit(char c) {
        return ZERO <= c && c <= NINE;
    }
}
