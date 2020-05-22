package com.gengine.core.model.terrain;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class TerrainBlendMap {

    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

    private Texture texture;
    private int width;
    private int height;
    private Pixmap map;

    public TerrainBlendMap(int width, int height) {
        this.map = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        this.map.setBlending(Pixmap.Blending.None);
        this.texture = new Texture(map);
        this.width = width;
        this.height = height;
    }

    public Pixmap getMap() {
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture asTexture() {
        return texture;
    }

    public void updateTexture() {
        texture.draw(map, 0, 0);
    }
}
