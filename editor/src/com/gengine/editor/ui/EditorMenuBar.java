package com.gengine.editor.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;

public class EditorMenuBar extends MenuBar {

    public EditorMenuBar() {
        buildFileMenu();
    }



    public void buildFileMenu() {
        Menu menu = new Menu("File");
        addMenu(menu);

        MenuItem save = new MenuItem("Save");
        save.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                super.clicked(event, x, y);
            }
        });
        menu.addItem(save);
    }

}
