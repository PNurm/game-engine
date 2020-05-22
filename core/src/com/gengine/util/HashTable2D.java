package com.gengine.util;

public class HashTable2D<T> {
    private final IntHashTable<T> table;
    private final boolean negative;

    public HashTable2D(int capacity, boolean negative) {
        table = new IntHashTable<>(capacity, false);
        this.negative = negative;
    }

    public T get(short x, short y) {
        return table.get(hash(x, y));
    }

    private int hash(short xb, short yb) {
        int x = xb;
        int y = yb;
        if (negative) {
            x = (((xb >> 31) ^ xb << 1) | (xb >>> 31));
            y = (((yb >> 31) ^ yb << 1) | (yb >>> 31));
        }
        // mix bits
        int bits = 0;
        bits |= (x & 0xF) << 0;
        bits |= (y & 0xF) << 4;
        bits |= (x & 0xF0) << 4;
        bits |= (y & 0xF0) << 8;
        bits |= (x & 0xF00) << 8;
        bits |= (y & 0xF00) << 12;
        bits |= (x & 0xF000) << 12;
        bits |= (y & 0xF000) << 16;
        return bits;
    }

    public void put(short x, short y, T v) {
        table.put(hash(x, y), v);
    }

    public T remove(short x, short y) {
        return table.remove(hash(x, y));
    }
}

