package com.gengine.core.cell.node;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.gengine.core.cell.NodeManager;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.cell.node.components.NodeComponent;
import com.gengine.core.util.DatabaseObject;
import com.gengine.core.util.IOUtils;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * The most basic and abstract definition of an entity in the 3D world
 * Cell nodes use tree like structure with infinite depth.
 */
public abstract class CellNode implements DatabaseObject {
    /**
     * Link to file this node was loaded from
     */
    public FileHandle fileHandle;

    /**
     * Identifier of this cell
     */
    private String identifier;

    /**
     * Children of this node
     */
    private final Set<CellNode> children = new HashSet<>();

    /**
     * Name to cell node mapping
     */
    private final Map<String, CellNode> childrenMap = new HashMap<>();

    /**
     * Position 3D-vector
     */
    public Vector3 position = new Vector3();
    /**
     * Rotation quaternion
     */
    public final Quaternion rotation = new Quaternion();

    /**
     * Scale 3D-vector
     */
    public final Vector3 scale = new Vector3(1, 1, 1);
    /**
     * All of above in matrix
     */
    private final Matrix4 combined = new Matrix4();

    public Matrix4 getTransform() {
        if (parent == null) {
            return combined.set(position, rotation, scale);
        }
        return combined.set(position, rotation, scale).mulLeft(parent.getTransform());
    }

    /**
     * Components this cell node uses
     */
    private HashMap<Class<?>, NodeComponent> components = new HashMap<>();

    /***
     * Adds a new component to this node
     * @param component
     * @param <T>
     */
    public <T extends NodeComponent> void addComponent(T component) {
        registerComponent(component.getClass(), component);
    }

    private <T extends NodeComponent> void registerComponent(Class<? extends NodeComponent> type, T component) {
        this.components.put(type, component);
        NodeManager.registerNode(type, this);

        Class<?> superClass = type.getSuperclass();
        if(superClass != null &&
                superClass != Object.class &&
                superClass != NodeComponent.class &&
                superClass.isAssignableFrom(component.getClass())) {
            registerComponent((Class<? extends NodeComponent>) superClass, component);
        }
    }

    /**
     * Removes a component from this node
     * @param component
     * @param <T>
     */
    public <T extends NodeComponent> void removeComponent(T component) {
        this.components.remove(component.getClass());
        NodeManager.unregisterNode(component.getClass(), this);
    }

    /**
     * Gets a component from this node
     * @param componentClass
     * @param <E>
     */
    @SuppressWarnings("unchecked")
    public <E extends NodeComponent> E getComponent(Class<?> componentClass) {
        return (E) this.components.get(componentClass);
    }

    /**
     * Parent of this node
     */
    protected CellNode parent;
    protected WorldCell root;

    public CellNode(String identifier) {
        this.identifier = identifier;
    }

    public void addChild(CellNode child) {
        children.add(child);
        child.setParent(this);
        refreshChildMap();
    }

    private void refreshChildMap() {
        childrenMap.clear();
        for (CellNode c : children) {
            if (childrenMap.containsKey(c.identifier))
                childrenMap.put(c.identifier, null);
            else
                childrenMap.put(c.identifier, c);
        }
    }

    public Collection<? extends CellNode> children() {
        return children;
    }


    public CellNode child(String id) {
        return childrenMap.get(id);
    }

    public String getIdentifier() {
        return identifier;
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
    public void pack(ByteBuffer data) { //
        IOUtils.pack(identifier, data);
        data.putShort((short) children().size());
        for (CellNode node : children()) {
            int id = NodeManager.Registry.idFor(node.getClass());
            data.put((byte) id);
            node.pack(data);
        }
    }

    @Override
    public int length() {
        //String length + childrenCount
        int len = IOUtils.sizeof(identifier) + 2;
        for (CellNode child : children) {
            //node type id (byte) + length
            len += 1 + child.length();
        }
        return len;
    }

    @Override
    public void unpack(ByteBuffer src) {
        identifier = IOUtils.unpackString(src);
        int childrenCount = src.getShort() & 0xFFFF;
        for (int i = 0; i < childrenCount; i++) {
            int id = src.get() & 0xFF;
            CellNode cellNode = null;
            try {
                cellNode = NodeManager.Registry.classFor(id).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            cellNode.unpack(src);
            addChild(cellNode);
        }
    }

    public boolean isParentOf(CellNode node) {
        return children.contains(node);
    }

    public void setIdentifier(String text) {
        this.identifier = text;
    }

    public void dispose() {

    }

    public String getExtension() {
        return "nd";
    }

    public boolean hasComponent(Class<?> component) {
        return components.containsKey(component);
    }

    public boolean getAll(Class<?> component) {
        return components.containsKey(component);
    }
}
