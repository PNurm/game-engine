package com.gengine.core.library.provider;

import com.badlogic.gdx.files.FileHandle;
import com.gengine.core.cell.NodeManager;
import com.gengine.core.library.LibraryElementProvider;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.util.IOUtils;

import java.nio.ByteBuffer;

public class CellNodeProvider implements LibraryElementProvider<CellNode> {

    @Override
    public CellNode load(FileHandle fileHandle) {
        if(!fileHandle.exists()) {
            throw new NullPointerException("File not found");
        }
        ByteBuffer buffer = ByteBuffer.wrap(fileHandle.readBytes());
        int id = buffer.get() & 0xFF;
        try {
            CellNode node = NodeManager.Registry.classFor(id).newInstance();
            node.fileHandle = fileHandle;
            node.unpack(buffer);
            return node;
        } catch (Exception e) {
            System.out.println("Error loading node " + fileHandle.name() + " of type id " + id);
            e.printStackTrace();
        }
        return null;
    }
}
