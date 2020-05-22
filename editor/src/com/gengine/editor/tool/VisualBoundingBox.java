package com.gengine.editor.tool;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.model.Vertex;
import com.gengine.core.world.node.CellNode;
import com.gengine.core.world.node.RenderableNode;

public class VisualBoundingBox implements RenderableProvider {

    private final Material material;
    private final Mesh boundingBoxMesh;
    private final int vertexSize;
    private final Mesh translateLines;

    private Vertex[] vertices;
    private CellNode node;

    private BoundingBox boundingBox = new BoundingBox();
    private Matrix4 worldTransform = new Matrix4();

    public VisualBoundingBox() {
        material = new Material();
        vertices = new Vertex[8];
        for (int i = 0; i < vertices.length; i++)
            vertices[i] = new Vertex();

        boundingBoxMesh = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());
        vertexSize = boundingBoxMesh.getVertexSize() / 4;

        translateLines = new Mesh(true, 9, 9, VertexAttribute.Position(),VertexAttribute.ColorUnpacked());


    }

    public void reset() {
        this.node = null;
        this.boundingBox.clr();
        for (int i = 0; i < vertices.length; i++)
            vertices[i] = new Vertex();
    }

    public void set(RenderableNode node) {
        this.node = node;
        this.boundingBox.clr();

        node.getBoundingBox(boundingBox, false);
        worldTransform.set(node.getTransform());

        vertices[0].position.set(boundingBox.max.x, boundingBox.max.y, boundingBox.max.z);
        vertices[1].position.set(boundingBox.max.x, boundingBox.max.y, boundingBox.min.z);
        vertices[2].position.set(boundingBox.max.x, boundingBox.min.y, boundingBox.max.z);
        vertices[3].position.set(boundingBox.max.x, boundingBox.min.y, boundingBox.min.z);

        vertices[4].position.set(boundingBox.min.x, boundingBox.max.y, boundingBox.max.z);
        vertices[5].position.set(boundingBox.min.x, boundingBox.max.y, boundingBox.min.z);
        vertices[6].position.set(boundingBox.min.x, boundingBox.min.y, boundingBox.max.z);
        vertices[7].position.set(boundingBox.min.x, boundingBox.min.y, boundingBox.min.z);

        float[] verts = new float[24 * vertexSize];
        short[] indices = new short[]{
                5, 7, 3, 2, 0, 1, 3, 1, 5, 4, 0, 4, 6, 2, 6, 7
        };

        for (int i = 0; i < 8; i++) {
            int index = i * vertexSize;
            verts[index] = vertices[i].position.x;
            verts[index + 1] = vertices[i].position.y;
            verts[index + 2] = vertices[i].position.z;

            verts[index + 3] = 1;
            verts[index + 4] = 1;
            verts[index + 5] = 0;
            verts[index + 6] = 1;
        }

        boundingBoxMesh.setVertices(verts);
        boundingBoxMesh.setIndices(indices);

        float[] translateLineVerts = new float[4 * 7];

        translateLineVerts[0] = 0;
        translateLineVerts[1] = 0;
        translateLineVerts[2] = 0;
        translateLineVerts[3] = 0;
        translateLineVerts[4] = 0;
        translateLineVerts[5] = 0;
        translateLineVerts[6] = 0;

        translateLineVerts[7] = boundingBox.max.x + 3f;
        translateLineVerts[8] = 0f;
        translateLineVerts[9] = 0f;
        translateLineVerts[10] = 1;
        translateLineVerts[11] = 0;
        translateLineVerts[12] = 0;
        translateLineVerts[13] = 1;

        translateLineVerts[14] = 0f;
        translateLineVerts[15] = boundingBox.max.y + 3f;
        translateLineVerts[16] = 0f;
        translateLineVerts[17] = 0;
        translateLineVerts[18] = 1;
        translateLineVerts[19] = 0;
        translateLineVerts[20] = 1;

        translateLineVerts[21] = 0f;
        translateLineVerts[22] = 0f;
        translateLineVerts[23] = boundingBox.max.z +3f;
        translateLineVerts[24] = 0;
        translateLineVerts[25] = 0;
        translateLineVerts[26] = 1;
        translateLineVerts[27] = 1;

        short translateIndices[] = {
                0, 1,
                0, 2,
                0, 3,
        };

        translateLines.setVertices(translateLineVerts);
        translateLines.setIndices(translateIndices);

    }


    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (node != null) {

            Renderable r = pool.obtain();
            r.worldTransform.set(worldTransform);
            r.meshPart.mesh = boundingBoxMesh;
            r.material = material;
            r.meshPart.offset = 0;
            r.meshPart.size = boundingBoxMesh.getNumIndices();
            r.meshPart.primitiveType = GL20.GL_LINE_STRIP;
            r.meshPart.update();

            renderables.add(r);


            r = pool.obtain();
            r.worldTransform.set(worldTransform);
            r.meshPart.mesh = translateLines;
            r.material = new Material();
            r.meshPart.offset = 0;
            r.meshPart.size = translateLines.getNumIndices();
            r.meshPart.primitiveType = GL20.GL_LINES;
            renderables.add(r);

        }
    }
}
