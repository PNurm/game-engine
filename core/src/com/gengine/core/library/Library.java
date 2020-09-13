package com.gengine.core.library;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.cell.node.LightNode;
import com.gengine.core.library.ex.AssetProviderNotFoundException;
import com.gengine.core.library.provider.CellNodeProvider;
import com.gengine.core.library.provider.MaterialProvider;
import com.gengine.core.library.provider.TextureProvider;
import com.gengine.core.library.provider.WorldCellProvider;
import com.gengine.core.model.Material;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.CellNode;

import java.util.HashMap;

public class Library {

    private static final HashMap<Class<?>, LibraryElementProvider> assetProviders = new HashMap<>();
    private static final HashMap<String, Object> loadedAssets = new HashMap<>();

    /***
     * Map of filepath -> last file modified, used to detect if file needs to be reloaded
     */
    private static HashMap<String, Long> lastModified = new HashMap<>();

    public static LibraryElementProvider getAssetProvider(Class<?> type) throws AssetProviderNotFoundException {
        if(!assetProviders.containsKey(type)) {
            throw new AssetProviderNotFoundException(type);
        }
        return assetProviders.get(type);
    }


    public static Object provideAsset(Class<?> type, String path) throws AssetProviderNotFoundException {
        return provideAsset(type, Gdx.files.local(path));
    }

    public static Object provideAsset(Class<?> type, FileHandle handle) throws AssetProviderNotFoundException {
        if(!assetProviders.containsKey(type)) {
            throw new AssetProviderNotFoundException(type);
        }
        String filepath = handle.path();
        System.out.println("Loading asset " + type.getName() + " at " + filepath);
        //Check if file has been changed, reload if it has
        /*if(loadedAssets.containsKey(filepath)) {
            if(handle.lastModified() == lastModified.get(filepath)) {
                return loadedAssets.get(filepath);
            }
        }*/

        Object object = assetProviders.get(type).load(Gdx.files.absolute(filepath));
        if(object == null) {
            throw new NullPointerException("Unable to load " + type.getName() + " at " + filepath);
        }
        //lastModified.put(filepath, handle.lastModified());
        //loadedAssets.put(filepath, object);
        System.out.println("Provided asset " + type.getName() + " at " + filepath);
        return object;
    }

    public static Class<?> resolveType(String extension) {
        extension = extension.toLowerCase();
        if (extension.equals("mtl")) {
            return Material.class;
        }
        if (extension.equals("lnd")) {
            return LightNode.class;
        }
        if (extension.equals("nd")) {
            return CellNode.class;
        }
        if (extension.equals("jpg") || extension.equals("png")) {
            return Texture.class;
        }
        return null;
    }

    static {
        assetProviders.put(Material.class, new MaterialProvider());
        assetProviders.put(Texture.class, new TextureProvider());
        assetProviders.put(CellNode.class, new CellNodeProvider());
        assetProviders.put(WorldCell.class, new WorldCellProvider());
        //assetProviders.put(Texture.class, new TextureProvider());
    }

    //TODO:
    public static void freeAsset(String filepath) {
       /* if(!loadedAssets.containsKey(filepath)) {
            System.out.println("Warning: " + filepath + " is not loaded");
            return;
        }
        loadedAssets.remove(filepath);*/
    }
}
