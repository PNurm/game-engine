package com.gengine.editor.library.packer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.gengine.core.cell.NodeManager;
import com.gengine.core.cell.node.CellNode;

import java.io.*;
import java.nio.ByteBuffer;

public class CellNodePacker {

    private static final String nodeDirectory = "assets/library/nodes/";

    public static void pack(CellNode node) {
        FileHandle fileHandle = node.fileHandle;
        //We're updating existing file
        if(fileHandle == null) {
            Gdx.files.local(nodeDirectory).mkdirs(); //Ensure we've got the library directory
            fileHandle = Gdx.files.local(nodeDirectory + node.getIdentifier() + "." + node.getExtension());
        }
        try {
            packInternal(fileHandle, node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void packInternal(FileHandle fileHandle, CellNode node) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileHandle.file());

        byte[] out = new byte[1 + node.length()];
        ByteBuffer buffer = ByteBuffer.wrap(out);

        //Write the type first
        int id = NodeManager.Registry.idFor(node.getClass());
        buffer.put((byte) id);

        node.pack(buffer);

        fos.write(buffer.array());
        fos.close();
    }

}
