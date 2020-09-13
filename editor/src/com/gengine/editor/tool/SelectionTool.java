package com.gengine.editor.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.gengine.core.Core;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.CellNode;
import com.gengine.core.cell.node.components.RenderComponent;
import com.gengine.editor.Editor;
import com.gengine.core.RenderCore;

public class SelectionTool extends EditorTool {


    private float mh;
    private float mv;
    private float rh;
    private float rv;
    private BoundingBox boundingBox = new BoundingBox();

    enum TransformMode {
        ROTATE,
        TRANSLATE,
        SCALE
    }

    enum TransformAxis {
        X, Y, Z
    }

    private Vector3 terrainPoint = new Vector3();
    private Vector3 lastPosition = new Vector3();

    private Vector2 lastMousePos = new Vector2();
    private Vector2 mouseDelta = new Vector2();

    private TransformMode transformMode = null;
    private TransformAxis transformAxis = null;

    private SelectionBounds selectionBounds;
    private CellNode selection;
    private GlyphShader glyphShader = new GlyphShader();
    private Ray ray;

    public SelectionTool() {
        selectionBounds = new SelectionBounds();
        setInputAdapter(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (button == 1) {
                    if (getSelection() != null) {
                        ray = Editor.ui.getSceneWidget().getViewport().getPickRay(screenX, screenY);
                        terrainPoint = Core.world().pickPoint(terrainPoint, ray);
                        transformMode = TransformMode.TRANSLATE;
                        lastPosition.set(terrainPoint);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {

                terrainPoint = Core.world().pickPoint(terrainPoint, ray);
                float width = Editor.ui.getSceneWidget().getViewport().getScreenWidth();
                float height = Editor.ui.getSceneWidget().getViewport().getScreenHeight();

                float aspectRatio = Editor.ui.getSceneWidget().getViewport().getScreenWidth() / Editor.ui.getSceneWidget().getViewport().getScreenHeight();

                mouseDelta.set(screenX, screenY).sub(lastMousePos);
                mh = -aspectRatio * mouseDelta.x / width;
                mv = -mouseDelta.x / height;

                rh = mouseDelta.x / 1 * 5.0f;
                rv = mouseDelta.y / 1 * 5.0f;

                lastMousePos.set(screenX, screenY);

                return super.touchDragged(screenX, screenY, pointer);
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                transformMode = null;
                transformAxis = null;

                return super.touchUp(screenX, screenY, pointer, button);
            }
        });
    }

    @Override
    public void render(RenderCore renderCore) {
        renderCore.render(selectionBounds, glyphShader);
    }

    @Override
    public void update() {
        if (Gdx.input.isButtonJustPressed(1)) {
            pickSelection();
        }
        if (getSelection() != null) {
            if (transformMode != null) {
                Vector3 nodePos = getSelection().position;
                Vector3 nodeScl = getSelection().scale;
                Quaternion nodeRotation = getSelection().rotation;

                if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    transformMode = TransformMode.SCALE;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
                    transformMode = TransformMode.TRANSLATE;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                    transformMode = TransformMode.ROTATE;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                    transformAxis = TransformAxis.X;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
                    transformAxis = TransformAxis.Y;
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
                    transformAxis = TransformAxis.Z;
                }

                if (transformMode == TransformMode.TRANSLATE) {

                    Vector3 delta = lastPosition.sub(terrainPoint);
                    if (transformAxis == TransformAxis.X) {
                        nodePos.set(nodePos.x - delta.x, nodePos.y, nodePos.z);
                    } else if (transformAxis == TransformAxis.Y) {
                        float y = mv * 80f;
                        nodePos.set(nodePos.x, nodePos.y + y, nodePos.z);
                    } else if (transformAxis == TransformAxis.Z) {
                        nodePos.set(nodePos.x, nodePos.y, nodePos.z - delta.z);
                    } else {
                        nodePos.sub(delta);
                    }
                    if(nodePos.x > WorldCell.SIZE) {
                        nodePos.x = WorldCell.SIZE;
                    } else if(nodePos.x < 0) {
                        nodePos.x = 0;
                    }
                    if(nodePos.z > WorldCell.SIZE) {
                        nodePos.z = WorldCell.SIZE;
                    } else if(nodePos.z < 0) {
                        nodePos.z = 0;
                    }
                } else if (transformMode == TransformMode.SCALE) {
                    //Vector3 delta = lastPosition.sub(terrainPoint);

                    float ScaleAmount = (float) Math.pow(2.0f, mv * 4.0f);
                    nodeScl.scl(ScaleAmount);
                } else if (transformMode == TransformMode.ROTATE) {
                    Vector3 rotateAround = null;
                    if (transformAxis == TransformAxis.X) {
                        rotateAround = Vector3.X;
                    }
                    if (transformAxis == TransformAxis.Y) {
                        rotateAround = Vector3.Y;
                    }
                    if (transformAxis == TransformAxis.Z) {
                        rotateAround = Vector3.Z;
                    }
                    if (transformAxis != null) {
                        Quaternion nodeRot = new Quaternion();
                        nodeRot.setFromAxis(rotateAround, rh + rv);
                        nodeRotation.mul(nodeRot);
                    }
                    rh = 0;
                    rv = 0;
                }
                selectionBounds.set(getSelection());

                mv = 0;

                lastPosition.set(terrainPoint);
            }
        }

    }

    private void pickSelection() {
        WorldCell cell = Editor.getCurrentCell();

        CellNode pickedNode = pickCellNode(cell);
        if(pickedNode != null) {
            setSelection(pickedNode);
            System.out.println("Picked " + pickedNode);
        }

    }

    private CellNode pickCellNode(CellNode node) {
        if(node.hasComponent(RenderComponent.class)) {
            boundingBox.clr();

            RenderComponent renderComponent = node.getComponent(RenderComponent.class);
            if(renderComponent.getBoundingBox(boundingBox, true) == null) {
                return null;
            }
            if (Intersector.intersectRayBounds(Editor.getLastRay(), boundingBox, null)) {
                return node;
            }
            System.out.println(node + " does not intersect");
        }
        for(CellNode child : node.children()) {
            return pickCellNode(child);
        }
        return null;
    }

    public CellNode getSelection() {
        return selection;
    }

    public void setSelection(CellNode selection) {
        this.selection = selection;
        selectionBounds.reset();
        if(selection.hasComponent(RenderComponent.class)) {
            selectionBounds.set(selection);
        }
        Editor.ui.getCellTree().setSelectedNode(selection);
    }

    @Override
    public String getIcon() {
        return null;
    }
}
