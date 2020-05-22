package com.gengine.render.world;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.world.node.ModelNode;

public class ModelNodeRenderContext extends RenderContextProvider<ModelNode> {

    public ModelNodeRenderContext(ModelNode node) {
        super(node);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        getElement().getInstance().transform.set(getElement().getTransform());
        getElement().getInstance().getRenderables(renderables, pool);
    }
}
