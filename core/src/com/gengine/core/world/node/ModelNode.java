package com.gengine.core.world.node;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;

import java.nio.ByteBuffer;

public class ModelNode extends RenderableNode {

    private ModelInstance instance;
    private Model model;

    @Override
    public void pack(ByteBuffer data) {

    }

    @Override
    public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
        instance.calculateBoundingBox(out);
        if (world)
            out.mul(getTransform());
        return out;
    }

    public ModelNode(String identifier) {
        super("Model[" + identifier + "]");
    }

    public void setModel(Model model) {
        this.model = model;
        this.instance = new ModelInstance(model);
    }

    public ModelInstance getInstance() {
        return instance;
    }

    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "Model: " + getName();
    }
}

