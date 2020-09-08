package com.gengine.editor.ui.views;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.gengine.editor.Editor;
import com.gengine.editor.tool.EditMode;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class EditorToolbar {

    private final VisTextButton terrain;
    private final VisTextButton select;
    private final VisTextButton texturePaint;

    private VisTable root;

    public VisTable getRoot() {
        return root;
    }

    private VisTable left;
    private VisTable right;

    public EditorToolbar() {
        root = new VisTable();
        left = new VisTable();
        right = new VisTable();

        root.align(Align.left | Align.center);

        left.left().top();
        root.add(left).pad(2f).expandX().fillX();

        right.right().top();
        root.add(right).pad(2f).expandX().fillX().row();

        root.addSeparator().pad(0f).height(2f).colspan(2);

        select = new VisTextButton("Object");
        select.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Editor.ui.showToolWidget("OBJECT", null);
                Editor.setEditMode(EditMode.OBJECT);
            }
        });
        addItem(select, true);

        terrain = new VisTextButton("Terrain");
        terrain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Editor.ui.showToolWidget("Terrain", Editor.ui.getBrushSettingsView());
                Editor.setEditMode(EditMode.SCULPT);
            }
        });
        addItem(terrain, true);

        texturePaint = new VisTextButton("Paint");
        texturePaint.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Editor.ui.showToolWidget("Texture Paint", Editor.ui.getTexturePaintView());
                Editor.setEditMode(EditMode.TEXTURE);
            }
        });
        addItem(texturePaint, true);

    }

    public TextureRegionDrawable createDrawable(FileHandle handle) {
        TextureRegionDrawable r = new TextureRegionDrawable(new TextureRegion(new Texture(handle)));
        return r;
    }

    public void addItem(Actor a, boolean left) {
        if(left)
            this.left.add(a).pad(5);
        else
            this.right.add(a).pad(5);
    }

    public void addSeperator(boolean left) {
        if(left)
            this.left.addSeparator();
        else
            this.right.addSeparator();
    }

    public VisTable getRight() {
        return right;
    }
}
