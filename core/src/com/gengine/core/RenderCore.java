package com.gengine.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.cell.NodeManager;
import com.gengine.core.cell.WorldManager;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.core.cell.node.components.RenderComponent;

import java.util.Collection;

public class RenderCore {

    public static boolean wireframe;
    private ModelBatch modelBatch;

    private PerspectiveCamera camera;
    private final WorldManager worldManager;
    private DecalBatch decalBatch;

    public RenderCore(final WorldManager worldManager) {
        this.worldManager = worldManager;
        this.modelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                if(renderable.material.has(TerrainNode.Attr.ATTRIBUTE_REGION)) {
                    return new TerrainNode.TerrainShader(worldManager, renderable);
                }
                return new DefaultShader(renderable);
            }
        });
    }

    public void setCamera(PerspectiveCamera camera) {
        this.camera = camera;
        this.decalBatch = new DecalBatch(new CameraGroupStrategy(camera));
    }

    public void render() {
        Collection<CellNode> nodeCollection = NodeManager.getNodes(RenderComponent.class);
        for(CellNode node : nodeCollection) {
            RenderComponent component = node.getComponent(RenderComponent.class);
            if(component == null) {
                System.out.println(node + " registered to rendering but does not have render component");
                continue;
            }
            //Components use both optionally for different kind of rendering.
            modelBatch.render(component);
            component.render(this);
        }
    }

    public void render(Renderable renderable) {
        modelBatch.render(renderable);
    }

    public void render(RenderableProvider renderable, Shader shader) {
        modelBatch.render(renderable, shader);
    }

    public void render(RenderableProvider renderable) {
        modelBatch.render(renderable);
    }

    public void render(Decal decal) {
        decalBatch.add(decal);
    }

    public void begin() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            modelBatch.dispose();
            modelBatch = new ModelBatch(new DefaultShaderProvider() {
                @Override
                protected Shader createShader(final Renderable renderable) {
                    return new TerrainNode.TerrainShader(worldManager, renderable);
                }
            });
        }
        modelBatch.begin(camera);
    }

    public void end() {
        modelBatch.end();
        decalBatch.flush();
    }

    public ModelBatch getModelBatch() {
        return modelBatch;
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
}
