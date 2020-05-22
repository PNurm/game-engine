package com.gengine.util;

public class IntHashTable<T> {
    private IntHashTableEntry[] table;

    private int size;
    private final boolean allowNegative;
    private final IntHashTableEntry head = new IntHashTableEntry();

    private IntHashTableEntry tail = head;
    public IntHashTable(int initialCapacity, boolean allowNegative) {
        if (initialCapacity <= 0)
            throw new IllegalArgumentException("intial capacity must be more than zero");
        this.table = new IntHashTableEntry[nextPowerOf2(initialCapacity)];
        this.size = 0;
        this.allowNegative = allowNegative;
    }
    public static int nextPowerOf2(int lpow2) {
        int n = lpow2;
        n--;
        n |= n >> 1;
        n |= n >> 2;
        n |= n >> 4;
        n |= n >> 8;
        n |= n >> 16;
        n++;
        return n;
    }

    @SuppressWarnings("unchecked")
    public T get(int key) {
        final int i = index(key);
        IntHashTableEntry e = table[i];
        return e != null && e.key == key ? (T) e.o : null;
    }

    private int index(int j) {
        if (allowNegative)
            // Center on zero;
            j = (((j >> 31) ^ j << 1) | (j >>> 31));
        return j & (table.length - 1);
    }

    public void put(int key, T v) {
        int i = -1;
        do {
            if (i != -1)
                rehash();
            i = index(key);
        } while (table[i] != null && table[i].key != key);
        if (table[i] != null) {
            table[i].o = v;
        } else {
            IntHashTableEntry e = new IntHashTableEntry();
            e.o = v;
            e.key = key;
            e.prev = tail;

            tail.next = e;
            tail = e;
            table[i] = e;
            size++;
        }
    }

    private void rehash() {
        table = new IntHashTableEntry[table.length << 1];
        for (IntHashTableEntry kk = head.next; kk != null; kk = kk.next)
            table[index(kk.key)] = kk;
    }

    public T remove(int key) {
        int i = index(key);
        if (table[i] != null && table[i].key == key) {
            if (table[i] == tail)
                tail = table[i].prev;
            table[i].removeMe();
        }
        return null;
    }

    public int size() {
        return size;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{");
        for (IntHashTableEntry kk = head.next; kk != null; kk = kk.next) {
            s.append(kk.key + "=" + kk.o);
            if (kk.next != null)
                s.append(", ");
        }
        return s.append('}').toString();
    }

    private static class IntHashTableEntry {
        int key;
        Object o;
        IntHashTableEntry prev, next;

        private void removeMe() {
            prev.next = next;
            if (next != null)
                next.prev = prev;
        }
    }
}
