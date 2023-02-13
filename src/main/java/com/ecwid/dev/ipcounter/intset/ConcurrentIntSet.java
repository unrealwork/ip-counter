package com.ecwid.dev.ipcounter.intset;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentIntSet implements IntSet {
    private final Set<Integer> delegate;
    private static final int MAXIMUM_CAPACITY = 1 << 30;


    public ConcurrentIntSet(final long capacity) {
        if (capacity > MAXIMUM_CAPACITY) {
            throw new IllegalArgumentException("Support only " + MAXIMUM_CAPACITY + " capacity");
        }
        delegate = ConcurrentHashMap.newKeySet((int) capacity);
    }

    @Override
    public void add(int e) {
        delegate.add(e);
    }

    @Override
    public long size() {
        return delegate.size();
    }
}
