package com.gengine.editor.convert;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.*;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.assimp.Assimp.aiProcess_FixInfacingNormals;


public class AssimpModelLoader {

    public static final String TEXTURE_DIRECTORY = "";
    /**
     * Notes from assimp docs:
     * <p>
     * mVertices -> Vertex positions.
     * If the mesh contains tangents, it automatically also contains bitangents.
     * A mesh does use only a single material.
     * If an imported model uses multiple materials, the import splits up the mesh.
     * <p>
     * Vertex animations refer to meshes by their names.
     */


    /* Maximum number of bones each vertex can be influenced by */
    private static final int BONES_PER_VERTEX = 4;

    private static final int MAX_TEX_COORDS = 4;
    /* Vertices bone entry contains the Bone ID and a weight */

    public static Model load(File input) throws Exception {
        AIScene aiScene = aiImportFile(input.getAbsolutePath(), aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate
                | aiProcess_FixInfacingNormals);
        if (aiScene == null) {
            throw new Exception("Error loading model [resourcePath: " + input + "");
        }

        int numMaterials = aiScene.mNumMaterials();

        Array<ModelMaterial> materials = new Array<>();
        for (int i = 0; i < numMaterials; i++) {
            AIMaterial aiMaterial = AIMaterial.create(aiScene.mMaterials().get(i));

            materials.add(convertMaterial(aiMaterial));
        }


        HashMap<String, ModelNode> nodesL = new HashMap<>();

        Array<ModelNode> nodes = new Array<>();
        Array<ModelMesh> meshes = new Array<>();
        ModelData modelData = new ModelData();

        int numMeshes = aiScene.mNumMeshes();

        for (int i = 0; i < numMeshes; i++) {
            AIMesh aiMesh = AIMesh.create(aiScene.mMeshes().get(i));

            String meshName = aiMesh.mName().dataString();

            CVertex[] vertices = new CVertex[aiMesh.mNumVertices()];
            for(int cv = 0; cv < vertices.length;cv++) {
                vertices[cv] = new CVertex();
            }
            for (int v = 0; v < aiMesh.mNumVertices(); v++) {
                AIVector3D pos = aiMesh.mVertices().get(v);
                float x = pos.x();
                float y = pos.y();
                float z = pos.z();
                System.out.println(pos.x() + ", " + pos.y() + ", " + pos.z());
                vertices[v].position.set(x, y, z);
                if (aiMesh.mNormals() != null) {
                    AIVector3D nor = aiMesh.mNormals().get(v);
                    vertices[v].normal.set(nor.x(), nor.y(), nor.z()).nor();
                } else {
                    vertices[v].normal.set(0, 1, 0);
                }
                if (aiMesh.mBitangents() != null) {
                    AIVector3D tangent = aiMesh.mBitangents().get(v);
                    AIVector3D bitangent = aiMesh.mBitangents().get(v);

                    vertices[v].tangent.set(tangent.x(), tangent.y(), tangent.z()).nor();
                    vertices[v].bitangent.set(bitangent.x(), bitangent.y(), bitangent.z()).nor();
                } else {
                    vertices[v].tangent.set(1, 0, 0).nor();
                    vertices[v].bitangent.set(0, 0, 1).nor();
                }

                for (int t = 0; t < CVertex.MAX_TEX_COORDS; t++) {
                    if (aiMesh.mTextureCoords(t) != null) {
                        AIVector3D tx = aiMesh.mTextureCoords(t).get(v);
                        vertices[v].textureCoords[t].set(tx.x(), tx.y(), 0).nor();
                    } else {
                        vertices[v].textureCoords[t].set(0, 0, 0).nor();
                    }
                }
                if (aiMesh.mNumBones() > 0) {
                    for (int t = 0; t < CVertex.BONES_PER_VERTEX; t++) {
                        vertices[v].boneW[t].set(0, 0);
                    }
                }
            }


            {gftyhttguh
                ModelNode node = new ModelNode();
                node.id = meshName;

                ModelNodePart nodePart = new ModelNodePart();
                nodePart.materialId = "" + materials.get(aiMesh.mMaterialIndex()).id;
                nodePart.meshPartId = "" + meshName + "-meshPart";

                for (int boneID = 0; boneID < aiMesh.mNumBones(); boneID++) {
                    AIBone aiBone = AIBone.create(aiMesh.mBones().get());
                    nodePart.bones = new ArrayMap<>();
                    nodePart.bones.put(aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
                    System.out.println(aiBone.mName().dataString() + " - " + aiBone.mOffsetMatrix());

                    prim:
                    for (int w = 0; w < aiBone.mNumWeights(); w++) {
                        AIVertexWeight bVW = aiBone.mWeights().get(w);
                        int vertexID = bVW.mVertexId();
                        float boneWeight = bVW.mWeight();

                        for (int vertexBone = 0; vertexBone < BONES_PER_VERTEX; vertexBone++) {
                            CVertex vert = vertices[vertexID];
                            float affectingWeight = vert.boneW[vertexBone].y; //getVertexBoneWeight(vertices, attr, vertexID, vertexBone);
                            if (boneWeight > affectingWeight) {
                                // Cycle down this and those below:
                                for (int c = BONES_PER_VERTEX - 2; c >= vertexBone; c--) {
                                    //Set bone ID and weight for c + 1 to c bone id + weight
                                    vert.boneW[c + 1].set(vert.boneW[c].x, vert.boneW[c].y);
                                }
                                vert.boneW[vertexBone].set(boneID, affectingWeight);
                                continue prim;
                            }
                        }
                    }
                }
                node.parts = new ModelNodePart[]{nodePart};
                nodes.add(node);
            }

            /* A mesh part. */
            ModelMesh modelMesh = new ModelMesh();

            {
                modelMesh.id = aiMesh.mName().dataString();
                modelMesh.attributes = getVertexAttributes(aiMesh);
                modelMesh.vertices = buildVertices(vertices, modelMesh.attributes);

                short[] indices = new short[aiMesh.mFaces().mNumIndices() * aiMesh.mNumFaces()];
                int index = 0;
                for (int face = 0; face < aiMesh.mNumFaces(); face++) {
                    AIFace aiFace = aiMesh.mFaces().get(face);
                    IntBuffer buffer = aiFace.mIndices();
                    while (buffer.remaining() > 0) {
                        indices[index++] = (short) buffer.get();
                    }
                }

                ModelMeshPart meshPart = new ModelMeshPart();
                meshPart.id = "" + meshName + "-meshPart";
                meshPart.indices = indices;
                meshPart.primitiveType = GL20.GL_TRIANGLES;

                modelMesh.parts = new ModelMeshPart[]{meshPart};
            }
            meshes.add(modelMesh);
        }

        AINode aiRootNode = aiScene.mRootNode();

        ModelNode rootNode = processNodesHierarchy(aiRootNode);

        Array<ModelAnimation> animations = new Array<>();

        int numAnims = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();
        for (int i = 0; i < numAnims; i++) {
            AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i));

            ModelAnimation animation = new ModelAnimation();
            animation.id = "" + aiAnimation.mName();

            int numChanels = aiAnimation.mNumChannels();
            PointerBuffer aiChannels = aiAnimation.mChannels();
            for (int j = 0; j < numChanels; j++) {
                AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(j));

                ModelNodeAnimation nodeAnimation = new ModelNodeAnimation();
                nodeAnimation.nodeId = aiNodeAnim.mNodeName().dataString();
                nodeAnimation.rotation = new Array<>();
                nodeAnimation.scaling = new Array<>();
                nodeAnimation.translation = new Array<>();


                int numFrames = aiNodeAnim.mNumPositionKeys();
                AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
                AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
                AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

                for (int f = 0; f < numFrames; f++) {
                    AIVectorKey translationKey = positionKeys.get(f);
                    AIVector3D translationVal = translationKey.mValue();

                    AIQuatKey quatKey = rotationKeys.get(f);
                    AIQuaternion aiQuat = quatKey.mValue();

                    AIVectorKey scalingKey = scalingKeys.get(f);
                    AIVector3D scalingVal = translationKey.mValue();

                    ModelNodeKeyframe<Quaternion> rotation = new ModelNodeKeyframe<>();
                    rotation.keytime = (float) quatKey.mTime();
                    rotation.value = new Quaternion(aiQuat.x(), aiQuat.y(), aiQuat.z(), aiQuat.w());
                    nodeAnimation.rotation.add(rotation);

                    ModelNodeKeyframe<Vector3> translation = new ModelNodeKeyframe<>();
                    translation.keytime = (float) translationKey.mTime();
                    translation.value = new Vector3(translationVal.x(), translationVal.y(), translationVal.z());
                    nodeAnimation.translation.add(translation);

                    ModelNodeKeyframe<Vector3> scaling = new ModelNodeKeyframe<>();
                    scaling.keytime = (float) scalingKey.mTime();
                    scaling.value = new Vector3(scalingVal.x(), scalingVal.y(), scalingVal.z());
                    nodeAnimation.scaling.add(scaling);

                    animation.nodeAnimations.add(nodeAnimation);
                }
                animations.add(animation);

                //Node node = rootNode.findByName(nodeName);
                //buildTransFormationMatrices(aiNodeAnim, node);
            }
        }


        modelData.nodes.addAll(nodes);
        modelData.meshes.addAll(meshes);
        modelData.materials.addAll(materials);

        return new Model(modelData);
    }


    private static float[] buildVertices(CVertex[] vertObjects, VertexAttribute[] attributes) {
        VertexAttributes attr = new VertexAttributes(attributes);

        int numVertices = vertObjects.length;
        int vertexSize = attr.vertexSize / Float.BYTES;

        float[] vertices = new float[numVertices * vertexSize];
        int offsetPos = attr.getOffset(VertexAttributes.Usage.Position);
        int normalPos = attr.getOffset(VertexAttributes.Usage.Normal);
        int colorPos = attr.getOffset(VertexAttributes.Usage.ColorUnpacked);
        int texPos = attr.getOffset(VertexAttributes.Usage.TextureCoordinates);
        int bonePos = attr.getOffset(VertexAttributes.Usage.BoneWeight);
        int biPos = attr.getOffset(VertexAttributes.Usage.BiNormal);
        int tanPos = attr.getOffset(VertexAttributes.Usage.Tangent);

        for (int vv = 0; vv < vertObjects.length; vv++) {
            int i = vv * vertexSize;
            CVertex vertex = vertObjects[vv];

            if (offsetPos > -1) {
                vertices[i + offsetPos] = vertex.position.x;
                vertices[i + offsetPos + 1] = vertex.position.x;
                vertices[i + offsetPos + 2] = vertex.position.x;
            }
            if (normalPos > -1) {
                vertices[i + normalPos] = vertex.normal.x;
                vertices[i + normalPos + 1] = vertex.normal.y;
                vertices[i + normalPos + 2] = vertex.normal.z;
            }
            if (colorPos > -1) {
                vertices[i + colorPos] = vertex.color.r;
                vertices[i + colorPos + 1] = vertex.color.g;
                vertices[i + colorPos + 2] = vertex.color.b;
                vertices[i + colorPos + 3] = vertex.color.a;
            }
            if (texPos > -1) {
                for (int tx = 0; tx < MAX_TEX_COORDS; tx++) {
                    int off = tx * 2;
                    vertices[i + texPos + off] = vertex.textureCoords[tx].x;
                    vertices[i + texPos + off + 1] = 1 - vertex.textureCoords[tx].y;//Flip, why?
                }
            }
            if (bonePos > -1) {
                for (int b = 0; b < BONES_PER_VERTEX; b++) {
                    int off = b * 2;
                    vertices[i + bonePos + off] = vertex.boneW[b].x; // ID
                    vertices[i + bonePos + off + 1] = vertex.boneW[b].y; //Weight
                }
            }
            if (biPos > -1) {
                vertices[i + biPos] = vertex.bitangent.x;
                vertices[i + biPos + 1] = vertex.bitangent.y;
            }
            if (tanPos > -1) {
                vertices[i + tanPos] = vertex.tangent.x;
                vertices[i + tanPos + 1] = vertex.tangent.y;
            }
        }


        return vertices;
    }


    private static VertexAttribute[] getVertexAttributes(AIMesh aiMesh) {
        ArrayList<VertexAttribute> attributes = new ArrayList<VertexAttribute>();
        attributes.add(VertexAttribute.Position());
        if (aiMesh.mNormals() != null)
            attributes.add(VertexAttribute.Normal());
        if (aiMesh.mBitangents() != null)
            attributes.add(VertexAttribute.Binormal());
        if (aiMesh.mTangents() != null)
            attributes.add(VertexAttribute.Tangent());
        if (aiMesh.mColors().hasRemaining())
            attributes.add(VertexAttribute.ColorUnpacked());

        for (int t = 0; t < MAX_TEX_COORDS; t++) {
            if (aiMesh.mTextureCoords(t) != null) {
                attributes.add(VertexAttribute.TexCoords(t));
            }
        }

        if (aiMesh.mBones() != null) {
            for (int i = 0; i < BONES_PER_VERTEX; i++) {
                attributes.add(VertexAttribute.BoneWeight(i));
            }
        }

        return attributes.toArray(new VertexAttribute[]{});
    }

    private static ModelNode processNodesHierarchy(AINode aiNode) {

        int numChildren = aiNode.mNumChildren();

        ModelNode node = new ModelNode();
        node.id = aiNode.mName().dataString();
        node.children = new ModelNode[numChildren];
        //nodeLookupMap.put(aiNode.mName().dataString(), node);
        for (int i = 0; i < numChildren; i++) {
            AINode aiChildNode = AINode.create(aiNode.mChildren().get(i));
            node.children[i] = processNodesHierarchy(aiChildNode);
        }

        return node;
    }

    private static Matrix4 toMatrix(AIMatrix4x4 aiMatrix4x4) {
        Matrix4 result = new Matrix4();
        result.val[Matrix4.M00] = aiMatrix4x4.a1();
        result.val[Matrix4.M10] = aiMatrix4x4.a2();
        result.val[Matrix4.M20] = aiMatrix4x4.a3();
        result.val[Matrix4.M30] = aiMatrix4x4.a4();

        result.val[Matrix4.M01] = aiMatrix4x4.b1();
        result.val[Matrix4.M11] = aiMatrix4x4.b2();
        result.val[Matrix4.M21] = aiMatrix4x4.b3();
        result.val[Matrix4.M31] = aiMatrix4x4.b4();

        result.val[Matrix4.M02] = aiMatrix4x4.c1();
        result.val[Matrix4.M12] = aiMatrix4x4.c2();
        result.val[Matrix4.M22] = aiMatrix4x4.c3();
        result.val[Matrix4.M32] = aiMatrix4x4.c4();

        result.val[Matrix4.M03] = aiMatrix4x4.d1();
        result.val[Matrix4.M13] = aiMatrix4x4.d2();
        result.val[Matrix4.M23] = aiMatrix4x4.d3();
        result.val[Matrix4.M33] = aiMatrix4x4.d4();
        return result;
    }

    protected static ModelMaterial convertMaterial(AIMaterial aiMaterial) throws Exception {

        AIString diffuseTexturePath = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, diffuseTexturePath, (IntBuffer) null,
                null, null, null, null, null);

        AIString specularTexturePath = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_SPECULAR, 0, specularTexturePath, (IntBuffer) null,
                null, null, null, null, null);

        AIString normalTexturePath = AIString.calloc();
        Assimp.aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, normalTexturePath, (IntBuffer) null,
                null, null, null, null, null);

        AIString materialName = AIString.create();
        aiGetMaterialString(aiMaterial, AI_MATKEY_NAME, 0, 0, materialName);

        String textPath = diffuseTexturePath.dataString();
        System.out.println(textPath);
        ModelMaterial material = new ModelMaterial();
        material.id = materialName.dataString();

             /*if (textPath != null && textPath.length() > 0) {

            ?? Seems pointless.
            String textureFile = "";
            if ( texturesDir != null && texturesDir.length() > 0 ) {
                textureFile += texturesDir + "/";
            }
            textureFile += textPath;
            textureFile = textureFile.replace("//", "/");
            copyTextureToDir(TEXTURE_DIRECTORY, textPath);
            //AIUVTransform aiuvTransform = AIUVTransform.create();
            //aiGetMaterialUVTransform(aiMaterial, AI_MATKEY_, 0,0, aiuvTransform);

            ModelTexture diffuse = new ModelTexture();
            diffuse.fileName = textPath;
            diffuse.id = "diffuse_" + materialName.dataString();
            diffuse.usage = ModelTexture.USAGE_DIFFUSE;

            ModelTexture normal = new ModelTexture();
            normal.fileName = textPath;
            normal.id = "normal_" + materialName.dataString();
            normal.usage = ModelTexture.USAGE_NORMAL;
            //texture.uvScaling

            material.textures.add(diffuse);
            //material.set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal(TEXTURE_DIRECTORY + ""))));
        }
        int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            material.diffuse = new Color(colour.r(), colour.g(), colour.b(), colour.a());
        }
        result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, colour);
        if (result == 0) {
            material.specular = new Color(colour.r(), colour.g(), colour.b(), colour.a());
        }*/
        return material;
    }

    private static void copyTextureToDir(String textureDirectory, String textPath) {
        FileHandle textureFileHandle = Gdx.files.absolute(textPath);
        FileHandle dir = Gdx.files.internal(textureDirectory);
        if (!dir.exists())
            dir.mkdirs();
        FileHandle copyTo = Gdx.files.internal(textureDirectory + "/" + textureFileHandle.name());
        textureFileHandle.copyTo(copyTo);
    }


}
