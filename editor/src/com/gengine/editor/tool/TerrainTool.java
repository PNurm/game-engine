package com.gengine.editor.tool;

import com.gengine.core.model.terrain.TerrainTile;
import com.gengine.editor.Editor;
import com.gengine.render.world.TerrainNodeRenderContext;

public class TerrainTool extends EditorBrushTool {


    public void set(float wx, float wy, float val) {
        TerrainTile t = Editor.world().worldTile(wx, wy);
        if (t != null && t.terrainNode != null && t.terrainNode.getRenderContext() != null) {
            TerrainNodeRenderContext renderContext = (TerrainNodeRenderContext) t.terrainNode.getRenderContext();
            t.setHeight(val);
            renderContext.meshDirty = true;
        }
    }

    public void mod(float wx, float wy, float mod) {
        TerrainTile t = Editor.world().worldTile(wx, wy);
        if (t != null && t.terrainNode != null && t.terrainNode.getRenderContext() != null) {
            TerrainNodeRenderContext renderContext = (TerrainNodeRenderContext) t.terrainNode.getRenderContext();
            t.setHeight(t.getHeight() + mod);
            renderContext.meshDirty = true;
        }
    }

    @Override
    public String getIcon() {
        return null;
    }

}
