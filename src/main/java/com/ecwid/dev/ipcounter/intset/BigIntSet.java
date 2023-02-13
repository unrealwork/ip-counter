package uniqueip;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.IntStream;

class BigIntSet implements IntSet {
    static final long BIG_SET_EFFICIENT_CAPACITY = 8_000_000;
    private static final int FIVE = 5;
    private static final int INT_BIT_SIZE = 1 << FIVE;
    private static final int BUCKETS_SIZE = 1 << (INT_BIT_SIZE - FIVE);
    private static final int BUCKET_MASK = (1 << (FIVE + 1)) - 1;
    private final AtomicIntegerArray storage = new AtomicIntegerArray(BUCKETS_SIZE);

    BigIntSet() {
    }

    @Override
    public void add(int e) {
        int bucketIndex = e >>> FIVE;
        int bitIndex = e & BUCKET_MASK;
        int bitToSet = 1 << bitIndex;
        storage.updateAndGet(bucketIndex, val -> val | bitToSet);
    }

    @Override
    public synchronized long size() {
        return IntStream.range(0, storage.length())
                .parallel()
                .map(i -> Integer.bitCount(storage.get(i)))
                .sum();
    }
}
