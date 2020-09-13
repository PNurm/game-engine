package com.gengine.editor.ui.dialog.material;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.gengine.core.model.Material;
import com.gengine.editor.util.Backgrounds;
import com.gengine.editor.Editor;
import com.gengine.editor.library.packer.MaterialPacker;
import com.gengine.editor.util.FileIO;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.SingleFileChooserListener;

public class MaterialConfigDialog extends VisDialog {

    private final VisSlider shininessSlider;
    private final VisSlider opacitySlider;

    public VisTable getEditorTable() {
        return editorTable;
    }

    private final VisTable editorTable;

    public Material selectedMaterial;
    private final VisTextField materialNameField;
    private final VisImageButton diffuseTexturePreview;
    private final VisImageButton normalTextureButton;

    private float currentShininess;
    private float currentOpacity;
    private String currentName;
    private String selectedTextureFile;
    private String selectedNormalFile;

    /**
     *                 FileHandle directory = Editor.ui.getLibrary().getCurrentDir();
     *
     *                 if(directory == null) {
     *                     VisDialog visDialog = new VisDialog("Error");
     *                     visDialog.text("Directory not selected in library tree, please select a directory");
     *                     visDialog.show(getStage());
     *                     return;
     *                 }
     *
     *                 Material material = new Material();
     *                 String resolvedName = MaterialManager.avoidConflictingNames(material.getName(), directory);
     *                 material.setName(resolvedName);
     *
     *                 MaterialManager.save(directory, material);
     *                 setMaterial(material);
     */

