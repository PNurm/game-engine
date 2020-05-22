package com.gengine.core.world;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.core.world.node.*;
import com.gengine.util.IOUtils;

import java.nio.ByteBuffer;

public class WorldCell {

    public static final int SIZE = 64;
    public static final int TILE_SIZE = 1;
    public static final int WORLD_SIZE = SIZE * TILE_SIZE;

    private CellLocation location;
    public RenderableNode root;

    private String id;

    public WorldCell(CellLocation location) {
        this.location = location;
        this.root = new RootCellNode(this);
    }

    public TerrainNode getTerrain() {
        for (CellNode c : root.children()) {
            if (c instanceof TerrainNode) {
                return (TerrainNode) c;
            }
        }
        if (location != null) {
            TerrainNode terrainNode = new TerrainNode(location);
            root.addChild(terrainNode);
            System.out.println("Created terrain node for " + location);
            //hn.move(root);
            return terrainNode;
        }
        return null;
    }

    public CellLocation getLocation() {
        return location;
    }

    public void pack(ByteBuffer data) {
        IOUtils.pack(id, data);
        root.pack(data);
    }

    public void unpack(ByteBuffer data) {
        id = IOUtils.unpackString(data);
        root.unpack(data);
    }

    public static final class RootCellNode extends RenderableNode {

        public final WorldCell cell;

        private RootCellNode(WorldCell cell) {
            super( "" + cell.getLocation());
            this.cell = cell;
            position.set(cell.location.worldX(), 0, cell.location.worldY());
        }

        @Override
        public String toString() {
            return "" + cell.getLocation();
        }

        @Override
        public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
            return null;
        }
    }
}
