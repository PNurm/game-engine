package com.gengine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.gengine.core.world.WorldManager;
import com.gengine.core.world.node.CellNode;
import com.gengine.core.world.node.ModelNode;
import com.gengine.core.world.node.RenderableNode;
import com.gengine.core.world.node.TerrainNode;
import com.gengine.render.world.ModelNodeRenderContext;
import com.gengine.render.world.RenderContextProvider;
import com.gengine.render.world.RenderCore;
import com.gengine.render.world.TerrainNodeRenderContext;
import com.gengine.util.Fa;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class Core extends ApplicationAdapter {

    public static RenderCore renderCore;
    private static WorldManager worldManager;
    private static Core instance;
    public static PerspectiveCamera camera;
    public static GLProfiler glProfiler;

    @Override
    public void create() {
        this.instance = this;
        glProfiler = new GLProfiler(Gdx.graphics);
        glProfiler.enable();

        worldManager = new WorldManager();

        camera = new PerspectiveCamera(70, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());


        renderCore = new RenderCore(worldManager);
        renderCore.setCamera(camera);

        //Fa.initialize();
    }



    public static WorldManager world() {
        return instance.worldManager;
    }

    public static RenderContextProvider provideRenderContext(RenderableNode c) {
        try {
            Class<? extends RenderContextProvider> provider = renderContextImpl.get(c.getClass());
            if(provider == null) {
                return null;
            }
            Constructor constructor = provider.getConstructor(c.getClass());

            return (RenderContextProvider) constructor.newInstance(c);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected static HashMap<Class<? extends CellNode>, Class<? extends RenderContextProvider>> renderContextImpl = new HashMap<>();

    static {
        renderContextImpl.put(TerrainNode.class, TerrainNodeRenderContext.class);
        renderContextImpl.put(ModelNode.class, ModelNodeRenderContext.class);
    }
}
