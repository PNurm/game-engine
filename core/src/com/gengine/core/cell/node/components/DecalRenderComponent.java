package com.gengine.core.cell.node.components;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.RenderCore;

public class DecalRenderComponent extends RenderComponent {

    private Decal decal;

    public DecalRenderComponent(CellNode node) {
        super(node);
    }

    public void setDecal(Decal decal) {
        this.decal = decal;
    }

    @Override
    public void render(RenderCore renderCore) {
        Matrix4 matrix4 = getNode().getTransform();
        decal.setPosition(matrix4.getTranslation(new Vector3()));

        renderCore.render(decal);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }

    @Override
    public BoundingBox getBoundingBox(BoundingBox out, boolean transform) {
        out.min.set(-1f, -1f, -1f);
        out.max.set(1f, 1f, 1f);
        if (transform)
            out.mul(node.getTransform());
        return out;
    }

}
