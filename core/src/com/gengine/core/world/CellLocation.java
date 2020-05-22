package com.gengine.core.world;

import com.gengine.util.HashTable2D;


public class CellLocation {

    private static final HashTable2D<CellLocation> cache = new HashTable2D<>(128, true);

    private final int x;
    private final int y;

    private CellLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static CellLocation create(int x, int y) {
        CellLocation l = cache.get((short) x, (short) y);
        if (l == null)
            cache.put((short) x, (short) y, l = new CellLocation((short) x, (short) y));
        return l;
    }

    public static CellLocation fromWorld(float wX, float wY) {
        return create((int) Math.floor(wX / WorldCell.SIZE), (int) Math.floor(wY / WorldCell.SIZE));
    }

    @Override
    public final boolean equals(final Object o) {
        if (o instanceof CellLocation) {
            CellLocation cellLocation = (CellLocation) o;
            return cellLocation.x == this.x
                    && cellLocation.y == this.y;
        }
        return false;
    }

    public String getFileName() {
        return String.format("x%+04dy%+04d", x, y);
    }

    public final int sectorX() {
        return x;
    }

    public final int sectorY() {
        return y;
    }

    @Override
    public final String toString() {
        return "CellLoc [x=" + x + ", y=" + y + "]";
    }

    public int startX() {
        return x * WorldCell.SIZE;
    }

    public int startY() {
        return y * WorldCell.SIZE;
    }

    public int worldX() {
        return startX() * WorldCell.TILE_SIZE;
    }

    public int worldY() {
        return startY() * WorldCell.TILE_SIZE;
    }

    public boolean inNegativeSpace() {
        return x < 0 || y < 0;
    }
}
