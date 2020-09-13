package com.gengine.editor.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.Ray;
import com.gengine.core.Core;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.editor.Editor;
import com.gengine.editor.util.Backgrounds;

import static com.gengine.core.cell.WorldCell.TILE_SIZE;

public class TextureTool extends EditorBrushTool {

    private static int selectedChannelID;
    private boolean beginPointSet;
    private Vector2 lastPoint = new Vector2();
    private Vector2 nextPoint = new Vector2();

    public static void setSelectedTextureID(int channel) {
        selectedChannelID = channel;
    }

    @Override
    public void mod(float wx, float wy, float mod) {

    }

    @Override
    public void set(float wx, float wy, float mod) {

    }

    Vector2 temp = new Vector2();

    @Override
    public void update() {
        float delta = Gdx.graphics.getDeltaTime();
        float brushSize = getBrushSize();
        float brushSpeed = getBrushSpeed();

        float modScale = delta * brushSpeed;
        Ray ray = Editor.getLastRay();

        Core.world().pickPoint(terrainPoint, ray);

        boolean primaryButtonDown = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean secondaryButtonDown = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

        if (primaryButtonDown || secondaryButtonDown) {

            WorldCell cell = Editor.world().getCellFromWorld(terrainPoint.x, terrainPoint.z);
            TerrainNode terrainNode = cell.getTerrain();

            if (terrainNode == null) {
                return;
            }

            TerrainNode.TextureMap textureMap = terrainNode.getTerrainBlendMap();
            Vector2 nextPoint = getBlendMapPos(terrainPoint.x, terrainPoint.z);

            if(lastPoint.equals(nextPoint)) {
                return;
            }

            if (!beginPointSet) {
                lastPoint.set(nextPoint);
                beginPointSet = true;
            }


            float scale = modScale * (primaryButtonDown ? 1 : -1);
            float dist = nextPoint.dst(lastPoint);

            int splatBrushSize = (int) brushSize * (TerrainNode.TextureMap.WIDTH / WorldCell.WORLD_SIZE);

            float alphaStep = splatBrushSize / (8f * dist);

            for (float step = 0; step < 1f; step += alphaStep) {
                Vector2 l = lastPoint.lerp(nextPoint, step);
                for (float x = -splatBrushSize; x < splatBrushSize; x++) {
                    for (float y = -splatBrushSize; y < splatBrushSize; y++) {
                        float opacity = scale * getBrushType().weight(splatBrushSize, x, y);
                        if (opacity > 0) {
                            drawDot(textureMap, (int) (l.x + x), (int) (l.y + y), opacity);
                        }
                    }
                }
            }

            lastPoint.set(nextPoint);

            textureMap.updateTexture();
            Editor.ui.getTexturePaintView().updateBlendImage(Backgrounds.create(textureMap.getMap(), TerrainNode.TextureMap.WIDTH, TerrainNode.TextureMap.HEIGHT));
        } else {
            beginPointSet = false;
        }
    }

    public void drawDot(TerrainNode.TextureMap blendMap, int xx, int yy, float alpha) {
        int blendedColor = blend(blendMap.getMap().getPixel(xx, yy), alpha);
        blendMap.getMap().drawPixel(xx, yy, blendedColor);
    }

    public Vector2 getBlendMapPos(float worldX, float worldZ) {
        int cellX = (int) Math.floor((worldX / TILE_SIZE) / WorldCell.SIZE);
        int cellY = (int) Math.floor((worldZ / TILE_SIZE) / WorldCell.SIZE);

        int cellStartX = cellX * WorldCell.WORLD_SIZE;
        int cellStartZ = cellY * WorldCell.WORLD_SIZE;

        float cellLocalX = worldX - cellStartX;
        float cellLocalZ = worldZ - cellStartZ;

        int x = (int) (cellLocalX * (TerrainNode.TextureMap.WIDTH / WorldCell.WORLD_SIZE));
        int y = (int) (cellLocalZ * (TerrainNode.TextureMap.HEIGHT / WorldCell.WORLD_SIZE));
        return new Vector2(x, y);
    }

    @Override
    public String getIcon() {
        return null;
    }


    private Color color = new Color();

    public int blend(int col, float mod) {
        this.color.set(col);

        if (selectedChannelID == 0) {
            color.r += mod;
            //color.add(mod, 0, 0, 0);
        } else if (selectedChannelID == 1) {
            color.g += mod;
           // color.add(0, mod, 0, 0);
        } else if (selectedChannelID == 2) {
            color.b += mod;
           // color.add(0, 0, mod, 0);
        } else if (selectedChannelID == 3) {
            color.a += mod;
           // color.add(0, 0, 0, mod);
        }

        final float sum = color.r + color.g + color.b + color.a;
        if (sum > 0) {
            color.r /= sum;
            color.g /= sum;
            color.b /= sum;
            color.a /= sum;
        }
        /*for (float f : mapping)
            tw += f;
        if (tw > 0)
            for (int i = 0; i < mapping.length; i++)
                mapping[i] /= tw;*/
        return Color.rgba8888(color);
    }

    /*public void additiveBlend(TerrainNode.Tile tile, int textureID, float strength) {
        if (tile.terrainNode.getMaterial(textureID) == null) {
            return;
        }
        tile.materialWeights[textureID] += strength;

        // prevent the sum to be greater than 1
        final float sum = tile.materialWeights[0] + tile.materialWeights[1] + tile.materialWeights[2] + tile.materialWeights[3];
        if (sum > 1f) {
            final float correction = 1f / sum;
            tile.materialWeights[0] *= correction;
            tile.materialWeights[1] *= correction;
            tile.materialWeights[2] *= correction;
            tile.materialWeights[3] *= correction;
        }
        if (tile.materialWeights[textureID] < 0) {
            tile.materialWeights[textureID] = 0;
        }

        TerrainNodeRenderContext renderContext = (TerrainNodeRenderContext) tile.terrainNode;
        renderContext.materialDirty = true;
        renderContext.meshDirty = true;
    }*/
}
