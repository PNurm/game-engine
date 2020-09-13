package com.gengine.core.cell.node;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.gengine.core.cell.node.components.DecalRenderComponent;
import com.gengine.core.util.IOUtils;

import java.nio.ByteBuffer;

/**
 * Lights are not being rendered, other than in the world editor.
 */
public class LightNode extends CellNode {

    public LightNode() {
        this("UNNAMED_LIGHT");
    }

    public LightNode(String text) {
        super(text);
        addComponent(new DecalRenderComponent(this));

        Texture texture = new Texture(Gdx.files.local("assets/icons/lightbulb.png"));
        Decal decal = Decal.newDecal(new TextureRegion(texture), true);
        decal.setWidth(1.5f);
        decal.setHeight(1.5f);

        DecalRenderComponent decalComponent = getComponent(DecalRenderComponent.class);
        decalComponent.setDecal(decal);

        this.type = LightType.POINT;
    }

    public enum LightType {
        SPOTLIGHT, DIRECTIONAL, POINT
    }

    public Vector3 ambient = new Vector3(1f, 1f, 1f);
    public Vector3 diffuse = new Vector3(1f, 1f, 1f);
    public Vector3 specular = new Vector3();
    public Vector3 direction = new Vector3();

    public LightType type;

    @Override
    public void pack(ByteBuffer data) {
        super.pack(data);

        data.put((byte) type.ordinal());
        System.out.println(diffuse);

        IOUtils.pack(ambient, data);
        IOUtils.pack(diffuse, data);
        IOUtils.pack(specular, data);

        IOUtils.pack(position, data);
        IOUtils.pack(direction, data);

        System.out.println("Packed " + getIdentifier());
    }

    @Override
    public void unpack(ByteBuffer src) {
        super.unpack(src);

        this.type = LightType.values()[src.get() & 0xFF];

        IOUtils.unpack(ambient, src);
        IOUtils.unpack(diffuse, src);
        IOUtils.unpack(specular, src);

        IOUtils.unpack(position, src);
        IOUtils.unpack(direction, src);
    }

    @Override
    public int length() {
        int lightSize = 0;

        lightSize += 1; //Light Type
        lightSize += IOUtils.sizeof(ambient);
        lightSize += IOUtils.sizeof(diffuse);
        lightSize += IOUtils.sizeof(specular);
        lightSize += IOUtils.sizeof(position);
        lightSize += IOUtils.sizeof(direction);

        return super.length() + lightSize;
    }

    @Override
    public String toString() {
        return "LightNode{" +
                "ambient=" + ambient +
                ", diffuse=" + diffuse +
                ", specular=" + specular +
                ", direction=" + direction +
                ", type=" + type +
                '}';
    }

    @Override
    public String getExtension() {
        return "lnd";
    }
}
