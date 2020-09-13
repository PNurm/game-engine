package com.gengine.core.library;

import com.badlogic.gdx.files.FileHandle;

public interface LibraryElementProvider<T> {
    public T load(FileHandle fileHandle);
}
