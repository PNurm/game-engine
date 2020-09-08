package com.gengine.editor.ui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.gengine.core.assets.NodeLibrary;
import com.gengine.core.world.WorldCell;
import com.gengine.editor.Editor;
import com.gengine.editor.Style;
import com.gengine.editor.tool.EditMode;
import com.gengine.editor.tool.SelectionTool;
import com.gengine.util.Backgrounds;
import com.gengine.core.world.node.LightNode;
import com.gengine.core.world.node.CellNode;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;

import java.util.Map;

public class EditorCellHierarchyTree extends VisTable {

    private VisTree cellNodeTree;
    private ScrollPane cellScrollPane;
    private final VisTable rootContent;
    private SimpleListAdapter<CellNode> cellHierarchListAdapter;
    private VisTable cellHierarcyTable;
    private ListView<NodeLibraryEntry> libraryListView;
    private SimpleListAdapter<NodeLibraryEntry> libraryListAdapter;
    private VisTextButton addSelected;
    private MenuItem newLightNode;
    private MenuItem newModelNode;

    private CellNodeMenu cellTreeMenu;
    private VisTree libraryTree;
    private CellNodeMenu libraryTreeMenu;
    private VisScrollPane libraryScrollPane;
    private VisTable libraryTable;
    private MenuItem createLight;
    private MenuItem importModel;

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

