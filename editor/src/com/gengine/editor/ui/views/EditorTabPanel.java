package com.gengine.editor.ui.views;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;

public class EditorTabPanel extends VisTable {

    private final ButtonGroup<Button> buttonGroup;

    private VisTable tabs;
    private VisTable tabViews;

    public EditorTabPanel() {
        buttonGroup = new ButtonGroup<>();
        tabs = new VisTable();
        tabViews = new VisTable();

        add(tabs);
        add(tabViews);

    }

    public void addView(Button button, final VisTable view) {
        this.buttonGroup.add(button);
        this.tabs.add(button);
        this.tabViews.add(view);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                for(Actor a : tabViews.getChildren()) {
                    a.setVisible(false);
                }
                view.setVisible(true);
                super.clicked(event, x, y);
            }
        });
    }

}
