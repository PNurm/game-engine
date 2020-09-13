package com.gengine.core.library.provider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.library.LibraryElementProvider;

public class TextureProvider implements LibraryElementProvider<Texture> {

    public Texture load(FileHandle fileHandle) {
        FileHandle handle = Gdx.files.local("assets/library/textures/" + fileHandle.path());
        Texture texture = new Texture(handle, true);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Nearest);
        texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        return texture;
    }
}
