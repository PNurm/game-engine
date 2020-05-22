package com.gengine.editor.tool;

public enum EditMode {
    OBJECT(new SelectionTool()),
    SCULPT(new TerrainTool()),
    TEXTURE(new TextureTool());

    private final EditorTool tool;

    EditMode(EditorTool tool) {
        this.tool = tool;
    }

    public EditorTool getTool() {
        return tool;
    }
}
