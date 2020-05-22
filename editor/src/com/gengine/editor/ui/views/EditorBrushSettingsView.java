package com.gengine.editor.ui.views;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.gengine.editor.Style;
import com.gengine.editor.tool.BrushMode;
import com.gengine.editor.tool.BrushType;
import com.gengine.editor.tool.EditorBrushTool;
import com.kotcrab.vis.ui.widget.*;

public class EditorBrushSettingsView extends VisTable {

    private Label brushSizeLabel;
    private Label brushSpeedLabel;

    public EditorBrushSettingsView() {
        VisTable brush = new VisTable();

        final VisSlider slider = new VisSlider(1, 15, 0.5F, false);
        slider.setAnimateDuration(0);
        slider.setValue(EditorBrushTool.getBrushSize());
        slider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                EditorBrushTool.setBrushSize(slider.getValue());
                brushSizeLabel.setText("Brush Size: " + EditorBrushTool.getBrushSize());
            }
        });
        slider.pack();

        final VisSlider brushSpeedSlider = new VisSlider(1, 15, 0.5F, false);
        brushSpeedSlider.setAnimateDuration(0);
        brushSpeedSlider.setValue(EditorBrushTool.getBrushSpeed());
        brushSpeedSlider.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                EditorBrushTool.setBrushSpeed(brushSpeedSlider.getValue());

                brushSpeedLabel.setText("Brush Speed: " + brushSpeedSlider.getValue());
            }
        });
        brushSpeedSlider.pack();


        final SelectBox<BrushMode> brushModeSelector = new SelectBox<>(Style.selectBox);
        brushModeSelector.setItems(BrushMode.MODIFY, BrushMode.SMOOTH, BrushMode.FLAT);
        brushModeSelector.setAlignment(Align.center);
        brushModeSelector.setSelected(EditorBrushTool.getBrushMode());
        brushModeSelector.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                EditorBrushTool.setBrushMode(brushModeSelector.getSelected());
            }
        });

        final SelectBox<BrushType> brushTypeSelector = new SelectBox<>(Style.selectBox);
        brushTypeSelector.setItems(BrushType.SMOOTHC, BrushType.CIRCLE, BrushType.SQUARE);
        brushTypeSelector.setAlignment(Align.center);
        brushTypeSelector.setSelected(EditorBrushTool.getBrushType());
        brushTypeSelector.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                EditorBrushTool.setBrushType(brushTypeSelector.getSelected());
            }
        });

        brush.add(brushSizeLabel = new VisLabel("Brush Size:" + EditorBrushTool.getBrushSize())).space(3);
        brush.add(slider).row();

        brush.add(brushSpeedLabel = new VisLabel("Brush Speed:" + EditorBrushTool.getBrushSpeed())).space(3);
        brush.add(brushSpeedSlider).row();

        brush.add(new VisLabel("Brush Mode: ")).space(3);
        brush.add(brushModeSelector);

        brush.add(new VisLabel("Brush Type: ")).space(3);
        brush.add(brushTypeSelector);
        add(brush);
        pack();
    }

}
