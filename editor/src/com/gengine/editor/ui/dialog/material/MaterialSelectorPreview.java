package com.gengine.editor.ui.dialog.material;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.library.Library;
import com.gengine.core.library.ex.AssetProviderNotFoundException;
import com.gengine.core.model.Material;
import com.gengine.editor.Editor;
import com.gengine.editor.ui.widgets.SplitPane;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;

public class MaterialSelectorPreview extends VisDialog {

    /**
     * FileHandle directory = Editor.ui.getLibrary().getCurrentDir();
     * <p>
     * if(directory == null) {
     * VisDialog visDialog = new VisDialog("Error");
     * visDialog.text("Directory not selected in library tree, please select a directory");
     * visDialog.show(getStage());
     * return;
     * }
     * <p>
     * Material material = new Material();
     * String resolvedName = MaterialManager.avoidConflictingNames(material.getName(), directory);
     * material.setName(resolvedName);
     * <p>
     * MaterialManager.save(directory, material);
     * setMaterial(material);
     */


    private final SimpleListAdapter<MaterialEntry> materialListAdapter;
    private final ListView<MaterialEntry> materialList;
    private final VisTextButton selectMaterialButton;
    private final MaterialConfigDialog materialEditor;
    private MaterialSelectCallback callback;

    public MaterialSelectorPreview() {
        super("Select a material");
        addCloseButton();

        materialEditor = new MaterialConfigDialog();

        materialListAdapter = new SimpleListAdapter<>(new Array<MaterialEntry>());
        materialListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        VisTable materialSelector = new VisTable();
        materialList = new ListView<MaterialEntry>(materialListAdapter);
        materialList.setItemClickListener(new ListView.ItemClickListener<MaterialEntry>() {
            @Override
            public void clicked(MaterialEntry entry) {
                try {
                    if (materialEditor.selectedMaterial != null) {
                        Library.freeAsset(materialEditor.selectedMaterial.fileHandle.path());
                    }
                    Material material = (Material) Library.provideAsset(Material.class, entry.file);
                    materialEditor.setMaterial(material);
                } catch (AssetProviderNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        selectMaterialButton = new VisTextButton("Select");
        selectMaterialButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (materialListAdapter.getSelection().size == 0) {
                    return;
                }
                MaterialEntry entry = materialListAdapter.getSelection().get(0);
                try {
                    callback.materialSelected((Material) Library.provideAsset(Material.class, entry.file));
                } catch (AssetProviderNotFoundException e) {
                    e.printStackTrace();
                }
                close();
            }
        });
        //materialLibrary.add(createMaterialButton).row();
        materialSelector.add(materialList.getMainTable()).size(225, 350).row();
        materialSelector.add(selectMaterialButton).row();
        //add(materialList.getMainTable()).grow().size(350, 450).row();

        SplitPane splitPane = new SplitPane(materialSelector, materialEditor.getEditorTable(), false);
        splitPane.setSplitAmount(0.50f);
        splitPane.setMinSplitAmount(0.50f);
        splitPane.setMaxSplitAmount(0.51f);
        add(splitPane).size(550, 450);

        refreshList();
    }

    public void refreshList() {
        FileHandle fileHandle = Gdx.files.local("assets/library/");
        populateMaterialList(fileHandle);
    }

    private void populateMaterialList(FileHandle handle) {
        for (FileHandle file : handle.list()) {
            if (file.isDirectory()) {
                populateMaterialList(file);
            }
            if (file.extension().equals("mtl")) {
                materialListAdapter.add(new MaterialEntry(file.name(), file));
            }
        }
    }

    public void setCallback(MaterialSelectCallback materialSelectCallback) {
        this.callback = materialSelectCallback;
    }

    static class MaterialEntry {
        String name;
        FileHandle file;

        public MaterialEntry(String name, FileHandle file) {
            this.name = name;
            this.file = file;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
