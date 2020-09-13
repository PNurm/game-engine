package com.gengine.editor.ui.hierarchy;

import com.badlogic.gdx.files.FileHandle;

import static com.gengine.core.library.Library.resolveType;

public class LibraryTreeEntry {

    public final Class<?> type;
    public String name;
    public FileHandle fileHandle;

    public LibraryTreeEntry(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    public LibraryTreeEntry(FileHandle file) {
        this(file.name(), resolveType(file.extension()));
        this.fileHandle = file;
    }

    @Override
    public String toString() {
        return name;
    }

}
