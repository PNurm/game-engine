package com.gengine.editor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.gengine.util.Fa;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class FaTextButton extends VisTextButton {

    public final static TextButton.TextButtonStyle styleNoBg = new TextButton.TextButtonStyle();
    static {
        styleNoBg.font = Fa.font;
        styleNoBg.fontColor = Color.WHITE;
        styleNoBg.pressedOffsetX = 1;
        styleNoBg.unpressedOffsetX = 0;
        styleNoBg.pressedOffsetY = -1;
    }

    public final static TextButton.TextButtonStyle styleBg = new TextButton.TextButtonStyle();
    static {
        styleBg.font = Fa.font;
        styleBg.pressedOffsetX = 1;
        styleBg.unpressedOffsetX = 0;
        styleBg.pressedOffsetY = -1;
    }

    public final static TextButton.TextButtonStyle styleActive = new TextButton.TextButtonStyle();
    static {
        styleActive.font = Fa.font;
        styleActive.pressedOffsetX = 1;
        styleActive.unpressedOffsetX = 0;
        styleActive.pressedOffsetY = -1;
        styleActive.fontColor = Color.WHITE;
    }

    public FaTextButton(String text) {
        this(text, styleBg);
    }

    public FaTextButton(String text, TextButtonStyle style) {
        super(text);
        setStyle(style);
        setFocusBorderEnabled(false);
    }

}
