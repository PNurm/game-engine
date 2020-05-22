package com.gengine.editor.ui.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
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
import com.gengine.core.world.node.ModelNode;
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

import static com.badlogic.gdx.graphics.GL20.GL_TRIANGLES;

public class EditorRegionNodeTree extends VisTable {

    private final VisTree nodeTree;
    private final ScrollPane scrollPane;
    private final VisTable content;
    private final SimpleListAdapter<CellNode> simpleListAdapter;
    private final ListView<CellNode> nodeLibrary;
    private final VisTextButton modelsTabButton;
    private final VisTextButton lightsTabButton;
    private final VisTextButton createNodeBtn;
    private final VisTextButton addSelected;

    private CellNodeMenu cellTreeMenu;

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

    public EditorRegionNodeTree() {
        this.nodeTree = new VisTree();
        this.cellTreeMenu = new CellNodeMenu();
        nodeTree.getSelection().setProgrammaticChangeEvents(false);

        content = new VisTable();
        content.align(Align.left | Align.top);


        scrollPane = new VisScrollPane(nodeTree);
        scrollPane.setFlickScroll(false);
        scrollPane.setFadeScrollBars(false);
        content.add(scrollPane).height(200).fill().expand();

        add(new VisLabel("Region Nodes")).expandX().fillX().pad(3f).row();
        //addSeparator().row();
        add(content).fill().expand().row();

        VisTable libraryTabs = new VisTable();
        libraryTabs.add(modelsTabButton = new VisTextButton("Models"));
        libraryTabs.add(lightsTabButton = new VisTextButton("Lights"));

        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.setMaxCheckCount(1);
        buttonGroup.setMinCheckCount(1);
        buttonGroup.add(modelsTabButton, lightsTabButton);

        simpleListAdapter = new SimpleListAdapter<CellNode>(new Array<CellNode>());
        simpleListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.SINGLE);

        nodeTree.addListener(new InputListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                TreeCellNode treeCellNode = (TreeCellNode) nodeTree.getNodeAt(y);
                if(treeCellNode == null) {
                    return;
                }
                CellNode node = treeCellNode.getValue();
                System.out.println("hello");
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

        nodeLibrary = new ListView<CellNode>(simpleListAdapter);
        nodeLibrary.setItemClickListener(new ListView.ItemClickListener<CellNode>() {
            @Override
            public void clicked(CellNode item) {
                if(item instanceof LightNode) {

                    EditLightDialog d = new EditLightDialog((LightNode) item, new CreateLightCallback() {
                        @Override
                        void callback(LightNode light) {

                        }
                    });
                    d.show(getStage());
                }
            }
        });
        VisTable library = new VisTable();
        library.add(new VisLabel("Node Library")).colspan(2).expandX().fillX().pad(3f).row();
        library.add(libraryTabs).colspan(2).fillX().row();
        library.add(nodeLibrary.getMainTable()).height(150).colspan(2).grow().expand().row();
        library.add(addSelected = new VisTextButton("Add selected"));
        library.add(createNodeBtn = new VisTextButton(""));
        add(library).fill().expand();
        pack();

        modelsTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createNodeBtn.setText("Import model");

                ModelBuilder modelBuilder = new ModelBuilder();
                Material m = new Material();
                m.set(ColorAttribute.createDiffuse(1, 0, 0, 1));

                Model sphere = modelBuilder.createSphere(6, 6, 6, 6, 6, GL_TRIANGLES, m, VertexAttributes.Usage.Position);

                ModelNode node = new ModelNode("SphereModel");
                node.position.set(0, 10f, 0);
                node.setModel(sphere);

                refreshNodeTree();

                super.clicked(event, x, y);
            }
        });
        lightsTabButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                createNodeBtn.setText("Create new light");
                super.clicked(event, x, y);
            }
        });
        addSelected.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(simpleListAdapter.getSelection().size == 0) {
                    return;
                }
                CellNode node = simpleListAdapter.getSelection().get(0);
                Editor.getCurrentCell().root.addChild(node);

                refreshNodeTree();
                super.clicked(event, x, y);
            }
        });
        createNodeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(lightsTabButton.isChecked()) {
                    CreateLightDialog d  = new CreateLightDialog(
                            new CreateLightCallback() {
                                @Override
                                void callback(LightNode light) {
                                    NodeLibrary.addNode(light);
                                    refreshNodeLibrary();
                                }
                             });
                    d.show(Editor.ui);
                }
                super.clicked(event, x, y);
            }
        });

        setupDragAndDrop();
    }

    private DragAndDrop dragAndDrop = new DragAndDrop();

    private void setupDragAndDrop() {
        dragAndDrop.addSource(new DragAndDrop.Source(nodeTree) {
            @Override
            public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
                DragAndDrop.Payload payload = new DragAndDrop.Payload();
                Tree.Node node = nodeTree.getNodeAt(y);
                if(node != null) {
                    payload.setObject(node);
                    return payload;
                }
                return null;
            }
        });

        dragAndDrop.addTarget(new DragAndDrop.Target(nodeTree) {
            @Override
            public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                Tree.Node node = nodeTree.getNodeAt(y);
                if(node == null && nodeTree.getSelectedNode() == null) {
                    return false;
                }
                if(node != null && !nodeTree.getSelection().contains(node)) {
                    nodeTree.getSelection().set(node);
                }
                return true;
            }

            @Override
            public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                TreeCellNode dragged = (TreeCellNode) payload.getObject();
                TreeCellNode droppedOn = (TreeCellNode) nodeTree.getNodeAt(y);
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
        simpleListAdapter.clear();
        for(Map.Entry<String, CellNode> regionNode : NodeLibrary.nodeLibrary.entrySet()) {
            simpleListAdapter.add(regionNode.getValue());
        }
    }

    public CellNode getSelectedNode() {
        TreeCellNode impl = (TreeCellNode) nodeTree.getSelectedNode();
        if(nodeTree.getSelectedNode() == null)
            return null;
        return impl.getValue();
    }

    public void setSelectedNode(CellNode selectedNode) {
        nodeTree.getSelection().clear();
        TreeCellNode node = (TreeCellNode) nodeTree.findNode(selectedNode);
        nodeTree.getSelection().set(node);
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
        nodeTree.findExpandedValues(array);
        nodeTree.clearChildren();

        WorldCell region = Editor.getCurrentCell();
        if(region == null) {
            return;
        }
        populateCellTree(null, region.root);
        nodeTree.restoreExpandedValues(array);
    }

    private void populateCellTree(TreeCellNode parent, CellNode child) {
        //Add node to node tree
        TreeCellNode childParent = new TreeCellNode(child);
        if(parent == null) {
            nodeTree.add(childParent);
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
