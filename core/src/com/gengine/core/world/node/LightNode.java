package com.gengine.core.world.node;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.util.IOUtils;

import java.nio.ByteBuffer;

/**
 * Lights are not being rendered, other than in the world editor.
 */
public class LightNode extends RenderableNode {

    public static enum LightType {
        SPOTLIGHT, DIRECTIONAL, POINT
    }

    public Vector3 ambient; // RGB, flicker amp
    public Vector3 diffuse;
    public Vector3 specular;
    public Vector3 position;
    public Vector3 direction;

    public LightType type;

    @Override
    public void pack(ByteBuffer data) {
        data.put((byte) type.ordinal());
        IOUtils.pack(ambient, data);
        IOUtils.pack(diffuse, data);
        IOUtils.pack(specular, data);

        IOUtils.pack(position, data);
        IOUtils.pack(direction, data);
    }

    @Override
    public void unpack(ByteBuffer src) {
        super.unpack(src);
    }

    @Override
    public int length() {
        return 1 + IOUtils.sizeof(ambient)
                + IOUtils.sizeof(diffuse)
                + IOUtils.sizeof(specular)
                + IOUtils.sizeof(position)
                + IOUtils.sizeof(direction);
    }

    @Override
    public BoundingBox getBoundingBox(BoundingBox out, boolean world) {
        out.min.set(position.x - 1f, position.y - 1f, position.z - 1f);
        out.max.set(position.x + 1f, position.y + 1f, position.z + 1f);
        if (world)
            out.mul(getTransform());
        return out;
    }

    public LightNode(String text) {
        super(text);
        ambient = new Vector3();
        diffuse = new Vector3();
        specular = new Vector3();
        direction = new Vector3();
        position = new Vector3();
        this.type = LightType.POINT;
    }

    @Override
    public String toString() {
        return "LightNode[" + getName() + "]";
    }
}
