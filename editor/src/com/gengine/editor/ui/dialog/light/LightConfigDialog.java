package com.gengine.editor.ui.dialog.light;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.gengine.core.cell.node.LightNode;
import com.gengine.editor.Editor;
import com.gengine.editor.Style;
import com.gengine.editor.util.Backgrounds;
import com.gengine.editor.library.packer.CellNodePacker;
import com.gengine.editor.util.FileIO;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

public class LightConfigDialog extends VisDialog {

    private final VisTextField lightName;
    private final Image diffuseColorPreview;
    private final Image specularColorPreview;
    private final VisTextButton saveButton;
    private final VisSelectBox<LightNode.LightType> lightTypeSelector;

    private LightNode light;

    private String name;
    private Vector3 diffuseColor = new Vector3(), specularColor = new Vector3();
    private LightNode.LightType lightType;

    public LightConfigDialog() {
        super("Create light node");
        addCloseButton();

        VisTable content = new VisTable();
        content.add(new VisLabel("Name: "));
        content.add(lightName = new VisTextField(""));
        content.row().padBottom(5);

        content.add(new VisLabel("Diffuse Color: ", Style.label));
        content.add(diffuseColorPreview = new Image(Backgrounds.create(new Color(diffuseColor.x, diffuseColor.y, diffuseColor.z, 1.0f), 50, 25))).pad(5);
        diffuseColorPreview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished (Color newColor) {
                        diffuseColor.set(newColor.r, newColor.g, newColor.b);
                        diffuseColorPreview.setDrawable(Backgrounds.create(new Color(diffuseColor.x, diffuseColor.y, diffuseColor.z, 1), 50, 25));
                    }
                });
                Editor.ui.addActor(picker);
            }
        });
        content.row();

        lightName.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                name = ((VisTextField) actor).getText();
            }
        });

        content.add(new VisLabel("Specular Color: ", Style.label));
        content.add(specularColorPreview = new Image(Backgrounds.create(new Color(specularColor.x, specularColor.y, specularColor.z, 1.0f), 50, 25))).pad(5);
        specularColorPreview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished (Color newColor) {
                        specularColor.set(newColor.r, newColor.g, newColor.b);
                        specularColorPreview.setDrawable(Backgrounds.create(new Color(specularColor.x, specularColor.y, specularColor.z, 1), 50, 25));
                    }
                });
                Editor.ui.addActor(picker);
            }
        });
        content.row();

        content.add(new VisLabel("Light type"));

        lightTypeSelector = new VisSelectBox<>();
        lightTypeSelector.setItems(LightNode.LightType.POINT, LightNode.LightType.SPOTLIGHT);
        lightTypeSelector.setAlignment(Align.center);
        lightTypeSelector.setSelected(LightNode.LightType.POINT);
        lightTypeSelector.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                lightType = lightTypeSelector.getSelected();
            }
        });
        content.add(lightTypeSelector).row();
        content.addSeparator().fillX().growX();

        content.add(saveButton = new VisTextButton("Submit"));
        content.row();
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(!light.getIdentifier().equals(name) && light.fileHandle != null) {
                    FileIO.renameFile(light.fileHandle, name + "." + light.getExtension());
                    System.out.println("Renaming " + light.getIdentifier() + " -> " + name);
                }

                light.setIdentifier(name);
                light.diffuse.set(diffuseColor);
                light.specular.set(specularColor);
                light.type = lightType;

                CellNodePacker.pack(light);
                Editor.ui.getLibrary().refreshLibrary();

                hide();
                super.clicked(event, x, y);
            }
        });
        add(content);
    }

    /**
     * Creates new light dialog to be edited
     * @param lightNode
     */
    public static void create(final LightNode lightNode) {
        LightConfigDialog lightDialog = new LightConfigDialog();
        lightDialog.setLight(lightNode);
        lightDialog.updateFields();
        lightDialog.show(Editor.ui);
    }

    /**
     * Sets local temporary variables used for tracking changes.
     * @param newLight
     */
    private void setLight(LightNode newLight) {
        this.light = newLight;
        if(this.light == null) {
            this.light = new LightNode();
        }

        //Initialize temporary variables that we'll use to track changes.
        this.name = this.light.getIdentifier();
        this.lightType = this.light.type;
        this.diffuseColor.set(light.diffuse);
        this.specularColor.set(light.specular);
    }

    /**
     * Updates the ui fields in this dialog
     */
    private void updateFields() {
        this.specularColorPreview.setDrawable(Backgrounds.create(new Color(this.specularColor.x, this.specularColor.y, this.specularColor.z, 1), 50, 25));
        this.diffuseColorPreview.setDrawable(Backgrounds.create(new Color(this.diffuseColor.x, this.diffuseColor.y, this.diffuseColor.z, 1), 50, 25));

        this.lightName.setText(this.light.getIdentifier());
        this.lightTypeSelector.setSelected(this.lightType);
    }


}