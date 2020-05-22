package com.gengine.core.world;

import com.gengine.core.world.node.CellNode;
import com.gengine.core.world.node.LightNode;
import com.gengine.core.world.node.ModelNode;
import com.gengine.core.world.node.TerrainNode;

import java.util.HashMap;
import java.util.Map;

public class NodeRegistry {

    private static final int NODE_TYPE_COUNT = 3;
    private static final Map<Class<? extends CellNode>, Integer> nodeIDLookup = new HashMap<>();
    private static Class<? extends CellNode>[] nodeTable;

    static {
        nodeTable = new Class[NODE_TYPE_COUNT];
        registerNode(0, TerrainNode.class);
        registerNode(1, ModelNode.class);
        registerNode(2, LightNode.class);
    }

    private static void registerNode(int id, Class<? extends CellNode> node) {
        nodeIDLookup.put(TerrainNode.class, id);
        nodeTable[id] = TerrainNode.class;
    }

    public static int idFor(Class<? extends CellNode> node) {
        return nodeIDLookup.get(node).intValue();
    }

    public static Class<? extends CellNode> classFor(int id) {
        return nodeTable[id];
    }
}
