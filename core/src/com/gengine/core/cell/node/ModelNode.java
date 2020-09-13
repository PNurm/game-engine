package com.gengine.core.cell.node;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.gengine.core.cell.node.components.ModelRenderComponent;

public class ModelNode extends CellNode {

    private Model model;
    private ModelInstance inst;

    public ModelNode(String identifier) {
        super(identifier);

        addComponent(new ModelRenderComponent(this));
    }

    public ModelNode setModel(Model model) {
        this.model = model;
        this.inst = new ModelInstance(model);
        return this;
    }

}
