package com.gengine.editor.library.packer;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import java.util.HashMap;

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class ModelPacker {

    public static final HashMap<String, Model> modelLibrary = new HashMap<>();

    public static void addModel(String identifier, Model model) {
        modelLibrary.put(identifier, model);
    }

    public static Model getModel(String identifier) {
        return modelLibrary.get(identifier);
    }


    static {
        ModelBuilder modelBuilder = new ModelBuilder();
        Model sphere = modelBuilder.createSphere(30, 30, 30, 30, 30, GL_TRIANGLES, new Material(), VertexAttributes.Usage.Position);
        addModel("sphere", sphere);
    }
}
