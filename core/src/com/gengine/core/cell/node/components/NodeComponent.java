package com.gengine.core.cell.node.components;

import com.gengine.core.cell.node.CellNode;
import com.gengine.core.model.TerrainMesh;

public abstract class NodeComponent {

    protected final CellNode node;

    public NodeComponent(CellNode node) {
        this.node = node;
    }

    public CellNode getNode() {
        return node;
    }

}
