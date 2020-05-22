package com.gengine.render.world;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.gengine.core.model.*;
import com.gengine.core.model.terrain.TerrainNodeAttribute;
import com.gengine.core.model.terrain.TerrainTile;
import com.gengine.core.world.WorldCell;
import com.gengine.core.world.WorldManager;
import com.gengine.core.world.CellLocation;
import com.gengine.core.world.node.TerrainNode;

import java.util.ArrayList;

import static com.gengine.core.world.WorldCell.TILE_SIZE;

public class TerrainNodeRenderContext extends RenderContextProvider<TerrainNode> {

    public final CellLocation location;

    private final Mesh mesh;
    private final int posPos;
    private final int norPos;
    private final int uvPos;
    private final int colPos;
    private final int stride;

    private final float[] vertices;
    private final short[] indices;
    private final int terrainVertexCount;
    private final Vertex[] vertexInfo;

    private final Matrix4 worldTransform = new Matrix4();
    private final int tangentPos;
    private final int binormalPos;
    private final Material material;

    public TerrainNodeRenderContext(TerrainNode region) {
        super(region);
        this.location = getElement().getLocation();

        terrainVertexCount = WorldCell.SIZE + 1;
        VertexAttributes attributes = MeshBuilder.createAttributes(
                VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.Normal
                        | VertexAttributes.Usage.BiNormal
                        | VertexAttributes.Usage.Tangent
                        | VertexAttributes.Usage.ColorUnpacked
                        | VertexAttributes.Usage.TextureCoordinates);

        this.stride = attributes.vertexSize / 4;

        this.posPos = attributes.getOffset(VertexAttributes.Usage.Position, -1);
        this.norPos = attributes.getOffset(VertexAttributes.Usage.Normal, -1);
        this.uvPos = attributes.getOffset(VertexAttributes.Usage.TextureCoordinates, -1);
        this.colPos = attributes.getOffset(VertexAttributes.Usage.ColorUnpacked, -1);
        this.binormalPos = attributes.getOffset(VertexAttributes.Usage.BiNormal, -1);
        this.tangentPos = attributes.getOffset(VertexAttributes.Usage.Tangent, -1);

        final int numVertices = terrainVertexCount * terrainVertexCount * 4;
        final int numIndices = WorldCell.SIZE * WorldCell.SIZE * 6;

        this.mesh = new Mesh(true, numVertices, numIndices, attributes);

        this.vertices = new float[numVertices * stride];
        this.indices = new short[numIndices];

        vertexInfo = new Vertex[terrainVertexCount * terrainVertexCount];
        for (int x = 0; x < terrainVertexCount; x++) {
            for (int z = 0; z < terrainVertexCount; z++) {
                int vID = x * terrainVertexCount + z;
                vertexInfo[vID] = new Vertex();
            }
        }

        setVertices();
        updateMesh();

        material = new Material();
        material.set(new TerrainNodeAttribute(TerrainNodeAttribute.ATTRIBUTE_REGION, getElement()));
    }


    public void setVertices() {
        for (int x = 0; x < terrainVertexCount; x++) {
            for (int z = 0; z < terrainVertexCount; z++) {

                int vID = x * terrainVertexCount + z;
                Vertex vertex = vertexInfo[vID];

                float worldX = (location.startX() + x) * TILE_SIZE;
                float worldZ = (location.startY() + z) * TILE_SIZE;

                TerrainTile tile;
                if (x < WorldCell.SIZE && z < WorldCell.SIZE) {
                    tile = getElement().getTile(x, z);
                } else
                    tile = WorldManager.worldTile(worldX, worldZ);

                vertex.setPos(x * TILE_SIZE, tile.getHeight(), z * TILE_SIZE);
                vertex.uv.set(x & 1, z & 1);

                vertex.materialWeights[0] = tile.materialWeights[0];
                vertex.materialWeights[1] = tile.materialWeights[1];
                vertex.materialWeights[2] = tile.materialWeights[2];
                vertex.materialWeights[3] = tile.materialWeights[3];

                calculateNormal(vertex.normal, worldX, worldZ);

                cross(vertex.tangent, vertex.normal, bias);
                cross(vertex.binormal, vertex.normal, vertex.tangent).nor();
                cross(vertex.tangent, vertex.binormal, vertex.normal);
            }
        }
    }

