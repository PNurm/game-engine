package com.gengine.editor.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.gengine.Core;
import com.gengine.editor.Editor;
import com.gengine.editor.TerrainLine;
import com.gengine.render.world.RenderCore;

public abstract class EditorBrushTool extends EditorTool {

    private static BrushMode brushMode = BrushMode.MODIFY;
    private static BrushType brushType = BrushType.CIRCLE;
    private static float brushSize = 1;
    private static float brushSpeed = 2;

    protected Vector3 terrainPoint = new Vector3();

    public abstract void mod(float wx, float wy, float mod);
    public abstract void set(float wx, float wy, float mod);

    private TerrainLine line;

    public EditorBrushTool() {
        line = new TerrainLine(128);
    }

    public static BrushMode getBrushMode() {
        return brushMode;
    }

    public static void setBrushMode(BrushMode selected) {
        brushMode = selected;
    }

    public static BrushType getBrushType() {
        return brushType;
    }

    public static void setBrushType(BrushType selected) {
        brushType = selected;
    }

    public static float getBrushSize() {
        return brushSize;
    }

    public static void setBrushSize(float value) {
        brushSize = value;
    }

    public static float getBrushSpeed() {
        return brushSpeed;
    }

    public static void setBrushSpeed(float value) {
        brushSpeed = value;
    }


    @Override
    public void render(RenderCore renderCore) {
        final int vcount = 64;
        if (brushType == BrushType.CIRCLE || brushType == BrushType.SMOOTHC) {
            for (int i = 0; i < vcount; i++) {
                float th = (float) (i * Math.PI * 2 / vcount);
                float wx = terrainPoint.x + (float) Math.cos(th) * brushSize;
                float wy = terrainPoint.z + (float) Math.sin(th) * brushSize;
                line.submit(wx, wy);
            }
        } else if (brushType == BrushType.SQUARE) {
            line.submit(terrainPoint.x - brushSize, terrainPoint.z - brushSize);
            line.submit(terrainPoint.x - brushSize, terrainPoint.z + brushSize);
            line.submit(terrainPoint.x + brushSize, terrainPoint.z + brushSize);
            line.submit(terrainPoint.x + brushSize, terrainPoint.z - brushSize);
        }
        line.flush();
        renderCore.render(line);
    }

    @Override
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();

        float modScale = delta * brushSpeed;
        Ray ray = Editor.getLastRay();
        Core.world().pickPoint(terrainPoint, ray);

        boolean primaryButtonDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean secondaryButtonDown = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        if(primaryButtonDown || secondaryButtonDown) {
            switch (brushMode) {
                case MODIFY: {
                    float mod = modScale * (primaryButtonDown ? 1 : -1);
                    for (float x = -brushSize; x <= brushSize; x++) {
                        for (float z = -brushSize; z <= brushSize; z++) {
                            mod(terrainPoint.x + x, terrainPoint.z + z, mod * brushType.weight(brushSize, x, z));
                        }
                    }
                    break;
                }
                case SMOOTH: {
                    float avg = 0;
                    float tw = 0;
                    for (float x = -brushSize; x <= brushSize; x++)
                        for (float z = -brushSize; z <= brushSize; z++) {
                            float v = Editor.world().worldTile(terrainPoint.x + x, terrainPoint.z + z).getHeight();
                            float w = brushType.weight(brushSize, x, z);
                            avg += v * w;
                            tw += w;
                        }
                    if (tw > 0) {
                        avg /= tw;
                        for (float x = -brushSize; x <= brushSize; x++)
                            for (float z = -brushSize; z <= brushSize; z++) {
                                float v = Editor.world().worldTile(terrainPoint.x + x, terrainPoint.z + z).getHeight();
                                float w = brushType.weight(brushSize, x, z);
                                set(terrainPoint.x + x, terrainPoint.z + z, v + (avg - v) * modScale * w / 3);
                            }
                    }
                    break;
                }
                case FLAT: {
                    float h = Editor.world().worldTile(terrainPoint.x, terrainPoint.z).getHeight();
                    for (float x = -brushSize; x <= brushSize; x++)
                        for (float z = -brushSize; z <= brushSize; z++)
                            if (brushType.weight(brushSize, x, z) > 0f)
                                set(terrainPoint.x + x, terrainPoint.z + z, h);
                    break;
                }
                default:
            }
        }
    }

    @Override
    public String getIcon() {
        return null;
    }
}
