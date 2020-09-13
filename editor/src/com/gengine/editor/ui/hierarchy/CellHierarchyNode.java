package com.gengine.editor.ui.hierarchy;


import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.gengine.core.cell.node.CellNode;
import com.kotcrab.vis.ui.widget.VisLabel;

public class CellHierarchyNode extends Tree.Node<Tree.Node, CellNode, VisLabel> {
    private final VisLabel nameLabel;

    public CellHierarchyNode(CellNode node) {
        super(new VisLabel());
        this.setValue(node);
        nameLabel = getActor();
        nameLabel.setText(node.getIdentifier()  + "." + node.getExtension());
    }
}