                    NodeLibrary.addNode(selection);
                }
            });

            rename.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    final VisTextField textField = new VisTextField();
                    final VisTextButton submit = new VisTextButton("Submit");
                    textField.setText(selection.getName());

                    VisDialog dialog = new VisDialog("Rename " + selection.getName());
                    dialog.addCloseButton();
                    dialog.add(textField).row();
                    dialog.add(submit);
                    dialog.show(Editor.ui);

                    submit.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            selection.setName(textField.getText());
                            refreshNodeTree();
                        }
                    });
                }
            });

            delete.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    selection.remove();
                }
            });
        }

        public void setSelection(CellNode selection) {
            this.selection = selection;
        }
    }

    public EditorCellHierarchyTree() {

        rootContent = new VisTable();
        rootContent.setFillParent(true);
        rootContent.align(Align.left | Align.top);
        add(rootContent).fill().expand().row();

        setupCellHierarchy();
        setupNodeLibrary();

        rootContent.pad(5);
        rootContent.add(cellHierarcyTable).expand().fill().top().uniform().row();
        rootContent.add(libraryTable).expand().fill().bottom().uniform().row();
        rootContent.pack();


        debugAll();
        setupDragAndDrop();
    }

    private void setupCellHierarchy() {

        this.cellNodeTree = new VisTree();
        this.cellTreeMenu = new CellNodeMenu();
        cellNodeTree.getSelection().setProgrammaticChangeEvents(false);

        cellScrollPane = new VisScrollPane(cellNodeTree);
        cellScrollPane.setFlickScroll(false);
        cellScrollPane.setFadeScrollBars(false);

        cellHierarcyTable = new VisTable();
        cellHierarcyTable.add(new VisLabel("Cell hierarchy")).expandX().fillX().pad(3f).row();
        cellHierarcyTable.add(cellScrollPane).fill().expand();

        cellHierarchListAdapter = new SimpleListAdapter<>(new Array<>());
        cellHierarchListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);
        cellNodeTree.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                TreeCellNode treeCellNode = (TreeCellNode) cellNodeTree.getNodeAt(y);
                if(treeCellNode == null) {
                    return;
                }
                CellNode node = treeCellNode.getValue();
                if(event.getButton() == 1) {
                    cellTreeMenu.setSelection(node);
                    cellTreeMenu.showMenu(getStage(), Gdx.input.getX(), (Gdx.graphics.getHeight() - Gdx.input.getY()));
                } else if(event.getButton() == 0) {
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

    private void setupNodeLibrary() {
        this.libraryTree = new VisTree();
        this.libraryTreeMenu = new CellNodeMenu();
        libraryTree.getSelection().setProgrammaticChangeEvents(false);

        libraryScrollPane = new VisScrollPane(libraryTree);
        libraryScrollPane.setFlickScroll(false);
        libraryScrollPane.setFadeScrollBars(false);

        libraryListAdapter = new SimpleListAdapter<>(new Array<>());
        libraryListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        libraryListView = new ListView<>(libraryListAdapter);
        libraryListAdapter.add(new NodeLibraryEntry("Hello", null));
        libraryListView.setItemClickListener(item -> {

            if(item.cellNode instanceof LightNode) {
                EditLightDialog d = new EditLightDialog((LightNode) item.cellNode, new CreateLightCallback() {
                    @Override
                    void callback(LightNode light) {

                    }
                });
                d.show(getStage());
            }
        });

        PopupMenu popupMenu = new PopupMenu();
        popupMenu.addItem(createLight = new MenuItem("Create Light"));
        popupMenu.addItem(importModel = new MenuItem("Import Model"));
        createLight.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                CreateLightDialog createLightDialog  = new CreateLightDialog(
                        new CreateLightCallback() {
                            @Override
                            void callback(LightNode light) {
                                NodeLibrary.addNode(light);
                                refreshNodeLibrary();
                            }
                        });
                createLightDialog.show(getStage());
            }
        });
        VisTextButton newButton = null;

        libraryTable = new VisTable();
        libraryTable.add(new VisLabel("Node Library")).expandX().fillX().pad(3f);
        libraryTable.add(newButton = new VisTextButton("+ New node")).expandX().fillX().pad(3f).row();
        libraryTable.add(libraryScrollPane).colspan(2).fill().expand().row();
        libraryTable.add(addSelected = new VisTextButton("Add selected")).colspan(2);

        VisTextButton finalNewButton = newButton;
        newButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                popupMenu.showMenu(finalNewButton.getStage(), finalNewButton);
            }
        });

        addSelected.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(cellHierarchListAdapter.getSelection().size == 0) {
                    return;
                }
                CellNode node = cellHierarchListAdapter.getSelection().get(0);
                Editor.getCurrentCell().root.addChild(node);

                refreshNodeTree();
                super.clicked(event, x, y);
            }
        });

    }

    static class NodeLibraryEntry {
        String name;
        CellNode cellNode;

        public NodeLibraryEntry(String name, CellNode value)  {
            this.name = name;
            this.cellNode = value;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private DragAndDrop dragAndDrop = new DragAndDrop();

    private void setupDragAndDrop() {
        dragAndDrop.addSource(new DragAndDrop.Source(cellNodeTree) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                Tree.Node node = cellNodeTree.getNodeAt(y);
                if(node != null) {
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
                if(node == null && cellNodeTree.getSelectedNode() == null) {
                    return false;
                }
                if(node != null && !cellNodeTree.getSelection().contains(node)) {
                    cellNodeTree.getSelection().set(node);
                }
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                TreeCellNode dragged = (TreeCellNode) payload.getObject();
                TreeCellNode droppedOn = (TreeCellNode) cellNodeTree.getNodeAt(y);
                if(droppedOn != null) {
                    if(droppedOn.getValue().isParentOf(dragged.getValue())) {
                        return;
                    }
                }
                dragged.getValue().remove(); // Remove from old parent
                droppedOn.getValue().addChild(dragged.getValue()); //Add to dragged parent
                refreshNodeTree();
            }
        });
    }

    public void refreshNodeLibrary() {
        libraryListAdapter.clear();
        for(Map.Entry<String, CellNode> regionNode : NodeLibrary.loaded.entrySet()) {
            libraryListAdapter.add(new NodeLibraryEntry(regionNode.getKey(), regionNode.getValue()));
        }
    }

    public CellNode getSelectedNode() {
        TreeCellNode impl = (TreeCellNode) cellNodeTree.getSelectedNode();
        if(cellNodeTree.getSelectedNode() == null)
            return null;
        return impl.getValue();
    }

    public void setSelectedNode(CellNode selectedNode) {
        cellNodeTree.getSelection().clear();
        TreeCellNode node = (TreeCellNode) cellNodeTree.findNode(selectedNode);
        cellNodeTree.getSelection().set(node);
    }

    abstract class CreateLightCallback {
        abstract void callback(LightNode light);
    }

    class CreateLightDialog extends VisDialog {

        private final VisTextField lightName;
        private final Image diffuseColorPreview;
        private final Image specularColorPreview;
        private final VisTextButton saveButton;

        private LightNode light;

        public CreateLightDialog(final CreateLightCallback createLightCallback) {
            super("Create light node");
            addCloseButton();
            light = new LightNode("Light");

            VisTable content = new VisTable();
            content.add(new VisLabel("Name: "));
            content.add(lightName = new VisTextField(""));
            content.row().padBottom(5);

            content.add(new VisLabel("Diffuse Color: ", Style.label));
            content.add(diffuseColorPreview = new Image(Backgrounds.create(new Color(light.diffuse.x, light.diffuse.y, light.diffuse.z, 1.0f), 50, 25))).pad(5);
            diffuseColorPreview.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                        @Override
                        public void finished (Color newColor) {
                            light.diffuse.set(newColor.r, newColor.g, newColor.b);
                            diffuseColorPreview.setDrawable(Backgrounds.create(new Color(light.diffuse.x, light.diffuse.y, light.diffuse.z, 1), 50, 25));
                        }
                    });
                    Editor.ui.addActor(picker);
                }
            });
            content.row();

            content.add(new VisLabel("Specular Color: ", Style.label));
            content.add(specularColorPreview = new Image(Backgrounds.create(new Color(light.specular.x, light.specular.y, light.specular.z, 1.0f), 50, 25))).pad(5);
            specularColorPreview.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                        @Override
                        public void finished (Color newColor) {
                            light.specular.set(newColor.r, newColor.g, newColor.b);
                            specularColorPreview.setDrawable(Backgrounds.create(new Color(light.specular.x, light.specular.y, light.specular.z, 1), 50, 25));
                        }
                    });
                    Editor.ui.addActor(picker);
                }
            });
            content.row();

            content.add(new VisLabel("Light type"));

            final VisSelectBox<LightNode.LightType> lightTypeSelector = new VisSelectBox<>();
            lightTypeSelector.setItems(LightNode.LightType.POINT, LightNode.LightType.SPOTLIGHT);
            lightTypeSelector.setAlignment(Align.center);
            lightTypeSelector.setSelected(LightNode.LightType.POINT);
            lightTypeSelector.addListener(new ChangeListener() {
                public void changed(ChangeEvent event, Actor actor) {
                    light.type = lightTypeSelector.getSelected();
                }
            });
            content.add(lightTypeSelector).row();
            content.addSeparator().fillX().growX();

            content.add(saveButton = new VisTextButton("Submit"));
            content.row();
            saveButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    createLightCallback.callback(light);
                    hide();
                    super.clicked(event, x, y);
                }
            });
            add(content);
        }

    }
    class EditLightDialog extends VisDialog {

        private final VisTextField lightName;
        private final Image diffuseColorPreview;
        private final Image specularColorPreview;
        private final VisTextButton saveButton;


        public EditLightDialog(final LightNode light, final CreateLightCallback createLightCallback) {
            super("Edit light node");
            addCloseButton();

            VisTable content = new VisTable();
            content.add(new VisLabel("Name: "));
            content.add(lightName = new VisTextField(light.getName()));
            content.row().padBottom(5);

            content.add(new VisLabel("Diffuse Color: ", Style.label));
            content.add(diffuseColorPreview = new Image(Backgrounds.create(new Color(light.diffuse.x, light.diffuse.y, light.diffuse.z, 1.0f), 50, 25))).pad(5);
            diffuseColorPreview.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                        @Override
                        public void finished (Color newColor) {
                            light.diffuse.set(newColor.r, newColor.g, newColor.b);

                            diffuseColorPreview.setDrawable(Backgrounds.create(new Color(light.diffuse.x, light.diffuse.y, light.diffuse.z, 1), 50, 25));
                        }
                    });
                    Editor.ui.addActor(picker);
                }
            });
            content.row();
            diffuseColorPreview.setDrawable(Backgrounds.create(new Color(light.diffuse.x, light.diffuse.y, light.diffuse.z, 1.0f), 50, 25));

            content.add(new VisLabel("Specular Color: ", Style.label));
            content.add(specularColorPreview = new Image(Backgrounds.create(new Color(light.specular.x, light.specular.y, light.specular.z, 1.0f), 50, 25))).pad(5);
            specularColorPreview.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                        @Override
                        public void finished (Color newColor) {
                            light.specular.set(newColor.r, newColor.g, newColor.b);
                            specularColorPreview.setDrawable(Backgrounds.create(newColor, 50, 25));
                        }
                    });
                    Editor.ui.addActor(picker);
                }
            });
            content.row();
            specularColorPreview.setDrawable(Backgrounds.create(new Color(light.specular.x, light.specular.y, light.specular.z, 1.0f), 50, 25));


            content.add(new VisLabel("Light type"));

            final VisSelectBox<LightNode.LightType> lightTypeSelector = new VisSelectBox<>();
            lightTypeSelector.setItems(LightNode.LightType.POINT, LightNode.LightType.SPOTLIGHT);
            lightTypeSelector.setAlignment(Align.center);
            lightTypeSelector.setSelected(LightNode.LightType.POINT);
            lightTypeSelector.addListener(new ChangeListener() {
                public void changed(ChangeEvent event, Actor actor) {
                    light.type = lightTypeSelector.getSelected();
                }
            });
            content.add(lightTypeSelector).row();
            content.addSeparator().fillX().growX();

            lightTypeSelector.setSelected(light.type);

            content.add(saveButton = new VisTextButton("Submit"));
            content.row();
            saveButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    LightNode node = new LightNode(lightName.getText());
                    createLightCallback.callback(node);
                    hide();
                    super.clicked(event, x, y);
                }
            });
            add(content);
        }

    }
    public void refreshNodeTree() {
        Array array = new Array();
        cellNodeTree.findExpandedValues(array);
        cellNodeTree.clearChildren();

        WorldCell region = Editor.getCurrentCell();
        if(region == null) {
            return;
        }
        populateCellTree(null, region.root);
        cellNodeTree.restoreExpandedValues(array);
    }

    private void populateCellTree(TreeCellNode parent, CellNode child) {
        //Add node to node tree
        TreeCellNode childParent = new TreeCellNode(child);
        if(parent == null) {
            cellNodeTree.add(childParent);
        } else {
            parent.add(childParent);
        }
        for(CellNode childChild : child.children()) {
            populateCellTree(childParent, childChild);
        }
    }

    private class TreeCellNode extends Tree.Node<Tree.Node, CellNode, VisLabel> {
        private final VisLabel nameLabel;

        public TreeCellNode(CellNode node) {
            super(new VisLabel());
            this.setValue(node);
            nameLabel = getActor();
            nameLabel.setText(node.getName());
        }
    }


}
