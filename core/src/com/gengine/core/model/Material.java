package com.gengine.core.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.library.Library;
import com.gengine.core.library.ex.AssetProviderNotFoundException;
import com.gengine.core.library.provider.TextureProvider;
import com.gengine.core.util.DatabaseObject;
import com.gengine.core.util.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

public class Material implements DatabaseObject {

    public static final Material NULL = new Material();
    /**
     * The file this material was loaded from
     */
    public FileHandle fileHandle;

    public String name = "UNNAMED_MATERIAL";

    public String diffuseTexture;
    public String normalTexture;

    public Color diffuseColor = new Color();
    public Color ambientColor = new Color();
    public Color specularColor = new Color();

    public float shininess;
    public float opacity;
    private Texture loadedNormalTexture;


    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public float getShininess() {
        return shininess;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public String getName() {
        return name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public String getDiffuseTexture() {
        return diffuseTexture;
    }

    public String getNormalTexture() {
        return normalTexture;
    }

    private Texture loadedDiffuseTexture;

    public Texture resolveDiffuse() {
        if(diffuseTexture == null) {
            return null;
        }
        if (loadedDiffuseTexture == null) {
            try {
                loadedDiffuseTexture =  (Texture) Library.provideAsset(Texture.class, diffuseTexture);
            } catch (AssetProviderNotFoundException e) {
                e.printStackTrace();
            }
        }
        return loadedDiffuseTexture;
    }

    public Texture resolveNormal() {
        if(normalTexture == null) {
            return null;
        }
        if (loadedNormalTexture == null) {
            try {
                loadedNormalTexture =  (Texture) Library.provideAsset(Texture.class, normalTexture);
            } catch (AssetProviderNotFoundException e) {
                System.out.println("Unable to load texture asset");
                e.printStackTrace();
            }
        }
        return loadedNormalTexture;
    }

    @Override
    public int length() {
        int nameLength = IOUtils.sizeof(name);
        int diffuseTextureLength = IOUtils.sizeof(diffuseTexture);
        int normalTextureLength = IOUtils.sizeof(normalTexture);
        return 8 + nameLength + diffuseTextureLength + normalTextureLength;
    }

    @Override
    public void pack(ByteBuffer dest) {
        IOUtils.pack(name, dest);
        IOUtils.pack(diffuseTexture, dest);
        IOUtils.pack(normalTexture, dest);
        dest.putFloat(shininess);
        dest.putFloat(opacity);
    }

    @Override
    public void unpack(ByteBuffer src) {
        this.name = IOUtils.unpackString(src);
        this.diffuseTexture = IOUtils.unpackString(src);
        this.normalTexture = IOUtils.unpackString(src);
        this.shininess = src.getFloat();
        this.opacity = src.getFloat();
    }

    public void setDiffuse(String selectedTextureFile) {
        this.diffuseTexture = selectedTextureFile;
    }

    public void setNormal(String s) {
        this.normalTexture = s;
    }

    @Override
    public String toString() {
        return "Material{" +
                "name='" + name + '\'' +
                '}';
    }
}
