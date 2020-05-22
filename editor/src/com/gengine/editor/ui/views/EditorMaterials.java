package com.gengine.editor.ui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.assets.MaterialLibrary;
import com.gengine.core.model.Material;
import com.gengine.editor.Editor;
import com.gengine.editor.ui.widgets.SplitPane;
import com.gengine.util.Backgrounds;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.SingleFileChooserListener;

import java.util.Map;

public class EditorMaterials extends VisTable {

    private final VisTextButton createMaterialButton;
    private final VisSlider shininessSlider;
    private final VisSlider opacitySlider;
    private final VisTextButton saveButton;
    private final VisTable materialTable;
    private final SimpleListAdapter<Material> simpleListAdapter;
    private final VisTextButton selectMaterialButton;

    private ListView<Material> materialList;

    private Material selectedMaterial;
    private VisTextField materialNameField;
    private VisImageButton diffuseTexturePreview;
    private VisImageButton normalTextureButton;

    private Drawable selectedDiffuseTexturePreview;
    private Drawable selectedNormalTexturePreview;

    private float currentShininess;
    private float currentOpacity;
    private String currentName;
    private String selectedTextureFile;
    private String selectedNormalFile;
    private MaterialSelectCallback callback;

    public void setCallback(MaterialSelectCallback selectCallback) {
        this.callback = selectCallback;
    }

