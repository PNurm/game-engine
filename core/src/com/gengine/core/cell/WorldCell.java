package com.gengine.core.cell;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.core.cell.node.*;
import com.gengine.core.cell.node.TerrainNode;

public class WorldCell extends CellNode {

    public static final int SIZE = 64;
    public static final int TILE_SIZE = 1;
    public static final int WORLD_SIZE = SIZE * TILE_SIZE;

    private final int cellX;
    private final int cellY;

    public WorldCell(int cellX, int cellY) {
        super(getFilename(cellX, cellY));
        this.cellX = cellX;
        this.cellY = cellY;

        TerrainNode terrainNode = new TerrainNode(this);
        this.addChild(terrainNode);

        position.set(worldX(), 0, worldY());
    }

    public WorldCell(String filename) {
        super(filename);
        int xp = filename.indexOf('x');
        int yp = filename.indexOf('y');
        if (xp == -1 || yp == -1) {
            throw new IllegalArgumentException("Invalid world cell filename");
        }
        String xv = filename.substring(xp + 1, yp);
        String yv = filename.substring(yp + 1);

        this.cellX = Short.parseShort(xv);
        this.cellY = Short.parseShort(yv);

        TerrainNode terrainNode = new TerrainNode(this);
        this.addChild(terrainNode);

        position.set(worldX(), 0, worldY());
    }


    public static String getFilename(int cellX, int cellY) {
        return String.format("Cell[X%d+Y%d]", cellX, cellY);
    }

    public TerrainNode getTerrain() {
        for (CellNode c : children()) {
            if (c instanceof TerrainNode) {
                return (TerrainNode) c;
            }
        }
        return null;
    }

    public int getX() {
        return cellX;
    }

    public int getY() {
        return cellY;
    }

    public void dispose() {
        for (CellNode c : children()) {
            c.dispose();
        }
    }

    public int startX() {
        return cellX * SIZE;
    }

    public int startY() {
        return cellY * SIZE;
    }

    public int worldX() {

        return startX() * TILE_SIZE;
    }

    public int worldY() {
        return startY() * TILE_SIZE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldCell worldCell = (WorldCell) o;
        return cellX == worldCell.cellX &&
                cellY == worldCell.cellY;
    }

    @Override
    public int hashCode() {
        return cellX * 31 + cellY;
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

}
