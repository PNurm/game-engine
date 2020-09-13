package com.gengine.editor;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.math.collision.Ray;
import com.gengine.core.Core;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.editor.tool.EditMode;
import com.gengine.editor.tool.GlyphShader;
import com.gengine.editor.ui.UI;
import com.gengine.editor.ui.widgets.SceneRenderWidget;

public class Editor extends Core implements InputProcessor {

    public static UI ui;

    public static void setEditMode(EditMode editMode) {
        Editor.editMode = editMode;
    }
    private static EditMode editMode;

    private static WorldCell lastCell;

    private static Ray lastRay;
    private final Vector3 terrainPick = new Vector3().set(0, 0, 0);
    private CameraController cameraController;
    private TerrainLine sectorBoundary;
    private boolean initialized;
    private TerrainLine targetBoundary;
    public static Ray getLastRay() {
        return lastRay;
    }

    public static WorldCell getCurrentCell() {
        return world().getCurrentCell();
    }

    public static void smoothTranslate(Vector3 position) {
        CameraController.smoothTranslate = position.cpy();
        CameraController.smoothPos = 0.0F;
    }

    @Override
    public void create() {
        super.create();
        editMode = EditMode.SCULPT;

        sectorBoundary = new TerrainLine(64);
        targetBoundary = new TerrainLine(64);

        ui = new UI();
        ui.getSceneWidget().setCam(camera);
        final GlyphShader glyphShader = new GlyphShader();
        ui.getSceneWidget().setRenderer(new SceneRenderWidget.Renderer() {
            @Override
            public void render(Camera cam) {

                submitLines();
                renderCore.begin();
                renderCore.render();
                editMode.getTool().render(renderCore);
                renderCore.render(targetBoundary, glyphShader);
                renderCore.render(sectorBoundary, glyphShader);
                renderCore.end();

            }
        });

        camera.near = 0.1f;
        camera.far = 256f;
        camera.update();

        cameraController = new CameraController(camera);
        cameraController.setCamera(140, 30, 240);

        Gdx.input.setInputProcessor(new InputMultiplexer(ui, this, cameraController));
    }


    @Override
    public void render() {
        glProfiler.reset();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);

        world().updateCellView(cameraController.getTarget().x, cameraController.getTarget().z);
        if (world().getCurrentCell() != lastCell) {
            lastCell = world().getCurrentCell();
            ui.getCellTree().refreshNodeTree();
            ui.getTexturePaintView().regionUpdated();
        }
        editMode.getTool().update();
        ui.act();
        ui.draw();
        ui.updateDebugs(glProfiler);
        cameraController.update();
        initialized = true;
    }

    private void submitLines() {
        targetBoundary.submit(cameraController.getTarget().x - 3f, cameraController.getTarget().y, cameraController.getTarget().z - 3f);
        targetBoundary.submit(cameraController.getTarget().x + 3f, cameraController.getTarget().y, cameraController.getTarget().z - 3f);
        targetBoundary.submit(cameraController.getTarget().x + 3f, cameraController.getTarget().y, cameraController.getTarget().z + 3f);
        targetBoundary.submit(cameraController.getTarget().x - 3f, cameraController.getTarget().y, cameraController.getTarget().z + 3f);

        if (lastCell != null) {
            WorldCell l = lastCell;
            sectorBoundary.submit(l.worldX(), l.worldY());
            sectorBoundary.submit(l.worldX() + WorldCell.SIZE * WorldCell.TILE_SIZE, l.worldY());
            sectorBoundary.submit(l.worldX() + WorldCell.SIZE * WorldCell.TILE_SIZE, l.worldY() + WorldCell.SIZE * WorldCell.TILE_SIZE);
            sectorBoundary.submit(l.worldX(), l.worldY() + WorldCell.SIZE * WorldCell.TILE_SIZE);
        }

        targetBoundary.flush();
        sectorBoundary.flush();
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.F3) {
            TerrainNode.TerrainShader.DEBUG_NORMALS = !TerrainNode.TerrainShader.DEBUG_NORMALS;
            System.out.println(TerrainNode.TerrainShader.DEBUG_NORMALS);
        }
        if(keycode == Input.Keys.F2) {
            TerrainNode.TerrainShader.WIREFRAME = !TerrainNode.TerrainShader.WIREFRAME;
        }
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return editMode.getTool().getInputAdapter().touchDown(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return editMode.getTool().getInputAdapter().touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!initialized) {
            return false;
        }
        lastRay = ui.getSceneWidget().getViewport().getPickRay(screenX, screenY);
        return editMode.getTool().getInputAdapter().touchDragged(screenX, screenY, pointer);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        if (!initialized) {
            return false;
        }
        lastRay = ui.getSceneWidget().getViewport().getPickRay(screenX, screenY);
        terrainPick.set(Core.world().pickPoint(terrainPick, lastRay.cpy()));
        return editMode.getTool().getInputAdapter().mouseMoved(screenX, screenY);
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void resize(int width, int height) {
        ui.getViewport().update(width, height);
        super.resize(width, height);
    }
}
