package com.gengine.editor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.gengine.util.Backgrounds;

public class Style {

    private static BitmapFont font = new BitmapFont();

    public static Slider.SliderStyle slider;
    public static Label.LabelStyle label;
    public static List.ListStyle list;

    public static ScrollPane.ScrollPaneStyle scroll;
    public static SelectBox.SelectBoxStyle selectBox;
    public static TextButton.TextButtonStyle textButton;

    public static Window.WindowStyle window;

    public static TextField.TextFieldStyle textField;

    static {
        slider = new Slider.SliderStyle();
        slider.knobDown = Backgrounds.create(Color.RED, 6, 6);
        slider.knob = Backgrounds.create(Color.DARK_GRAY, 6, 6);
        slider.knobOver = Backgrounds.create(Color.GRAY, 6, 6);
        slider.background = Backgrounds.create(Color.valueOf("#0e1111bb"), 12, 12);

        list = new List.ListStyle();
        list.background = Backgrounds.create(Color.valueOf("#0e1111bb"), 12, 12);
        list.selection = Backgrounds.create(Color.valueOf("#414a4c"), 12, 12);
        list.font = font;

        scroll = new ScrollPane.ScrollPaneStyle();
        scroll.background = Backgrounds.create(Color.valueOf("#0e1111bb"), 12, 12);

        label = new Label.LabelStyle();
        label.font = font;
        label.fontColor = Color.WHITE;

        selectBox = new SelectBox.SelectBoxStyle();
        selectBox.background = Backgrounds.create(Color.valueOf("#0e1111bb"), 12, 12);
        selectBox.font = font;
        selectBox.fontColor = Color.WHITE;
        selectBox.listStyle = list;
        selectBox.scrollStyle = scroll;

        textButton = new TextButton.TextButtonStyle();
        textButton.font = font;
        textButton.fontColor = Color.WHITE;
        textButton.downFontColor = Color.RED;
        textButton.checked = Backgrounds.create(Color.DARK_GRAY, Color.RED, 75, 25);
        textButton.checkedOverFontColor = Color.RED;
        textButton.over = Backgrounds.create(Color.DARK_GRAY, Color.RED, 75, 25);
        textButton.up = Backgrounds.create(Color.DARK_GRAY, Color.WHITE, 75, 25);

        window = new Window.WindowStyle();
        window.background = Backgrounds.create(Color.valueOf("#232b2bbb"), Color.GRAY, 200, 300);
        window.titleFont = font;

        textField = new TextField.TextFieldStyle();
        textField.focusedFontColor = Color.WHITE;
        textField.font  = font;
        textField.fontColor = Color.GRAY;
        textField.background = Backgrounds.create(Color.DARK_GRAY, Color.WHITE, 150, 20);
        textField.cursor = Backgrounds.create(Color.WHITE, 1, 1);


    }
}
