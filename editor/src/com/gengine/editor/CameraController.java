package com.gengine.editor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter {

    private Camera camera;

    private Vector3 tmpV1 = new Vector3();
    private Vector3 tmpV2 = new Vector3();

    private float startX;
    private float startY;

    public float rotateAngle = 360f;
    public float translateUnits = 10f;

    private Vector3 target = new Vector3();
    private int button;

    public CameraController(Camera camera) {
        this.camera = camera;
    }


    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == this.button) this.button = -1;
        return super.touchUp(screenX, screenY, pointer, button);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        startX = screenX;
        startY = screenY;
        this.button = button;

        return super.touchDown(screenX, screenY, pointer, button);
    }

    private Quaternion quaternion = new Quaternion();

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        final float translateUnits = camera.position.dst(target);
        final float deltaX = (screenX - startX) / Gdx.graphics.getWidth();
        final float deltaY = (startY - screenY) / Gdx.graphics.getHeight();
        startX = screenX;
        startY = screenY;

        if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
                camera.translate(tmpV1.set(camera.direction).crs(camera.up).nor().scl(-deltaX * translateUnits));
                camera.translate(tmpV2.set(camera.up).scl(-deltaY * translateUnits));
                target.add(tmpV1).add(tmpV2);
            } else {
                tmpV1.set(camera.direction).crs(camera.up).y = 0f;
                camera.rotateAround(target, tmpV1.nor(), deltaY * rotateAngle);
                camera.rotateAround(target, Vector3.Y, deltaX * -rotateAngle);
            }
        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        float distanceToTarget = camera.position.dst(target);
        final float zoomUnits = camera.position.dst(target) * 0.05f * amount;

        camera.translate(tmpV1.set(camera.direction).scl(-zoomUnits));

        return super.scrolled(amount);
    }

    public void update() {

    }

    public void setCamera(float x, float y, float z) {
        this.camera.position.set(x, y, z);
        this.target.set(x, 0f, z);
        this.camera.lookAt(target);
    }

    public Vector3 getTarget() {
        return target;
    }
}
