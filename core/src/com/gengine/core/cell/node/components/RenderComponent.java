package com.gengine.core.cell.node.components;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.RenderCore;

public abstract class RenderComponent extends NodeComponent implements RenderableProvider {

    public RenderComponent(CellNode element) {
        super(element);
    }

    public abstract void render(RenderCore renderCore);

    public abstract void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool);

    public abstract BoundingBox getBoundingBox(BoundingBox out, boolean world);
}
