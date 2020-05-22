package com.gengine.editor.ui.widgets;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisTable;

public class Toolbar {

    private VisTable root;

    private VisTable left;
    private VisTable right;

    public Toolbar() {
        root = new VisTable();
        left = new VisTable();
        right = new VisTable();

        root.align(Align.left | Align.center);

        left.left().top();
        root.add(left).pad(2f).expandX().fillX();

        right.right().top();
        root.add(right).pad(2f).expandX().fillX().row();

        root.addSeparator().pad(0f).height(2f).colspan(2);
    }

    public void addItem(Actor a, boolean left) {
        if(left)
            this.left.add(a);
        else
            this.right.add(a);
    }

    public void addSeperator(boolean left) {
        if(left)
            this.left.addSeparator();
        else
            this.right.addSeparator();
    }

}
