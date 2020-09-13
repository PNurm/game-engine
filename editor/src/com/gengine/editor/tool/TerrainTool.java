package com.gengine.editor.tool;

import com.gengine.core.cell.WorldManager;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.editor.Editor;

public class TerrainTool extends EditorBrushTool {


    public void set(float wx, float wy, float val) {
        TerrainNode.Tile tile = WorldManager.worldTile(wx, wy);
        if (tile != null && tile.terrainNode != null) {
            tile.setHeight(val);

            if (tile.terrainNode.terrainMesh != null)
                tile.terrainNode.terrainMesh.meshDirty = true;
        }
    }

    public void mod(float wx, float wy, float mod) {
        TerrainNode.Tile tile = WorldManager.worldTile(wx, wy);

        if (tile != null && tile.terrainNode != null) {
            tile.setHeight(tile.getHeight() + mod);
            if (tile.terrainNode.terrainMesh != null)
                tile.terrainNode.terrainMesh.meshDirty = true;
        }
    }

    @Override
    public String getIcon() {
        return null;
    }

}
