package com.gengine.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.gengine.core.cell.WorldManager;

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

}
