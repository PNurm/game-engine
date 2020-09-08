package com.gengine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Fa {

    public static final String PALETTE = "\uf53f";
    public static BitmapFont font;

    public static void initialize() {

        StringBuilder glyphBuilder = new StringBuilder();

        Field[] fields = Fa.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType().equals(String.class)) {
                try {
                    glyphBuilder.append(f.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("assets/fonts/fa-regular-400.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 12;

        params.minFilter = Texture.TextureFilter.Nearest;
        params.magFilter = Texture.TextureFilter.MipMapLinearNearest;
        params.color = Color.WHITE;
        params.borderWidth = 3;
        params.characters = "" + '\uf0f3';

        font = fontGenerator.generateFont(params);
        fontGenerator.dispose();
    }

    public static final String MOUNTAIN = "\uf6fc";
    public static final String VECTOR_SQUARE = "\uf5cb";
}
