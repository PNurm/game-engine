package com.gengine.core.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.gengine.core.world.NodeRegistry;
import com.gengine.core.world.node.CellNode;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class NodeLibrary {

    private static final String nodeDirectory = "assets/nodes/";
    private static final FileFilter nodeFileFilter = pathname -> pathname.getName().endsWith(".nd");

    public static HashMap<String, CellNode> nodeLibrary = new HashMap<>();

    public static void addNode(CellNode node) {
        nodeLibrary.put(node.getName(), node);
        saveNode(node);
    }

    private static void saveNode(CellNode node) {
        FileHandle newFileHandle = Gdx.files.local(nodeDirectory + node.getName() + ".nd");

        if (!newFileHandle.exists()) {
            FileHandle dir = Gdx.files.local(nodeDirectory);
            dir.mkdirs();

            File f = newFileHandle.file();
            try {
                FileOutputStream fos = new FileOutputStream(f);

                byte[] out = new byte[node.length()];
                ByteBuffer buffer = ByteBuffer.wrap(out);
                node.pack(buffer);

                fos.write(buffer.array());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void loadNodeLibrary() {
        FileHandle handle = Gdx.files.local(nodeDirectory);
        if (!handle.exists()) {
            System.out.println("Node directory doesnt exist");
            return;
        }
        for (FileHandle f : handle.list(nodeFileFilter)) {
            ByteBuffer buffer = ByteBuffer.wrap(f.readBytes());
            int id = buffer.get();

            try {
                CellNode node = NodeRegistry.classFor(id).newInstance();
                node.unpack(buffer);
                nodeLibrary.put(node.getName(), node);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    static {
        loadNodeLibrary();
    }
}
