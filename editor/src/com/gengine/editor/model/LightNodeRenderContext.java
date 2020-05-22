package com.gengine.editor.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.world.node.LightNode;
import com.gengine.render.world.RenderContextProvider;
import com.gengine.render.world.RenderCore;

public class LightNodeRenderContext extends RenderContextProvider<LightNode> {


    private final Decal decal;

    public LightNodeRenderContext(LightNode light) {
        super(light);

        Texture texture = new Texture(Gdx.files.internal("assets/icons/lightbulb.png"));
        decal = Decal.newDecal(new TextureRegion(texture), true);
        decal.setWidth(1.5f);
        decal.setHeight(1.5f);

    }

    @Override
    public void render(RenderCore renderCore) {
        Matrix4 matrix4 = getElement().getTransform();
        decal.setPosition(matrix4.getTranslation(new Vector3()));

        renderCore.render(decal);
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {

    }
}
