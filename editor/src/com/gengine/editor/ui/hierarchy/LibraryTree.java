package com.gengine.editor.ui.hierarchy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.library.Library;
import com.gengine.core.library.ex.AssetProviderNotFoundException;
import com.gengine.core.model.Material;
import com.gengine.editor.Editor;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.cell.node.LightNode;
import com.gengine.editor.ui.dialog.ErrorDialog;
import com.gengine.editor.ui.dialog.light.LightConfigDialog;
import com.gengine.editor.ui.dialog.material.MaterialConfigDialog;
import com.gengine.editor.util.FileIO;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.*;

public class LibraryTree extends VisTable {

    public LibraryTree() {
        setupNodeLibrary();
    }

    private VisTree libraryTree;
    private SimpleListAdapter<LibraryTreeEntry> libraryListAdapter;
    private NodePopupMenu selectionOptionMenu;
    private LibraryTreeEntry lastSelected;

    private MenuItem createLight;
    private MenuItem importModel;
    private MenuItem createMaterial;

    private final MenuItem[] cellNodeOperations = {
            new MenuItem("Add to cell"),
    };

    private MenuItem[] lightOptions = new MenuItem[]{
            new MenuItem("Edit Light")
    };

    private final MenuItem[] materialOptions = {
            new MenuItem("Edit Material"),
    };

    private final MenuItem[] fileOperations = {
            new MenuItem("Rename"),
            new MenuItem("Delete"),
    };

    private final MenuItem[] clipboardOperations = {
            new MenuItem("Copy"),
            new MenuItem("Paste"),
    };

    /**
     * Returns multidimensional array containing
     * sets of menu items based on selected entry
     *
     * @param node
     * @return
     */
    private MenuItem[][] getMenuItems(LibraryTreeEntry node) {
        if (node.type == Material.class) {
            return new MenuItem[][]{materialOptions, fileOperations};
        }
        if (node.type == LightNode.class) {
            return new MenuItem[][]{lightOptions, cellNodeOperations, fileOperations};
        }
        if (node.type == CellNode.class) {
            return new MenuItem[][]{cellNodeOperations, fileOperations};
        }
        System.out.println(node.type);

        return new MenuItem[][]{fileOperations};
    }


