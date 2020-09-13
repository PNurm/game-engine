package com.gengine.editor.tool;

import com.badlogic.gdx.InputAdapter;
import com.gengine.core.RenderCore;

public abstract class EditorTool {

    private InputAdapter inputAdapter = new InputAdapter();

    public abstract void render(RenderCore renderCore);
    public abstract void update();

    public abstract String getIcon();


    public InputAdapter getInputAdapter() {
        return inputAdapter;
    }

    public void setInputAdapter(InputAdapter inputAdapter) {
        this.inputAdapter = inputAdapter;
    }
}
