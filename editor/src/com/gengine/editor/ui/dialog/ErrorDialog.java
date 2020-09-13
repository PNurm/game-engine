package com.gengine.editor.ui.dialog;

import com.gengine.editor.Editor;
import com.kotcrab.vis.ui.widget.VisDialog;

public class ErrorDialog extends VisDialog {

    public ErrorDialog(String errorMessage) {
        super("Error!");
        text(errorMessage);
        button("Close");
    }

    public static void show(String string) {
        new ErrorDialog(string).show(Editor.ui);
    }
}
