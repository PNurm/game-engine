package com.gengine.render.world;

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
import com.gengine.Core;
import com.gengine.core.model.terrain.TerrainNodeAttribute;
import com.gengine.core.world.WorldManager;
import com.gengine.core.world.WorldCell;
import com.gengine.core.world.node.CellNode;
import com.gengine.core.world.node.RenderableNode;

import java.util.Collection;

public class RenderCore {

    public static boolean wireframe;
    private ModelBatch modelBatch;

    private PerspectiveCamera camera;
    private final WorldManager worldManager;
    private TerrainShader terrainShader;
    private Array<Renderable> renderables = new Array<Renderable>();
    private DecalBatch decalBatch;
    public RenderCore(final WorldManager worldManager) {
        this.worldManager = worldManager;
        this.modelBatch = new ModelBatch(new DefaultShaderProvider() {
            @Override
            protected Shader createShader(final Renderable renderable) {
                if(renderable.material.has(TerrainNodeAttribute.ATTRIBUTE_REGION)) {
                    return terrainShader = new TerrainShader(worldManager, renderable);
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
        for (WorldCell cell : worldManager.loadedCells.values()) {
            renderCellNode(cell.root);
        }
    }

    private void renderCellNode(CellNode childNode) {
        Collection<CellNode> children = (Collection<CellNode>) childNode.children();
        for(CellNode node : children) {
            renderCellNode(node);
        }
        if(childNode instanceof RenderableNode) {
            RenderableNode renderableNode = (RenderableNode) childNode;
            if(renderableNode.getRenderContext() == null) {
                RenderContextProvider contextProvider = Core.provideRenderContext(renderableNode);
                if(contextProvider == null) {
                    return;
                }
                renderableNode.setRenderContext(Core.provideRenderContext(renderableNode));
            }
            modelBatch.render(renderableNode.getRenderContext());
            renderableNode.getRenderContext().render(this);
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
                    return terrainShader = new TerrainShader(worldManager, renderable);
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
