package com.ecwid.dev.ipcounter.intset;

public interface IntSet {
    void add(int e);

    long size();
    
    static IntSet withCapacity(long capacity) {
        if (capacity < BigIntSet.BIG_SET_EFFICIENT_CAPACITY) {
            return new ConcurrentIntSet(capacity);
        }
        return new BigIntSet();
    } 
}
