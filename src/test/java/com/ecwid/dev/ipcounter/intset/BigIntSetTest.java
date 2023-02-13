package com.ecwid.dev.ipcounter.intset;

import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigIntSetTest {
    private static final int BIG_SIZE = Integer.MAX_VALUE >> 1;

    @Test
    void test() {
        IntSet intSet = new BigIntSet();
        int elements = BIG_SIZE;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < elements; i++) {
                intSet.add(i);
            }
        }
        assertEquals(intSet.size(), elements);
    }

    @Test
    void testConcurrentUsage() {
        IntSet intSet = new BigIntSet();
        IntStream.range(0, BIG_SIZE)
                .parallel()
                .forEach(intSet::add);
        assertEquals(BIG_SIZE, intSet.size());
    }
}
