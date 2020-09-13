package com.gengine.editor.ui.hierarchy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.cell.node.LightNode;
import com.gengine.editor.library.packer.CellNodePacker;
import com.gengine.core.cell.WorldCell;
import com.gengine.editor.Editor;
import com.gengine.editor.tool.EditMode;
import com.gengine.editor.tool.SelectionTool;
import com.gengine.core.cell.node.CellNode;
import com.gengine.editor.ui.dialog.light.LightConfigDialog;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.*;

public class CellHierarchyTree extends VisTable {

    private CellNode lastSelected;

    private VisTree cellNodeTree;
    private ScrollPane cellScrollPane;
    private SimpleListAdapter<CellNode> cellHierarchListAdapter;
    private CellNodeMenu cellTreeMenu;
    private NodePopupMenu selectionOptionMenu;

    class CellNodeMenu extends PopupMenu {

        private CellNode selection;

        public CellNodeMenu() {

            MenuItem saveToLibrary = new MenuItem("Save to library");
            addItem(saveToLibrary);

            MenuItem rename = new MenuItem("Rename");
            addItem(rename);

            MenuItem delete = new MenuItem("Delete");
            addItem(delete);

            saveToLibrary.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    CellNodePacker.pack(selection);
                    refreshNodeTree();
                }
            });

            rename.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    final VisTextField textField = new VisTextField();
                    final VisTextButton submit = new VisTextButton("Submit");
                    textField.setText(selection.getIdentifier());

                    VisDialog dialog = new VisDialog("Rename " + selection.getIdentifier());
                    dialog.addCloseButton();
                    dialog.add(textField).row();
                    dialog.add(submit);
                    dialog.show(Editor.ui);

                    submit.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            selection.setIdentifier(textField.getText());
                            refreshNodeTree();
                        }
                    });
                }
            });

            delete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selection.remove();
                    /*if (selection.fileHandle.exists())
                        selection.fileHandle.delete();*/
                    refreshNodeTree();
                }
            });
        }

        public void setSelection(CellNode selection) {
            this.selection = selection;
        }
    }

    private final MenuItem[] cellNodeOperations = {
            new MenuItem("Save to Library"),
    };

    private MenuItem[] lightOptions = new MenuItem[]{
            new MenuItem("Edit Light")
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
    private MenuItem[][] getMenuItems(CellNode node) {
        if (node.getClass() == LightNode.class) {
            return new MenuItem[][]{lightOptions, cellNodeOperations, fileOperations, clipboardOperations};
        }
        return new MenuItem[][]{cellNodeOperations, fileOperations, clipboardOperations};
    }

    public CellHierarchyTree() {
        setupCellHierarchy();
        setupDragAndDrop();
        setupOptionMenuListeners();
    }

    private void setupOptionMenuListeners() {
        //Edit light
        lightOptions[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                LightConfigDialog.create((LightNode) lastSelected);
            }
        });

        cellNodeOperations[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CellNodePacker.pack(lastSelected);
                Editor.ui.getLibrary().refreshLibrary();
            }
        });



        /* File operations */

        //Rename
        fileOperations[0].addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                final VisTextField textField = new VisTextField();
                final VisTextButton submit = new VisTextButton("Submit");
                textField.setText(lastSelected.getIdentifier());

                VisDialog dialog = new VisDialog("Rename " + lastSelected.getIdentifier());
                dialog.addCloseButton();
                dialog.add(textField).row();
                dialog.add(submit);
                dialog.show(Editor.ui);

                submit.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        lastSelected.setIdentifier(textField.getText());
                        refreshNodeTree();
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
                lastSelected.remove();

                /*
                * oops not supposed to delete the file
                *  if (selection.fileHandle.exists())
                *    selection.fileHandle.delete();
                * */
                refreshNodeTree();
            }
        });

    }

    private void setupCellHierarchy() {

        this.cellNodeTree = new VisTree();
        this.cellTreeMenu = new CellNodeMenu();

        this.selectionOptionMenu = new NodePopupMenu();

        cellNodeTree.getSelection().setProgrammaticChangeEvents(false);

        cellScrollPane = new VisScrollPane(cellNodeTree);
        cellScrollPane.setFlickScroll(false);
        cellScrollPane.setFadeScrollBars(false);

        add(new VisLabel("Cell hierarchy")).expandX().fillX().pad(3f).row();
        add(cellScrollPane).fill().expand();

        cellHierarchListAdapter = new SimpleListAdapter<>(new Array<>());
        cellHierarchListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);
        cellNodeTree.addListener(new InputListener() {
            long lastClick = 0;

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                CellHierarchyNode treeCellNode = (CellHierarchyNode) cellNodeTree.getNodeAt(y);
                if (treeCellNode == null) {
                    return;
                }
                CellNode node = treeCellNode.getValue();
                lastSelected = node;
                if (event.getButton() == 1) {
                    MenuItem[][] menuItems = getMenuItems(node);
                    selectionOptionMenu.setMenuItems(menuItems);
                    selectionOptionMenu.showMenu(getStage(), Gdx.input.getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()));
                } else if (event.getButton() == 0) {

                    System.out.println(System.currentTimeMillis() - lastClick);
                    if (System.currentTimeMillis() - lastClick <= 500) {

                        //Editor.smoothTranslate(node.position);
                    }
                    lastClick = System.currentTimeMillis();
                    Editor.setEditMode(EditMode.OBJECT);
                    SelectionTool tool = (SelectionTool) EditMode.OBJECT.getTool();
                    tool.setSelection(node);
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });
    }


    private DragAndDrop dragAndDrop = new DragAndDrop();

    private void setupDragAndDrop() {
        dragAndDrop.addSource(new DragAndDrop.Source(cellNodeTree) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                Tree.Node node = cellNodeTree.getNodeAt(y);
                if (node != null) {
                    payload.setObject(node);
                    return payload;
                }
                return null;
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(cellNodeTree) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Tree.Node node = cellNodeTree.getNodeAt(y);
                if (node == null && cellNodeTree.getSelectedNode() == null) {
                    return false;
                }
                if (node != null && !cellNodeTree.getSelection().contains(node)) {
                    cellNodeTree.getSelection().set(node);
                }
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                CellHierarchyNode dragged = (CellHierarchyNode) payload.getObject();
                CellHierarchyNode droppedOn = (CellHierarchyNode) cellNodeTree.getNodeAt(y);
                if (droppedOn != null) {
                    if (droppedOn.getValue().isParentOf(dragged.getValue())) {
                        return;
                    }
                }
                dragged.getValue().remove(); // Remove from old parent
                droppedOn.getValue().addChild(dragged.getValue()); //Add to dragged parent
                refreshNodeTree();
            }
        });
    }


    public CellNode getSelectedNode() {
        CellHierarchyNode impl = (CellHierarchyNode) cellNodeTree.getSelectedNode();
        if (cellNodeTree.getSelectedNode() == null)
            return null;
        return impl.getValue();
    }

    public void setSelectedNode(CellNode selectedNode) {
        cellNodeTree.getSelection().clear();
        CellHierarchyNode node = (CellHierarchyNode) cellNodeTree.findNode(selectedNode);
        cellNodeTree.getSelection().set(node);
    }

    public void refreshNodeTree() {
        Array array = new Array();
        cellNodeTree.findExpandedValues(array);
        cellNodeTree.clearChildren();
        for (WorldCell cell : Editor.world().loadedCells.values()) {
            populateCellTree(null, cell);
        }
        cellNodeTree.restoreExpandedValues(array);
    }

    private void populateCellTree(CellHierarchyNode parent, CellNode child) {
        //Add node to node tree
        CellHierarchyNode childParent = new CellHierarchyNode(child);
        if (parent == null) {
            cellNodeTree.add(childParent);
        } else {
            parent.add(childParent);
        }
        for (CellNode childChild : child.children()) {
            populateCellTree(childParent, childChild);
        }
    }

}
