package com.gengine.render.world;

import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.gengine.core.world.node.RenderableNode;

public abstract class RenderContextProvider<T extends RenderableNode> implements RenderableProvider {

    public T getElement() {
        return element;
    }

    private final T element;

    public RenderContextProvider(T element) {
        this.element = element;
    }

    public void render(RenderCore renderCore) {

    }

}
