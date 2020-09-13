package com.gengine.core.library.provider;

import com.badlogic.gdx.files.FileHandle;
import com.gengine.core.library.LibraryElementProvider;
import com.gengine.core.cell.WorldCell;

import java.nio.ByteBuffer;

public class WorldCellProvider implements LibraryElementProvider<WorldCell> {
    @Override
    public WorldCell load(FileHandle fileHandle) {
        WorldCell worldCell = new WorldCell(fileHandle.nameWithoutExtension());
        worldCell.unpack(ByteBuffer.wrap(fileHandle.readBytes()));
        return worldCell;
    }
}
