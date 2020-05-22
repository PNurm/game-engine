package com.gengine.core.model.terrain;

import com.gengine.util.DatabaseObject;
import com.gengine.core.world.node.TerrainNode;

import java.nio.ByteBuffer;

import static com.gengine.Constants.MULTITEXTURE_COUNT;

public class TerrainTile implements DatabaseObject {

    public TerrainNode terrainNode;
    public float[] materialWeights = new float[MULTITEXTURE_COUNT];
    private float height;

    public TerrainTile(TerrainNode terrainNode) {
        this.terrainNode = terrainNode;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public int length() {
        return 4 + 2;
    }

    @Override
    public void pack(ByteBuffer dest) {
        dest.putFloat(height);
       // dest.putShort((short) texture);
    }

    @Override
    public void unpack(ByteBuffer src) {
        height = src.getFloat();
        //texture = src.getShort();
    }

    public float[] getTextureWeights() {
        return materialWeights;
    }
}
