package com.gengine.editor.ui.hierarchy;

import com.kotcrab.vis.ui.widget.*;

public class NodePopupMenu extends PopupMenu {

    public void setMenuItems(MenuItem[]... items) {
        remove();
        clearChildren();

        for(MenuItem[] menuItemArray : items) {
            for(MenuItem item : menuItemArray) {
                addItem(item);
            }
            addSeparator();
        }
    }


}
