package com.gengine.core.world.node;

import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.render.world.RenderContextProvider;


public abstract class RenderableNode extends CellNode {

    public abstract BoundingBox getBoundingBox(BoundingBox out, boolean world);

    private RenderContextProvider renderContext = null;

    public RenderableNode(String identifier) {
        super(identifier);
    }

    public void setRenderContext(RenderContextProvider provider) {
        this.renderContext = provider;
    }

    public RenderContextProvider getRenderContext() {
        return renderContext;
    }
}
