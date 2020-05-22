package com.gengine.core.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.model.Material;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.TreeMap;

public class MaterialLibrary {

    private static final String materialDirectory = "assets/materials/";
    private static final String textureDirectory = "assets/textures/";

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

    private static final TreeMap<String, Material> materials = new TreeMap<>();

    public static TreeMap<String, Material> getMaterials() {
        return materials;
    }

    public static boolean save(Material asset) {
        FileHandle newFileHandle = Gdx.files.local(materialDirectory + asset.getName());
        if (!newFileHandle.exists()) {
            FileHandle dir = Gdx.files.local(materialDirectory);
            dir.mkdirs();

            File f = newFileHandle.file();
            try {
                FileOutputStream fos = new FileOutputStream(f);

                byte[] out = new byte[asset.length()];
                ByteBuffer buffer = ByteBuffer.wrap(out);
                asset.pack(buffer);

                fos.write(buffer.array());
                fos.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static String avoidConflictingNames(String string) {
        String name = string;
        int number = 1;
        while (materials.containsKey(name + ".mtl")) {
            name = string + " (" + number + ")";
            number++;
            System.out.println("checking " + name);
        }
        return name;
    }

    public static void loadMaterials() {
        FileHandle handle = Gdx.files.local(materialDirectory);
        if (!handle.exists()) {
            System.out.println("material directory doesnt exist");
            return;
        }
        for (FileHandle f : handle.list(materialFileFilter)) {
            Material asset = new Material();
            asset.setName(f.name());
            asset.unpack(ByteBuffer.wrap(f.readBytes()));
            materials.put(asset.getName(), asset);
            System.out.println("Loaded material " + asset.getName());
        }
    }

    private static final FileFilter materialFileFilter = pathname -> pathname.getName().endsWith(".mtl");


    static {
        loadMaterials();
    }

    public Texture getSpecularTexture() {
        return null;
    }
}
