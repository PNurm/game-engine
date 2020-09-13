package com.gengine.editor.util;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class Backgrounds {


    public static Drawable create(Color color, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Drawable create(Color color, Color edge, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        labelColor.setColor(edge);
        labelColor.drawRectangle(0,0, w, h);
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Drawable create(Texture m, Color edge, int w, int h) {
        if (!m.getTextureData().isPrepared()) {
            m.getTextureData().prepare();
        }
        Pixmap pixmap = m.getTextureData().consumePixmap();
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.drawPixmap(pixmap, 0, 0, pixmap.getWidth(), pixmap.getHeight(), 0, 0, w, h);
        labelColor.setColor(edge);
        labelColor.drawRectangle(0,0, w, h);
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Drawable create(Pixmap m, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.drawPixmap(m, 0, 0, w, h, 0, 0, m.getWidth(), m.getHeight());
        Color c = new Color();
        for(int i = 0; i < m.getWidth();i++) {
            for(int j = 0; j < m.getHeight();j++) {
                int color = m.getPixel(i, j);
                Color.rgba8888ToColor(c, color);
                c.a = 1.0f;
                labelColor.drawPixel(i,j, Color.rgba8888(c));
            }
        }
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Drawable create(Pixmap m, Color edge, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.drawPixmap(m, 0, 0, w, h, 0, 0, m.getWidth(), m.getHeight());
        Color c = new Color();
        for(int i = 0; i < m.getWidth();i++) {
            for(int j = 0; j < m.getHeight();j++) {
                int color = m.getPixel(i, j);
                Color.rgba8888ToColor(c, color);
                c.a = 1.0f;
                labelColor.drawPixel(i,j, Color.rgba8888(c));
            }
        }
        labelColor.setColor(edge);
        labelColor.drawRectangle(0,0, w, h);
        return new Image(new Texture(labelColor)).getDrawable();
    }

    public static Texture createSolidBackgroundT(Color color, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        return new Texture(labelColor);
    }

    public static Texture createSolidBackgroundT(Color color, Color edge, int w, int h) {
        Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        labelColor.setColor(color);
        labelColor.fill();
        labelColor.setColor(edge);
        labelColor.drawRectangle(0,0, w, h);
        return new Texture(labelColor);
    }
}