    public EditorMaterials() {
        simpleListAdapter = new SimpleListAdapter<>(new Array<Material>());
        simpleListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        VisTable materialLibrary = new VisTable();
        materialList = new ListView<Material>(simpleListAdapter);
        materialList.setItemClickListener(new ListView.ItemClickListener<Material>() {
            @Override
            public void clicked(Material item) {
                setMaterial(item);
            }
        });
        createMaterialButton = new VisTextButton("Create new material");
        createMaterialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Material material = new Material();
                material.setName("New Material.mtl");

                MaterialLibrary.getMaterials().put(material.getName(), material);
                MaterialLibrary.save(material);
                updateMaterialList();
                setMaterial(material);
                super.clicked(event, x, y);
            }
        });
        selectMaterialButton = new VisTextButton("Select");
        selectMaterialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(simpleListAdapter.getSelection().size == 0) {
                    return;
                }
                callback.materialSelected(simpleListAdapter.getSelection().get(0));
                super.clicked(event, x, y);
            }
        });
        materialLibrary.add(createMaterialButton).row();
        materialLibrary.add(materialList.getMainTable()).size(225, 350).row();
        materialLibrary.add(selectMaterialButton).row();
        //add(materialList.getMainTable()).grow().size(350, 450).row();


        materialTable = new VisTable();
        materialTable.add(new VisLabel("Edit Material"));
        materialTable.row();
        materialTable.add(new VisLabel("Name: "));
        materialTable.add(materialNameField = new VisTextField(""));
        materialTable.row().padBottom(5);

        diffuseTexturePreview = new VisImageButton(Backgrounds.create(Color.WHITE, 64, 64));
        diffuseTexturePreview.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTextureChooser(false);
                super.clicked(event, x, y);
            }
        });

        materialTable.add(new VisLabel("Texture"));
        materialTable.add(diffuseTexturePreview);
        materialTable.row().padBottom(5);

        normalTextureButton = new VisImageButton(Backgrounds.create(Color.WHITE, 64, 64));
        normalTextureButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showTextureChooser(true);
                super.clicked(event, x, y);
            }
        });

        materialTable.add(new VisLabel("Normal texture"));
        materialTable.add(normalTextureButton);
        materialTable.row().padBottom(5);

        materialTable.add(new VisLabel("Shininess"));
        materialTable.add(shininessSlider = new VisSlider(0.0f, 1.0f, 0.01f, false));
        materialTable.row().padBottom(5);

        materialTable.add(new VisLabel("Opacity"));
        materialTable.add(opacitySlider = new VisSlider(0.0f, 1.0f, 0.01f, false));
        materialTable.row().padBottom(5);

        materialTable.add(saveButton = new VisTextButton("Save changes"));
        materialTable.row();

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
                if(MaterialLibrary.getMaterials().containsKey(currentName) && !selectedMaterial.getName().equalsIgnoreCase(currentName)) {
                    return;
                }
                if(!currentName.endsWith(".mtl")) {
                    return;
                }
                selectedMaterial.setName(currentName);
                selectedMaterial.setOpacity(currentShininess);
                selectedMaterial.setOpacity(currentOpacity);

                if(selectedTextureFile != null)
                    selectedMaterial.setDiffuse(selectedTextureFile);
                if(selectedNormalFile != null)
                    selectedMaterial.setNormal(selectedNormalFile);

                MaterialLibrary.save(selectedMaterial);
                updateMaterialList();
                super.clicked(event, x, y);
            }
        });


        SplitPane splitPane = new SplitPane(materialLibrary, materialTable, false);
        splitPane.setSplitAmount(0.50f);
        splitPane.setMinSplitAmount(0.50f);
        splitPane.setMaxSplitAmount(0.51f);
        add(splitPane).size(550, 450);
        setMaterial(null);
    }

    public void updateMaterialList() {
        simpleListAdapter.clear();
        for (Map.Entry<String, Material> entry : MaterialLibrary.getMaterials().entrySet()) {
            Material asset = entry.getValue();
            materialList.getAdapter().add(asset);
        }
        materialList.rebuildView();
    }


    private void setMaterial(Material asset) {
        selectedMaterial = asset;
        if (selectedMaterial == null) {
            materialTable.setVisible(false);
            return;
        }
        materialTable.setVisible(true);
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

                if (!MaterialLibrary.importTexture(file)) {
                    System.out.println("Could not load texture");
                    return;
                }
                System.out.println("Loaded " + file);
                if (normal)
                    setNormalTexturePreview(file.name());
                else
                    setDiffuseTexturePreview(file.name());

            }
        });
        Editor.ui.addActor(fileChooser.fadeIn());
    }

    private void setDiffuseTexturePreview(String f) {
        selectedTextureFile = f;
        if (selectedTextureFile != null) {
            selectedDiffuseTexturePreview = Backgrounds.create(new Pixmap(Gdx.files.internal(f)), 64, 64);
            diffuseTexturePreview.getStyle().imageUp = selectedDiffuseTexturePreview;
            diffuseTexturePreview.getStyle().imageDown = selectedDiffuseTexturePreview;
            diffuseTexturePreview.getStyle().imageChecked = selectedDiffuseTexturePreview;

            diffuseTexturePreview.invalidate();
        } else {
            diffuseTexturePreview.getStyle().imageUp = Backgrounds.create(Color.BLACK, 64, 64);
            diffuseTexturePreview.getStyle().imageDown = Backgrounds.create(Color.BLACK, 64, 64);
            diffuseTexturePreview.getStyle().imageChecked = Backgrounds.create(Color.BLACK, 64, 64);
        }
    }

    private void setNormalTexturePreview(String handle) {
        selectedNormalFile = handle;
        if (selectedNormalFile != null) {
            selectedNormalTexturePreview = Backgrounds.create(new Pixmap(Gdx.files.internal(handle)), 64, 64);

            normalTextureButton.getStyle().imageUp = selectedNormalTexturePreview;
            normalTextureButton.getStyle().imageDown = selectedNormalTexturePreview;
            normalTextureButton.getStyle().imageChecked = selectedNormalTexturePreview;

            normalTextureButton.invalidate();
        } else {
            normalTextureButton.getStyle().imageUp = Backgrounds.create(Color.BLACK, 64, 64);
            normalTextureButton.getStyle().imageDown = Backgrounds.create(Color.BLACK, 64, 64);
            normalTextureButton.getStyle().imageChecked = Backgrounds.create(Color.BLACK, 64, 64);
        }
    }
}
