package com.gengine.core.cell;

import com.gengine.core.cell.node.CellNode;
import com.gengine.core.cell.node.LightNode;
import com.gengine.core.cell.node.ModelNode;
import com.gengine.core.cell.node.TerrainNode;
import com.gengine.core.cell.node.components.NodeComponent;

import java.util.*;

public class NodeManager {
    /**
     * Mapping of component to implementing child nodes
     */
    private static final HashMap<Class<?>, ArrayList<CellNode>> componentImplMap = new HashMap<>();

    /**
     * Gets nodes based on component
     * @return
     */
    public static Collection<CellNode> getNodes(Class<?> component) {
        if(!componentImplMap.containsKey(component)) {
            return new ArrayList<>();
        }
        return Collections.unmodifiableList(componentImplMap.get(component));
    }

    public static void registerNode(Class<? extends NodeComponent> component, CellNode node) {
        ArrayList<CellNode> cellNodes = componentImplMap.getOrDefault(component, new ArrayList<>());
        cellNodes.add(node);
        componentImplMap.put(component, cellNodes);

        //TODO: Nodes are not removed from the map.
        System.out.println("Registering node " + node + " to component array " + component);
    }


    public static void unregisterNode(Class<? extends NodeComponent> aClass, CellNode node) {
        ArrayList<CellNode> cellNodes = componentImplMap.getOrDefault(aClass, new ArrayList<>());
        cellNodes.remove(node);
        componentImplMap.put(aClass, cellNodes);
    }


    public static class Registry {

        private static final int NODE_TYPE_COUNT = 4;
        private static final Map<Class<? extends CellNode>, Integer> nodeIDLookup = new HashMap<>();
        private static Class<? extends CellNode>[] nodeTable;

        static {
            nodeTable = new Class[NODE_TYPE_COUNT];
            registerNode(0, TerrainNode.class);
            registerNode(1, ModelNode.class);
            registerNode(2, LightNode.class);
            registerNode(3, WorldCell.class);
        }

        private static void registerNode(int id, Class<? extends CellNode> node) {
            nodeIDLookup.put(node, id);
            nodeTable[id] = node;
        }

        public static int idFor(Class<? extends CellNode> node) {
            if(nodeIDLookup.get(node) == null) {
                System.out.println("Unable to find node ID for " + node.getName());
            }
            return nodeIDLookup.get(node);
        }

        public static Class<? extends CellNode> classFor(int id) {
            return nodeTable[id];
        }
    }
}