    public MaterialConfigDialog() {
        super("Materials");
        addCloseButton();


        editorTable = new VisTable();
        editorTable.add(new VisLabel("Name: "));
        editorTable.add(materialNameField = new VisTextField("")).padBottom(8);
        editorTable.row();

        diffuseTexturePreview = new VisImageButton(Backgrounds.create(Color.WHITE, 128, 128));
        diffuseTexturePreview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTextureChooser(false);
                super.clicked(event, x, y);
            }
        });

        editorTable.add(new VisLabel("Texture"));
        editorTable.add(diffuseTexturePreview);
        editorTable.row().padBottom(5);

        normalTextureButton = new VisImageButton(Backgrounds.create(Color.WHITE, 128, 128));
        normalTextureButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTextureChooser(true);
                super.clicked(event, x, y);
            }
        });

        editorTable.add(new VisLabel("Normal texture"));
        editorTable.add(normalTextureButton);
        editorTable.row().padBottom(8);

        editorTable.add(new VisLabel("Shininess"));
        editorTable.add(shininessSlider = new VisSlider(0.0f, 1.0f, 0.01f, false));
        editorTable.row().padBottom(8);

        editorTable.add(new VisLabel("Opacity"));
        editorTable.add(opacitySlider = new VisSlider(0.0f, 1.0f, 0.01f, false));
        editorTable.row().padBottom(8);

        VisTextButton saveButton;
        editorTable.add(saveButton = new VisTextButton("Save changes"));
        editorTable.row();

        opacitySlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentOpacity = ((VisSlider) actor).getValue();
            }
        });
        shininessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentShininess = ((VisSlider) actor).getValue();
            }
        });
        materialNameField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                currentName = ((VisTextField) actor).getText();

            }
        });

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                selectedMaterial.setOpacity(currentShininess);
                selectedMaterial.setOpacity(currentOpacity);

                if(selectedTextureFile != null)
                    selectedMaterial.setDiffuse(selectedTextureFile);
                if(selectedNormalFile != null)
                    selectedMaterial.setNormal(selectedNormalFile);

                if(selectedMaterial.fileHandle == null) {
                    selectedMaterial.setName(currentName);
                    FileHandle fileHandle = Gdx.files.local("assets/library/materials/");
                    MaterialPacker.save(fileHandle, selectedMaterial);
                } else {
                    if(!currentName.equals(selectedMaterial.getName())) {
                        FileIO.renameFile(selectedMaterial.fileHandle, currentName);
                        selectedMaterial.setName(currentName);
                        System.out.println("Renamed material " + selectedMaterial.name);
                        Editor.ui.getLibrary().refreshLibrary();
                    }
                    MaterialPacker.pack(selectedMaterial);
                }

                Editor.ui.getLibrary().refreshLibrary();
                super.clicked(event, x, y);
                close();
            }
        });

        add(editorTable);
    }


    public void setMaterial(Material asset) {
        selectedMaterial = asset;
        if(selectedMaterial == null) {
            selectedMaterial = new Material();
        }
        initMaterialViewToSelected();
    }

    private void initMaterialViewToSelected() {
        currentName = selectedMaterial.getName();
        currentOpacity = selectedMaterial.getOpacity();
        currentShininess = selectedMaterial.getShininess();

        materialNameField.setText(currentName);
        shininessSlider.setValue(currentShininess);
        opacitySlider.setValue(currentOpacity);

        setDiffuseTexturePreview(selectedMaterial.getDiffuseTexture());
        setNormalTexturePreview(selectedMaterial.getNormalTexture());
    }


    private void showTextureChooser(final boolean normal) {
        FileChooser fileChooser = Editor.ui.getFileChooser();
        fileChooser.setSelectionMode(FileChooser.SelectionMode.FILES);
        fileChooser.setListener(new SingleFileChooserListener() {
            public void selected(FileHandle file) {
                FileHandle assetDir = Gdx.files.local("assets/library/textures");
                if(!file.path().startsWith(assetDir.path())) {
                    MaterialPacker.importTexture(file);
                }
                if (normal)
                    setNormalTexturePreview(file.name());
                else
                    setDiffuseTexturePreview(file.name());

            }
        });
        Editor.ui.addActor(fileChooser.fadeIn());
    }

    private void setDiffuseTexturePreview(String textureFilename) {
        selectedTextureFile = textureFilename;
        if (selectedTextureFile != null) {
            FileHandle textureFile = Gdx.files.local("assets/library/textures/" + textureFilename);
            if(!textureFile.exists()) {
                System.out.println("Texture file " + textureFilename + " not found at " + textureFile.path());
                return;
            }
            Drawable selectedDiffuseTexturePreview = Backgrounds.create(new Pixmap(textureFile), 128, 128);
            diffuseTexturePreview.getStyle().imageUp = selectedDiffuseTexturePreview;
            diffuseTexturePreview.getStyle().imageDown = selectedDiffuseTexturePreview;
            diffuseTexturePreview.getStyle().imageChecked = selectedDiffuseTexturePreview;

            diffuseTexturePreview.invalidate();
        } else {
            diffuseTexturePreview.getStyle().imageUp = Backgrounds.create(Color.BLACK, 128, 128);
            diffuseTexturePreview.getStyle().imageDown = Backgrounds.create(Color.BLACK, 128, 128);
            diffuseTexturePreview.getStyle().imageChecked = Backgrounds.create(Color.BLACK, 128, 128);
        }
    }

    private void setNormalTexturePreview(String handle) {
        selectedNormalFile = handle;
        if (selectedNormalFile != null) {
            Drawable selectedNormalTexturePreview = Backgrounds.create(new Pixmap(Gdx.files.local(handle)), 128, 128);

            normalTextureButton.getStyle().imageUp = selectedNormalTexturePreview;
            normalTextureButton.getStyle().imageDown = selectedNormalTexturePreview;
            normalTextureButton.getStyle().imageChecked = selectedNormalTexturePreview;

            normalTextureButton.invalidate();
        } else {
            normalTextureButton.getStyle().imageUp = Backgrounds.create(Color.BLACK, 128, 128);
            normalTextureButton.getStyle().imageDown = Backgrounds.create(Color.BLACK, 128, 128);
            normalTextureButton.getStyle().imageChecked = Backgrounds.create(Color.BLACK, 128, 128);
        }
    }
}