    private void setupNodeLibrary() {
        this.libraryTree = new VisTree();
        this.selectionOptionMenu = new NodePopupMenu();
        setupOptionMenuListeners();

        libraryTree.getSelection().setProgrammaticChangeEvents(false);

        VisScrollPane libraryScrollPane = new VisScrollPane(libraryTree);
        libraryScrollPane.setFlickScroll(false);
        libraryScrollPane.setFadeScrollBars(false);

        libraryListAdapter = new SimpleListAdapter<>(new Array<>());
        libraryListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        libraryTree.addListener(new InputListener() {

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                LibraryNodeTree libraryNode = (LibraryNodeTree) libraryTree.getNodeAt(y);
                if (libraryNode == null) {
                    return;
                }
                LibraryTreeEntry node = libraryNode.getValue();
                lastSelected = node;
                if (event.getButton() == 1) {
                    MenuItem[][] menuItems = getMenuItems(node);
                    selectionOptionMenu.setMenuItems(menuItems);
                    selectionOptionMenu.showMenu(getStage(), Gdx.input.getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        PopupMenu popupMenu = new PopupMenu();
        popupMenu.addItem(createLight = new MenuItem("Create light"));
        popupMenu.addItem(createMaterial = new MenuItem("Create material"));
        //popupMenu.addItem(importModel = new MenuItem("Import Model"));

        createLight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                LightConfigDialog.create(null);
            }
        });

        createMaterial.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MaterialConfigDialog materialConfigDialog = new MaterialConfigDialog();
                materialConfigDialog.setMaterial(null);
                materialConfigDialog.show(getStage());
            }
        });

        VisTextButton newButton = null;
        refreshLibrary();

        add(new VisLabel("Library")).expandX().fillX().pad(3f);
        add(newButton = new VisTextButton("Create new")).expandX().fillX().pad(3f).row();
        add(libraryScrollPane).colspan(2).fill().expand().row();

        VisTextButton finalNewButton = newButton;
        newButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popupMenu.showMenu(finalNewButton.getStage(), finalNewButton);
            }
        });

    }

    private void setupOptionMenuListeners() {
        //Edit material
        materialOptions[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MaterialConfigDialog materialDialog = new MaterialConfigDialog();
                Material material = null;
                try {
                    material = (Material) Library.provideAsset(Material.class, lastSelected.fileHandle);
                } catch (AssetProviderNotFoundException e) {
                    e.printStackTrace();
                }
                materialDialog.setMaterial(material);
                materialDialog.show(getStage());
            }
        });

        //Edit light
        lightOptions[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    LightNode cellNode = (LightNode) Library.provideAsset(CellNode.class, lastSelected.fileHandle.path());
                    if (cellNode == null) {
                        ErrorDialog.show("Unable to load cell node at " + lastSelected.fileHandle.path() + " of type " + lastSelected.type);
                        return;
                    }
                    cellNode.fileHandle = lastSelected.fileHandle;
                    LightConfigDialog.create(cellNode);
                } catch (AssetProviderNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        cellNodeOperations[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    CellNode cellNode = (CellNode) Library.provideAsset(CellNode.class, lastSelected.fileHandle.path());
                    if (cellNode == null) {
                        ErrorDialog.show("Unable to load cell node at " + lastSelected.fileHandle.path() + " of type " + lastSelected.type);
                        return;
                    }
                    Editor.getCurrentCell().addChild(cellNode);
                    Editor.ui.getCellTree().refreshNodeTree();
                } catch (AssetProviderNotFoundException e) {
                    ErrorDialog.show("Unable to load cell node at " + lastSelected.fileHandle.path() + " of type " + lastSelected.type);
                    e.printStackTrace();
                }
            }
        });



        /* File operations */

        //Rename
        fileOperations[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final VisTextField textField = new VisTextField();
                final VisTextButton submit = new VisTextButton("Submit");
                textField.setText(lastSelected.name);

                VisDialog dialog = new VisDialog("Rename " + lastSelected.name);
                dialog.addCloseButton();
                dialog.add(textField).row();
                dialog.add(submit);
                dialog.show(Editor.ui);

                submit.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        lastSelected.name = (textField.getText());

                        if (lastSelected.fileHandle != null) {
                            FileIO.renameFile(lastSelected.fileHandle, lastSelected.name);
                        }

                        refreshLibrary();
                    }
                });
            }
        });

        //Delete
        fileOperations[1].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (lastSelected.fileHandle != null) {
                    lastSelected.fileHandle.delete();
                }
                refreshLibrary();
            }
        });

    }


    public Array<LibraryTreeEntry> getSelection() {
        return libraryListAdapter.getSelection();
    }

    public FileHandle getCurrentDir() {
        LibraryTreeEntry entry = getSelection().get(0);
        if (entry == null) {
            return null;
        }
        return entry.fileHandle.isDirectory() ? entry.fileHandle : entry.fileHandle.parent();
    }

    public void refreshLibrary() {
        Array array = new Array();
        libraryTree.findExpandedValues(array);
        libraryTree.clearChildren();

        FileHandle fileHandle = Gdx.files.local("assets/library/");
        if (!fileHandle.exists()) {
            fileHandle.mkdirs();
        }
        populateLibraryTree(null, fileHandle);

        libraryTree.restoreExpandedValues(array);
    }


    private void populateLibraryTree(LibraryNodeTree parent, FileHandle child) {

        //Add node to node tree
        LibraryTreeEntry entry = new LibraryTreeEntry(child);
        LibraryNodeTree childParent = new LibraryNodeTree(entry);
        if (parent == null) {
            libraryTree.add(childParent);
        } else {
            parent.add(childParent);
        }

        for (FileHandle childChild : child.list()) {
            populateLibraryTree(childParent, childChild);
        }
    }

    private class LibraryNodeTree extends Tree.Node<Tree.Node, LibraryTreeEntry, VisLabel> {
        private final VisLabel nameLabel;

        public LibraryNodeTree(LibraryTreeEntry node) {
            super(new VisLabel());
            this.setValue(node);
            nameLabel = getActor();
            nameLabel.setText(node.name);
        }
    }

}
