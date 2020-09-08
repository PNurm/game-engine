package com.gengine.core.world.node;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.gengine.core.world.NodeRegistry;
import com.gengine.util.DatabaseObject;
import com.gengine.util.IOUtils;

import java.nio.ByteBuffer;
import java.util.*;

public abstract class CellNode implements DatabaseObject {

    private String name;
    private final Set<CellNode> children = new HashSet<>();
    private final Map<String, CellNode> childrenMap = new HashMap<>();

    public Vector3 position = new Vector3();
    public final Quaternion rotation = new Quaternion();
    public final Vector3 scale = new Vector3(1, 1, 1);

    private Matrix4 combined = new Matrix4();

    public Matrix4 getTransform() {
        if (parent == null) {
            return combined.set(position, rotation, scale);
        }
        return combined.set(position, rotation, scale).mulLeft(parent.getTransform());
    }

    protected CellNode parent;

    public CellNode(String name) {
        this.name = name;
    }

    public void addChild(CellNode child) {
        children.add(child);
        child.setParent(this);
        refreshChildMap();
    }

    private void refreshChildMap() {
        childrenMap.clear();
        for (CellNode c : children) {
            if (childrenMap.containsKey(c.name))
                childrenMap.put(c.name, null);
            else
                childrenMap.put(c.name, c);
        }
    }

    public Collection<? extends CellNode> children() {
        return children;
    }


    public CellNode child(String id) {
        return childrenMap.get(id);
    }

    public String getName() {
        return name;
    }
    public CellNode getParent() {
        return this.parent;
    }

    public void setParent(CellNode parent) {
        this.parent = parent;
    }

    public boolean movable() {
        return true;
    }

    public void remove() {
        if (parent != null) {
            parent.children.remove(this);
            this.parent = null;
        }
    }

    @Override
    public void pack(ByteBuffer data) {
        IOUtils.pack(name, data);
        data.putShort((short) children().size());
        for(CellNode node : children()) {
            int id = NodeRegistry.idFor(node.getClass());
            data.put((byte) id);
            node.pack(data);
        }
    }

    @Override
    public int length() {
        //String length + childrenCount
        int len = IOUtils.sizeof(name) + Short.BYTES;
        for (CellNode c : children) {
            //node type id + length
            len += Short.BYTES + c.length();
        }
        return len;
    }

    @Override
    public void unpack(ByteBuffer src) {
        name = IOUtils.unpackString(src);
        int childrenCount = src.getShort();

        for(int i = 0; i < childrenCount; i++) {
            int id = src.get();
            CellNode node = null;
            try {
                node = NodeRegistry.classFor(id).newInstance();
                node.unpack(src);
            } catch (Exception e) {
                e.printStackTrace();
            }
            addChild(node);
        }
    }

    public boolean isParentOf(CellNode node) {
        return children.contains(node);
    }

    public void setName(String text) {
        this.name = text;
    }

    public void dispose() {

    }
}
