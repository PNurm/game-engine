package com.gengine.core.library.provider;

import com.badlogic.gdx.files.FileHandle;
import com.gengine.core.library.LibraryElementProvider;
import com.gengine.core.model.Material;

import java.nio.ByteBuffer;

public class MaterialProvider implements LibraryElementProvider<Material> {

    @Override
    public Material load(FileHandle fileHandle) {
        Material material = new Material();
        material.setName(fileHandle.name());
        material.unpack(ByteBuffer.wrap(fileHandle.readBytes()));
        material.fileHandle = fileHandle;
        return material;
    }
}
