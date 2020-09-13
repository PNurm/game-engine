package com.gengine.editor.ui.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.gengine.core.model.Material;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.editor.Editor;
import com.gengine.editor.Style;
import com.gengine.editor.tool.*;
import com.gengine.editor.util.Backgrounds;
import com.gengine.editor.ui.dialog.material.MaterialSelectCallback;
import com.gengine.editor.ui.dialog.material.MaterialSelectorPreview;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;

public class TexturePaintView extends VisTable {

    private final VisLabel brushSpeedLabel;
    private final Image blendImage;
    private ImageButton.ImageButtonStyle[] textureSlotStyles = new ImageButton.ImageButtonStyle[4];
    private ImageButton[] textureSlots = new ImageButton[4];

    private ButtonGroup textureSlotGroup;
    private VisLabel brushSizeLabel;

    public TexturePaintView()
    {

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

        VisTable textureTable = new VisTable();
        textureSlotGroup = new ButtonGroup();
        textureSlotGroup.setMaxCheckCount(1);
        textureTable.add(createTextureSlot(0));
        textureTable.add(createTextureSlot(1));
        textureTable.add(createTextureSlot(2));
        textureTable.add(createTextureSlot(3));
        textureSlotGroup.add(textureSlots);


        VisTable preview = new VisTable();
        preview.align(Align.top);
        preview.add(new VisLabel("Cell Materials:", Style.label)).padRight(5);
        preview.add(textureTable).space(15);

        preview.add(new VisLabel("Brush Settings", Style.label)).padRight(10);
        preview.add(brush);
        preview.add(blendImage = new Image(Backgrounds.create(Color.DARK_GRAY, TerrainNode.TextureMap.WIDTH, TerrainNode.TextureMap.HEIGHT))).size(128, 128);
        preview.row();
        blendImage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                VisDialog dialog = new VisDialog("Texture Blend Map");
                dialog.addCloseButton();
                TerrainNode.TextureMap blendMap = Editor.getCurrentCell().getTerrain().getTerrainBlendMap();
                dialog.add(new Image(Backgrounds.create(blendMap.getMap(), TerrainNode.TextureMap.WIDTH, TerrainNode.TextureMap.HEIGHT)));
                dialog.pack();
                dialog.show(getStage());
                super.clicked(event, x, y);
            }
        });


        preview.pack();


        add(preview);
    }

    public void updateBlendImage(Drawable texture) {
        blendImage.setDrawable(texture);
    }

    private ImageButton createTextureSlot(final int channelID) {

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = Backgrounds.create(Color.DARK_GRAY, Color.WHITE, 64, 64);
        style.imageChecked = Backgrounds.create(Color.DARK_GRAY, Color.RED, 64, 64);

        ImageButton i = new ImageButton(style);
        textureSlotStyles[channelID] = style;
        textureSlots[channelID] = i;

        i.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == 0) {
                    TextureTool.setSelectedTextureID(channelID);
                }
                if (button == 2) {

                    MaterialSelectorPreview materialDialog = new MaterialSelectorPreview();
                    materialDialog.setCallback(new MaterialSelectCallback() {
                        @Override
                        public void materialSelected(Material asset) {
                            WorldCell region = Editor.getCurrentCell();
                            if (region != null) {
                                TerrainNode terrain = region.getTerrain();
                                terrain.setMaterial(channelID, asset);
                                channelMaterialChanged(channelID);
                            }
                        }
                    });
                    materialDialog.show(getStage());
                }
                return super.touchDown(event, x, y, pointer, button);
            }
        });
        return textureSlots[channelID];
    }

   /* private VisTable getColorModeView() {
        VisTable table = new VisTable();
        table.add(new VisLabel("Color: ", Style.label)).row();
        table.add(selectedColorPrev = new Image(Backgrounds.create(Color.WHITE, 50, 25))).pad(5);
        table.add(new VisLabel("Click to pick new color"));
        selectedColorPrev.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished (Color newColor) {
                        selectedColorPrev.setDrawable(Backgrounds.create(newColor, 50, 25));
                        EditorBrushTool.setSelectedColor(newColor);
                    }
                });
                picker.setColor(EditorBrushTool.getSelectedColor());
                EditorBrushTool.ui.addActor(picker);
                super.clicked(event, x, y);
            }
        });
        return table;
    }*/

    private VisTable getTextureModeView() {
        VisTable table = new VisTable();

        VisTable textureTable = new VisTable();
        textureSlotGroup = new ButtonGroup();
        textureSlotGroup.setMaxCheckCount(1);
        textureTable.add(createTextureSlot(0));
        textureTable.add(createTextureSlot(1));
        textureTable.add(createTextureSlot(2));
        textureTable.add(createTextureSlot(3));
        textureSlotGroup.add(textureSlots);

        VisTable preview = new VisTable();
        preview.align(Align.top);
        preview.add(new VisLabel("Region Materials:", Style.label));
        preview.row();
        preview.add(textureTable).row();
        preview.pack();

        table.add(preview);
        table.row();
        table.add(textureTable);
        return table;
    }

    private void channelMaterialChanged(int id) {
        Drawable imageChecked;
        Drawable imageUp;

        WorldCell region = Editor.getCurrentCell();
        if(region == null) {
            return;
        }
        TerrainNode terrain = region.getTerrain();
        if(terrain == null || terrain.getMaterial(id) == null) {
            return;
        }

        if (terrain.getMaterial(id).getDiffuseTexture() != null) {
            Texture t = terrain.getMaterial(id).resolveDiffuse();
            imageChecked = Backgrounds.create(t, Color.RED, 64, 64);
            imageUp = Backgrounds.create(t, Color.WHITE, 64, 64);
        } else {
            imageUp = Backgrounds.create(Color.DARK_GRAY, Color.WHITE, 64, 64);
            imageChecked = Backgrounds.create(Color.DARK_GRAY, Color.RED, 64, 64);
        }

        textureSlotStyles[id].imageChecked = imageChecked;
        textureSlotStyles[id].imageUp = imageUp;

    }

    public void regionUpdated() {
        WorldCell region = Editor.getCurrentCell();
        if(region == null) {
            return;
        }

        channelMaterialChanged(0);
        channelMaterialChanged(1);
        channelMaterialChanged(2);
        channelMaterialChanged(3);

        //this.blendMapPreview.setDrawable(Backgrounds.create(region.getBlendMap().getBlendMapPixmap(), Color.CYAN, 256, 256));
    }
}
