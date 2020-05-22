package com.gengine.editor.convert;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class CVertex {

    public static final int BONES_PER_VERTEX = 4;
    public static final int MAX_TEX_COORDS = 4;

    public Vector3 position = new Vector3();
    public Vector3 normal = new Vector3();
    public Color color = new Color();


    public Vector3 bitangent = new Vector3();
    public Vector3 tangent = new Vector3();

    public Vector3[] textureCoords = new Vector3[] {new Vector3(), new Vector3(), new Vector3(), new Vector3()};
    /* x = id, y = weight*/
    public Vector2[] boneW = new Vector2[] {new Vector2(), new Vector2(), new Vector2(), new Vector2()};

}
