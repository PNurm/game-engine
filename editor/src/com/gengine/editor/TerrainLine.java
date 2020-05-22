package com.gengine.editor;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class TerrainLine implements RenderableProvider {

    private final int maxVertices;
    private int pointCount;
    private final float[] points;

    private final int stride;
    private final int posPos;
    private float[] vertices;
    private final Mesh mesh;

    public TerrainLine(int size) {
        this.maxVertices = size;
        this.points = new float[maxVertices * 2];

        VertexAttributes attributes = MeshBuilder.createAttributes(VertexAttributes.Usage.Position);
        this.stride = attributes.vertexSize / 4;
        this.posPos = attributes.getOffset(VertexAttributes.Usage.Position, -1);

        this.vertices = new float[maxVertices * 3];
        this.mesh = new Mesh(true, maxVertices, maxVertices, attributes);
    }

    public void flush() {
        if (pointCount > 1) {
            int vertex = 0;
            int segmentSize = maxVertices / pointCount;

            for (int currentPoint = 0; currentPoint < pointCount; currentPoint++) {

                int nextPoint = (currentPoint + 1) % pointCount;

                float x1 = points[currentPoint * 3];
                float y1 = points[currentPoint * 3 + 1];
                float z1 = points[currentPoint * 3 + 2];

                float x2 = points[nextPoint * 3];
                float y2 = points[nextPoint * 3 + 1];
                float z2 = points[nextPoint * 3 + 2];

                for (int q = 0; q < segmentSize; q++) {
                    float t = q / (float) segmentSize;

                    float worldX = x1 + (x2 - x1) * t;
                    float worldY = y1 + (y2 - y1) * t;
                    float worldZ = z1 + (z2 - z1) * t;

                    setVertex(vertex++, worldX, worldY, worldZ);
                }
            }
        }
        short[] ind = new short[maxVertices];
        for (short i = 0; i < ind.length; i++) {
            ind[i] = i;
        }
        mesh.setVertices(vertices);
        mesh.setIndices(ind);
        pointCount = 0;
    }

    public void submit(float wx, float wy) {
        submit(wx, Editor.world().worldHeight(wx, wy) + .35f, wy);
    }

    public void submit(float worldX, float worldY, float worldZ) {
        points[pointCount * 3] = worldX;
        points[pointCount * 3 + 1] = worldY;
        points[pointCount * 3 + 2] = worldZ;
        pointCount++;
    }

    protected void setVertex(int index, float x, float y, float z) {
        index *= stride;
        if (posPos >= 0) {
            vertices[index + posPos + 0] = x;
            vertices[index + posPos + 1] = y;
            vertices[index + posPos + 2] = z;
        }
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Renderable r = pool.obtain();
        r.worldTransform.idt();
        r.material = new Material();
        r.meshPart.mesh = mesh;
        r.meshPart.primitiveType = GL20.GL_LINE_LOOP;
        r.meshPart.offset = 0;
        r.meshPart.size = maxVertices;
        r.meshPart.update();

        renderables.add(r);
    }
}