    private Vector3 bias = new Vector3(1, 0, 0);

    public Vector3 cross(Vector3 origin, Vector3 a, Vector3 b) {
        if (origin == a) {
            final float a0 = a.x;
            final float a1 = a.y;
            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b.x) - (a0 * b.z);
            origin.z = (a.x * b.y) - (a1 * b.x);
        } else if (origin == b) {
            final float b0 = b.x;
            final float b1 = b.y;

            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b0) - (a.x * b.z);
            origin.z = (a.x * b1) - (a.y * b.x);
        } else {
            origin.x = (a.y * b.z) - (a.z * b.y);
            origin.y = (a.z * b.x) - (a.x * b.z);
            origin.z = (a.x * b.y) - (a.y * b.x);
        }
        return origin;
    }

    private void updateMesh() {
        for (int x = 0; x < terrainVertexCount; x++) {
            for (int z = 0; z < terrainVertexCount; z++) {
                int vID = x * terrainVertexCount + z;
                Vertex vertex = vertexInfo[vID];
                setVertex(vID, vertex);
            }
        }
        mesh.setVertices(vertices);
    }


    public void updateMaterials() {
        int index = 0;
        for (int x = 0; x < terrainVertexCount - 1; x++) {
            for (int z = 0; z < terrainVertexCount - 1; z++) {

                int vID = x * terrainVertexCount + z;

                indices[index++] = (short) vID;
                indices[index++] = (short) (vID + terrainVertexCount + 1);
                indices[index++] = (short) (vID + 1);

                indices[index++] = (short) vID;
                indices[index++] = (short) (vID + terrainVertexCount);
                indices[index++] = (short) (vID + terrainVertexCount + 1);
            }
        }
        mesh.setIndices(indices);
    }

    public void dispose() {
        mesh.dispose();
    }

    class MaterialGroup {

        public short[] indices;
        public int indexOffset;
        private Material material;

        public MaterialGroup(Material material, short... indices) {
            this.material = material;
            this.indices = indices;
        }

        public void insert(short... elements) {
            short[] nEw = new short[indices.length + elements.length];
            System.arraycopy(indices, 0, nEw, 0, indices.length);
            System.arraycopy(elements, 0, nEw, indices.length, elements.length);
            indices = nEw;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MaterialGroup) {
                MaterialGroup group = (MaterialGroup) obj;
                return group.material.equals(material);
            }
            return false;
        }
    }

    private final ArrayList<MaterialGroup> materialGroup = new ArrayList<MaterialGroup>();

    public MaterialGroup getMaterialGroup(Material material) {
        for (MaterialGroup m : materialGroup) {
            if (m.material.equals(material))
                return m;
        }
        return null;
    }


    public Vector3 position(Vector3 pos, float worldX, float worldY) {
        TerrainTile tt = WorldManager.worldTile(worldX, worldY);
        pos.set(worldX - location.worldX(), tt != null ? tt.getHeight() : 0, worldY - location.worldY());
        return pos;
    }

    public Vector3 linearComb(Vector3 dest, Vector3 a, float aC, Vector3 b, float bC) {
        dest.x = a.x * aC + b.x * bC;
        dest.y = a.y * aC + b.y * bC;
        dest.z = a.z * aC + b.z * bC;
        return dest;
    }

    public void calculateNormal(Vector3 normal, float wX, float wY) {
        normal.setZero();

        Vector3 me = position(new Vector3(), wX, wY);
        Vector3[] surroundingTiles = {
                position(new Vector3(), wX - TILE_SIZE, wY - TILE_SIZE),
                position(new Vector3(), wX - TILE_SIZE, wY),
                position(new Vector3(), wX - TILE_SIZE, wY + TILE_SIZE),
                position(new Vector3(), wX, wY + TILE_SIZE),
                position(new Vector3(), wX + TILE_SIZE, wY + TILE_SIZE),
                position(new Vector3(), wX + TILE_SIZE, wY),
                position(new Vector3(), wX + TILE_SIZE, wY - TILE_SIZE),
                position(new Vector3(), wX, wY - TILE_SIZE)
        };

        Vector3 tmpA = new Vector3();
        Vector3 tmpB = new Vector3();
        Vector3 tmpC = new Vector3();

        Vector3 dest = new Vector3();
        for (int i = 0; i < surroundingTiles.length; i++) {
            for (int k = 0; k < 2; k++) {
                if (surroundingTiles[i] != null && surroundingTiles[(i + 1) % surroundingTiles.length] != null) {
                    if (Float.isNaN(surroundingTiles[i].y) || Float.isNaN(surroundingTiles[(i + 1) % surroundingTiles.length].y))
                        continue;

                    linearComb(tmpA, surroundingTiles[i], 1, me, -1);
                    linearComb(tmpB, surroundingTiles[(i + 1) % surroundingTiles.length], 1, me, -1);
                    cross(tmpC, tmpA, tmpB).nor();
                    if (tmpC.y < 0)
                        tmpC.scl(-1);
                    dest.add(tmpC);
                }
            }
        }
        normal.set(dest).nor();
    }

    protected void setVertex(int index, Vertex vertex) {
        index *= stride;
        if (posPos >= 0) {
            vertices[index + posPos + 0] = vertex.position.x;
            vertices[index + posPos + 1] = vertex.position.y;
            vertices[index + posPos + 2] = vertex.position.z;
        }
        if (norPos >= 0) {
            vertices[index + norPos + 0] = vertex.normal.x;
            vertices[index + norPos + 1] = vertex.normal.y;
            vertices[index + norPos + 2] = vertex.normal.z;
        }
        if (uvPos >= 0) {
            vertices[index + uvPos + 0] = vertex.uv.x;
            vertices[index + uvPos + 1] = vertex.uv.y;
        }
        if (colPos >= 0) {
            vertices[index + colPos + 0] = vertex.materialWeights[0];
            vertices[index + colPos + 1] = vertex.materialWeights[1];
            vertices[index + colPos + 2] = vertex.materialWeights[2];
            vertices[index + colPos + 3] = vertex.materialWeights[3];
        }
        if (binormalPos >= 0) {
            vertices[index + binormalPos + 0] = vertex.binormal.x;
            vertices[index + binormalPos + 1] = vertex.binormal.y;
            vertices[index + binormalPos + 2] = vertex.binormal.z;
        }
        if (tangentPos >= 0) {
            vertices[index + tangentPos + 0] = vertex.tangent.x;
            vertices[index + tangentPos + 1] = vertex.tangent.y;
            vertices[index + tangentPos + 2] = vertex.tangent.z;
        }
    }

    public boolean meshDirty = true;
    public boolean materialDirty = true;

    @Override
    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (meshDirty) {
            setVertices();
            updateMesh();
            meshDirty = false;
        }
        if (materialDirty) {
            materialGroup.clear();
            updateMaterials();
            materialDirty = false;
        }
        Renderable r = pool.obtain();
        r.meshPart.mesh = mesh;
        r.worldTransform.set(getElement().getTransform());
        r.material = material;
        r.meshPart.offset = 0;
        r.meshPart.size = indices.length;
        r.meshPart.primitiveType = GL20.GL_TRIANGLES;
        r.meshPart.update();
        renderables.add(r);

    }

}
