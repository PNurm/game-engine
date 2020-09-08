package com.gengine.core.world;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.core.world.node.*;
import com.gengine.render.world.TerrainNodeRenderContext;
import com.gengine.util.IOUtils;

import java.awt.geom.Point2D;
import java.nio.ByteBuffer;
import java.util.Objects;

public class WorldCell {

    public static final int SIZE = 64;
    public static final int TILE_SIZE = 1;
    public static final int WORLD_SIZE =    SIZE * TILE_SIZE;

    private int cellX, cellY;

    public RenderableNode root;

    private String id;

    public WorldCell(int cellX, int cellY) {
        this.id = String.format("x%+04dy%+04d", cellX, cellY);
        this.cellX = cellX;
        this.cellY = cellY;
        this.root = new RootCellNode(this);
    }

    public TerrainNode getTerrain() {
        for (CellNode c : root.children()) {
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

    public void pack(ByteBuffer data) {
        IOUtils.pack(id, data);
        root.pack(data);
    }

    public void unpack(ByteBuffer data) {
        id = IOUtils.unpackString(data);
        root.unpack(data);
    }

    public void dispose() {
        for (CellNode c : root.children()) {
            c.dispose();
        }
    }

    public static final class RootCellNode extends RenderableNode {

        public final WorldCell cell;

        private RootCellNode(WorldCell cell) {
            super( "" + cell.toString());
            this.cell = cell;
            position.set(cell.worldX(), 0, cell.worldY());
        }

        @Override
        public String toString() {
            return "" + cell.toString();
        }

        @Override
        public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
            return null;
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
        return "WorldCell{id='" + id + '\'' + '}';
    }

}
