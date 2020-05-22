package com.gengine.core.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class TextureProvider {

    private static final String textureDirectory = "assets/textures/";

    private String textureFileName;

    public TextureProvider(String textureFileName) {
        this.textureFileName = textureFileName;
    }

    public Texture load() {
        FileHandle fileHandle = Gdx.files.local(textureDirectory + textureFileName);
        if(!fileHandle.exists())
            return null;
        Texture texture = new Texture(fileHandle, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }


}
