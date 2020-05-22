package com.gengine.core.model;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.gengine.core.assets.TextureProvider;
import com.gengine.util.DatabaseObject;
import com.gengine.util.IOUtils;

import java.nio.ByteBuffer;

public class Material implements DatabaseObject {

    public static final Material NULL = new Material();

    public String name;

    public String diffuseTexture;
    public String normalTexture;

    public Color diffuseColor = new Color();
    public Color ambientColor = new Color();
    public Color specularColor = new Color();

    public float shininess;
    public float opacity;


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

    public Texture resolveDiffuse() {
        return new TextureProvider(diffuseTexture).load();
    }
    public Texture resolveNormal() {
        return new TextureProvider(diffuseTexture).load();
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
}
