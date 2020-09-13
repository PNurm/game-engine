package com.gengine.core.library;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.gengine.core.library.LibraryElement;

public class LibraryTexture extends Texture implements LibraryElement {

    public LibraryTexture(String localPath) {
        super(localPath);
    }

    public LibraryTexture(FileHandle file) {
        super(file);
    }

    public LibraryTexture(FileHandle file, boolean useMipMaps) {
        super(file, useMipMaps);
    }

    public LibraryTexture(FileHandle file, Pixmap.Format format, boolean useMipMaps) {
        super(file, format, useMipMaps);
    }

    public LibraryTexture(Pixmap pixmap) {
        super(pixmap);
    }

    public LibraryTexture(Pixmap pixmap, boolean useMipMaps) {
        super(pixmap, useMipMaps);
    }

    public LibraryTexture(Pixmap pixmap, Pixmap.Format format, boolean useMipMaps) {
        super(pixmap, format, useMipMaps);
    }

    public LibraryTexture(int width, int height, Pixmap.Format format) {
        super(width, height, format);
    }

    public LibraryTexture(TextureData data) {
        super(data);
    }

    protected LibraryTexture(int glTarget, int glHandle, TextureData data) {
        super(glTarget, glHandle, data);
    }


}
