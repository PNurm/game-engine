package com.gengine.core.world.node;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.core.model.Material;
import com.gengine.core.model.terrain.TerrainBlendMap;
import com.gengine.core.model.terrain.TerrainTile;
import com.gengine.core.world.WorldCell;
import com.gengine.core.world.CellLocation;
import com.gengine.render.world.RenderContextProvider;

import java.nio.ByteBuffer;

import static com.gengine.Constants.MULTITEXTURE_COUNT;

public class TerrainNode extends RenderableNode {

    private final CellLocation location;
    private TerrainTile[][] tiles = new TerrainTile[WorldCell.SIZE][WorldCell.SIZE];
    private Material[] materials = new Material[MULTITEXTURE_COUNT];
    private TerrainBlendMap terrainBlendMap;

    public TerrainNode(CellLocation location) {
        super("Terrain");
        this.location = location;
        this.terrainBlendMap = new TerrainBlendMap(TerrainBlendMap.WIDTH, TerrainBlendMap.HEIGHT);

        for (int i = 0; i < WorldCell.SIZE; i++)
            for (int j = 0; j < WorldCell.SIZE; j++)
                tiles[i][j] = new TerrainTile(this);

        for(int i = 0; i < materials.length;i++) {
            materials[i] = Material.NULL;
        }
    }

    @Override
    public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
        return null;
    }

    @Override
    public void setRenderContext(RenderContextProvider provider) {
        super.setRenderContext(provider);
    }

    public CellLocation getLocation() {
        return location;
    }

    public TerrainTile getTile(int x, int z) {
        if (x >= tiles.length || z >= tiles[0].length || x < 0 || z < 0)
            return null;

        return tiles[x][z];
    }

    public TerrainTile tile(float lx, float ly) {
        int ax = (int) Math.floor(lx / WorldCell.TILE_SIZE);
        int ay = (int) Math.floor(ly / WorldCell.TILE_SIZE);
        return tiles[ax][ay];
    }

    public void setMaterial(int channelID, Material asset) {
        materials[channelID] = asset;
    }

    public Material getMaterial(int textureID) {
        return materials[textureID];
    }

    public TerrainBlendMap getTerrainBlendMap() {
        return terrainBlendMap;
    }

    public void setTerrainBlendMap(TerrainBlendMap terrainBlendMap) {
        this.terrainBlendMap = terrainBlendMap;
    }



    @Override
    public void pack(ByteBuffer data) {
        super.pack(data);

        data.putShort((short) terrainBlendMap.getWidth());
        data.putShort((short) terrainBlendMap.getHeight());

        for (int i = 0; i < terrainBlendMap.getWidth(); i++)
            for (int j = 0; j < terrainBlendMap.getHeight(); j++) {
                int pixel = terrainBlendMap.getMap().getPixel(i, j);
                data.putInt(pixel);
            }
        data.put((byte) WorldCell.SIZE);
        for (int i = 0; i < WorldCell.SIZE; i++)
            for (int j = 0; j < WorldCell.SIZE; j++)
                tiles[i][j].pack(data);
    }

    @Override
    public void unpack(ByteBuffer src) {
        super.unpack(src);

        int blendWidth = src.getShort();
        int blendHeight = src.getShort();
        for(int i = 0; i < blendWidth; i++) {
            for(int j = 0; j < blendHeight;j++) {
                int pixel = src.getInt();
                terrainBlendMap.getMap().drawPixel(i,j, pixel);
            }
        }
        int tileCount = src.getShort();
        if(tileCount != WorldCell.SIZE * 2) {
            System.out.println("Tile count mismatch");
        }
        for(int i = 0; i <  WorldCell.SIZE; i++) {
            for (int j = 0; j < WorldCell.SIZE; j++) {
                tiles[i][j].unpack(src);
            }
        }
    }
}
