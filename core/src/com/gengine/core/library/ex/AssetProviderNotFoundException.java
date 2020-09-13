package com.gengine.core.library.ex;

import com.badlogic.gdx.files.FileHandle;

public class AssetProviderNotFoundException extends Exception {

    public AssetProviderNotFoundException(Class<?> type, FileHandle handle) {
        super("AssetProvider not found for type " + type.getName() + " at " + handle.path());
    }
    public AssetProviderNotFoundException(Class<?> type) {
        super("AssetProvider not found for type " + type.getName() + "");
    }
}
