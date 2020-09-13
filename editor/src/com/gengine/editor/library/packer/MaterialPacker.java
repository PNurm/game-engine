package com.gengine.editor.library.packer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.model.Material;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.TreeMap;

public class MaterialPacker {

    private static final String textureDirectory = "assets/library/textures/";

    public static boolean importTexture(FileHandle importFileHandle) {
        FileHandle matDirectory = Gdx.files.local(textureDirectory);
        if (!matDirectory.exists()) {
            matDirectory.file().mkdir();
        }
        if (importFileHandle.equals(matDirectory)) {
            return true;
        }
        String importName = importFileHandle.nameWithoutExtension();
        String ext = importFileHandle.extension();

        FileHandle copyTo = Gdx.files.local(textureDirectory + importName + "." + ext);

        int number = 1;
        while (copyTo.exists()) {
            importName = importFileHandle.name() + " (" + number + ")";
            number++;
            copyTo = Gdx.files.local(textureDirectory + importName + "." + ext);
        }

        importFileHandle.copyTo(copyTo);
        return true;
    }

    public static boolean save(FileHandle directory, Material material) {
        FileHandle newFileHandle = Gdx.files.local(directory + File.separator + material.getName() + ".mtl");
        if (!newFileHandle.exists()) {
            directory.mkdirs();
            File f = newFileHandle.file();
            try {
                FileOutputStream fos = new FileOutputStream(f);

                byte[] out = new byte[material.length()];
                ByteBuffer buffer = ByteBuffer.wrap(out);
                material.pack(buffer);

                fos.write(buffer.array());
                fos.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean pack(Material material) {
        File f = material.fileHandle.file();
        try {
            FileOutputStream fos = new FileOutputStream(f);

            byte[] out = new byte[material.length()];
            ByteBuffer buffer = ByteBuffer.wrap(out);
            material.pack(buffer);

            fos.write(buffer.array());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String avoidConflictingNames(String string, FileHandle directory) {
        String name = string;
        int number = 1;
        FileHandle fileTest = Gdx.files.local(directory + name);

        while (true) {
            if (!fileTest.exists()) {
                return name;
            }
            name = string + " (" + number + ")";
            number++;
            fileTest = Gdx.files.local(name);
        }
    }

    private static final FileFilter materialFileFilter = pathname -> pathname.getName().endsWith(".mtl");
}
