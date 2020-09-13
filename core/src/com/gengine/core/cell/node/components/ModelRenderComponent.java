package com.gengine.core.cell.node.components;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.RenderCore;

public class ModelRenderComponent extends RenderComponent {

    private RenderableProvider renderableProvider;

    private Model model;
    private ModelInstance instance;

    public ModelRenderComponent(CellNode element) {
        super(element);
    }

    @Override
    public void render(RenderCore renderCore) {
        renderCore.render(renderableProvider);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        renderableProvider.getRenderables(renderables, pool);
    }


    public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
        if(instance == null)
            return null;

        instance.calculateBoundingBox(out);
        if (world)
            out.mul(getNode().getTransform());
        return null;
    }

    public void setRenderable(RenderableProvider renderableProvider) {
        this.renderableProvider = renderableProvider;
    }

    public void setModel(Model model) {
        this.model = model;
        this.instance = new ModelInstance(model);
        this.renderableProvider = instance;
    }

}
