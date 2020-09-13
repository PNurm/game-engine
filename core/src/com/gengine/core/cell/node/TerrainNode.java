package com.gengine.core.cell.node;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;
import com.gengine.core.cell.WorldManager;
import com.gengine.core.cell.node.components.ModelRenderComponent;
import com.gengine.core.model.Material;
import com.gengine.core.cell.WorldCell;
import com.gengine.core.model.TerrainMesh;
import com.gengine.core.util.DatabaseObject;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.gengine.core.Constants.MULTITEXTURE_COUNT;

public class TerrainNode extends CellNode {
    /**
     * The world cell this terrain is attached to.
     */
    private final WorldCell cell;

    /**
     * The terrains tiles
     */
    private Tile[][] tiles = new Tile[WorldCell.SIZE][WorldCell.SIZE];

    /**
     * Materials used in this cell, maximum count is defined at constants
     */
    private Material[] materials = new Material[MULTITEXTURE_COUNT];

    /**
     * The terrain map used to blend together terrain's textures
     */
    private TextureMap textureMap;

    public TerrainMesh terrainMesh;

    public TerrainNode(WorldCell cell) {
        super("Terrain");
        this.cell = cell;
        this.textureMap = new TextureMap(TextureMap.WIDTH, TextureMap.HEIGHT);

        for (int i = 0; i < WorldCell.SIZE; i++)
            for (int j = 0; j < WorldCell.SIZE; j++)
                tiles[i][j] = new Tile(this);

        Arrays.fill(materials, Material.NULL);
    }

    public void build() {
        ModelRenderComponent renderComponent;
        addComponent(renderComponent = new ModelRenderComponent(this));

        terrainMesh = new TerrainMesh(this);
        terrainMesh.meshDirty = true;
        renderComponent.setRenderable(terrainMesh);
    }

    @Override
    public String toString() {
        return "TerrainNode{" +
                "cell=" + cell +
                '}';
    }

    @Override
    public void dispose() {
        if(terrainMesh != null) {
            terrainMesh.dispose();
            terrainMesh = null;
        }
    }

    public WorldCell getCell() {
        return cell;
    }

    public boolean movable() {
        return false;
    }

    public Tile getTile(int x, int z) {
        if (x >= tiles.length || z >= tiles[0].length || x < 0 || z < 0)
            return null;

        return tiles[x][z];
    }

    public Tile tile(float lx, float ly) {
        int ax = (int) Math.floor(lx / WorldCell.TILE_SIZE);
        int ay = (int) Math.floor(ly / WorldCell.TILE_SIZE);
        return tiles[ax][ay];
    }

    public void setMaterial(int channelID, Material asset) {
        materials[channelID] = asset;
    }

    public Material getMaterial(int textureID) {
        return materials[textureID];
    }

    public TextureMap getTerrainBlendMap() {
        return textureMap;
    }


    @Override
    public void pack(ByteBuffer data) {
        super.pack(data);

        data.putShort((short) textureMap.getWidth());
        data.putShort((short) textureMap.getHeight());

        for (int i = 0; i < textureMap.getWidth(); i++)
            for (int j = 0; j < textureMap.getHeight(); j++) {
                int pixel = textureMap.getMap().getPixel(i, j);
                data.putInt(pixel);
            }
        data.put((byte) WorldCell.SIZE);
        for (int i = 0; i < WorldCell.SIZE; i++)
            for (int j = 0; j < WorldCell.SIZE; j++)
                tiles[i][j].pack(data);
    }

    @Override
    public void unpack(ByteBuffer src) {
        super.unpack(src);

        int blendWidth = src.getShort();
        int blendHeight = src.getShort();
        for(int i = 0; i < blendWidth; i++) {
            for(int j = 0; j < blendHeight;j++) {
                int pixel = src.getInt();
                textureMap.getMap().drawPixel(i,j, pixel);
            }
        }
        int tileCount = src.getShort();
        if(tileCount != WorldCell.SIZE * 2) {
            System.out.println("Tile count mismatch");
        }
        for(int i = 0; i <  WorldCell.SIZE; i++) {
            for (int j = 0; j < WorldCell.SIZE; j++) {
                tiles[i][j].unpack(src);
            }
        }
    }

    @Override
    public int length() {
        int length = 0;
        length += 2;
        length += 2;
        for(int i = 0; i < textureMap.getWidth(); i++) {
            for(int j = 0; j < textureMap.getHeight();j++) {
                length += 4;
            }
        }
        length += 2;
        for(int i = 0; i <  WorldCell.SIZE; i++) {
            for (int j = 0; j < WorldCell.SIZE; j++) {
                length += tiles[i][j].length();
            }
        }
        return super.length() + length;
    }

    public static class Attr extends Attribute {

        public static final String ATTRIBUTE_REGION_ALIAS = "region0";
        public static final long ATTRIBUTE_REGION = register(ATTRIBUTE_REGION_ALIAS);

        public TerrainNode region;

        protected static long Mask = ATTRIBUTE_REGION;

        /**
         * Method to check whether the specified type is a valid DoubleAttribute
         * type
         */
        public static Boolean is(final long type) {
            return (type & Mask) != 0;
        }

        public Attr(long type, TerrainNode region) {
            super(type);
            if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
            this.region = region;
        }

        public Attr(Attr other) {
            super(other.type);
            if (!is(type)) throw new GdxRuntimeException("Invalid type specified");
            this.region = other.region;
        }

        @Override
        public Attribute copy() {
            return new Attr(this);
        }

        @Override
        public int hashCode() {
            final int prime = 7;
            final long v = NumberUtils.doubleToLongBits(region.hashCode());
            return prime * super.hashCode() + (int) (v ^ (v >>> 32));
        }

        @Override
        public int compareTo(Attribute o) {
            if (type != o.type) return type < o.type ? -1 : 1;
            TerrainNode otherValue = ((Attr) o).region;
            return region.equals(otherValue) ? 0 : -1;
        }

    }

    @Override
    public String getExtension() {
        return "terrain";
    }

    public static class Tile implements DatabaseObject {

        public TerrainNode terrainNode;
        public float[] materialWeights = new float[MULTITEXTURE_COUNT];
        private float height;

        public Tile(TerrainNode terrainNode) {
            this.terrainNode = terrainNode;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        @Override
        public int length() {
            return 4 + 2;
        }

        @Override
        public void pack(ByteBuffer dest) {
            dest.putFloat(height);
           // dest.putShort((short) texture);
        }

        @Override
        public void unpack(ByteBuffer src) {
            height = src.getFloat();
            //texture = src.getShort();
        }

        public float[] getTextureWeights() {
            return materialWeights;
        }
    }

    public static class TextureMap {

        public static final int WIDTH = 512;
        public static final int HEIGHT = 512;

        private Texture texture;
        private int width;
        private int height;
        private Pixmap map;

        public TextureMap(int width, int height) {
            this.map = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            this.map.setBlending(Pixmap.Blending.None);
            this.texture = new Texture(map);
            this.width = width;
            this.height = height;
        }

        public Pixmap getMap() {
            return map;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Texture asTexture() {
            return texture;
        }

        public void updateTexture() {
            texture.draw(map, 0, 0);
        }
    }

    public static class TerrainShader implements Shader {

        public static boolean DEBUG_NORMALS = false;
        public static boolean WIREFRAME = false;
        private final int u_cameraPosition;

        private ShaderProgram program;

        private int u_worldTrans;
        private int u_projViewTrans;
        private int u_normalMatrix;


        public Renderable renderable;
        private Camera camera;
        private RenderContext context;

        private final String vertexShader;
        private final String fragmentShader;
        private int u_blendTexture;

        public TerrainShader(WorldManager worldManager, final Renderable renderable) {
            this.renderable = renderable;
            vertexShader = Gdx.files.local("assets/shaders/world_v.glsl").readString();
            fragmentShader = Gdx.files.local("assets/shaders/world_f.glsl").readString();

            this.program = new ShaderProgram(vertexShader, fragmentShader);

            if (!program.isCompiled()) {
                System.err.println("Error with TerrainShader: " + program.getLog());
                System.exit(1);
            } else {
                Gdx.app.log("init", "TerrainShader compiled " + program.getLog() + "");
            }

            u_worldTrans = program.getUniformLocation("u_worldTrans");
            u_cameraPosition = program.getUniformLocation("u_cameraPosition");
            u_projViewTrans = program.getUniformLocation("u_projViewTrans");
            u_normalMatrix = program.getUniformLocation("u_normalMatrix");

            u_blendTexture = program.getUniformLocation("u_blendTexture");
        }


        @Override
        public void begin(final Camera camera, final RenderContext context) {
            this.camera = camera;
            this.context = context;

            program.begin();
            program.setUniformMatrix(u_projViewTrans, camera.combined);
            program.setUniformf(u_cameraPosition, camera.position.x, camera.position.y, camera.position.z, 1.1881f / (camera.far * camera.far));
            context.setDepthTest(GL20.GL_LEQUAL);
        }

        Matrix3 tmpM = new Matrix3();


        @Override
        public void render(final Renderable renderable) {
            if(WIREFRAME) {
                renderable.meshPart.primitiveType = GL20.GL_LINES;
            }
            program.setUniformi(program.getUniformLocation("u_debugNormals"), DEBUG_NORMALS ? 1 : 0);
            if (!renderable.material.has(BlendingAttribute.Type))
                context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
            program.setUniformMatrix(u_normalMatrix, tmpM.set(renderable.worldTransform).inv().transpose());

            int u_direction = program.getUniformLocation("u_directionalLight.direction");
            int u_ambient = program.getUniformLocation("u_directionalLight.ambient");
            int u_diffuse = program.getUniformLocation("u_directionalLight.diffuse");
            int u_specular = program.getUniformLocation("u_directionalLight.specular");

            //program.setUniformf(u_alphaTest, 0.1f);
            //program.setUniformf("alphaTesting", 1);
            context.setDepthTest(GL20.GL_LEQUAL, 0f, 1f);
            context.setDepthMask(true);

            assignCellUniforms(renderable);

            program.setUniformf(u_direction, -0.1f, -1, -0.3f);
            program.setUniformf(u_ambient, 0.05f, 0.05f, 0.05f);
            program.setUniformf(u_diffuse, 0.8f, 0.8f, 0.8f);
            program.setUniformf(u_specular, 0.1f, 0.1f, 0.1f);

            renderable.meshPart.render(program);
        }

        final static Texture blankTexture = createSolidBackgroundT(Color.WHITE, 32, 32);
        final static Texture blankNormal = createSolidBackgroundT(new Color(0x008080FF), 32, 32);

        public static Texture createSolidBackgroundT(Color color, int w, int h) {
            Pixmap labelColor = new Pixmap(w, h, Pixmap.Format.RGBA8888);
            labelColor.setColor(color);
            labelColor.fill();
            return new Texture(labelColor);
        }

        protected void assignCellUniforms(Renderable renderable) {

            final Attr regionAttr = (Attr) renderable.material
                    .get(Attr.ATTRIBUTE_REGION);

            if (regionAttr != null) {
                final TerrainNode region = regionAttr.region;
                bindTexture(u_blendTexture, region.getTerrainBlendMap().asTexture());

                for(int i = 0; i < MULTITEXTURE_COUNT; i++) {

                    int u_diffuseT = program.getUniformLocation("materials[" + i + "].diffuseTexture");
                    int u_normalT = program.getUniformLocation("materials[" + i + "].normalTexture");
                    int u_specularT = program.getUniformLocation("materials[" + i + "].specularTexture");

                    int u_ambientC = program.getUniformLocation("materials[" + i + "].ambientColor");
                    int u_diffuseC = program.getUniformLocation("materials[" + i + "].diffuseColor");
                    int u_specularC = program.getUniformLocation("materials[" + i + "].specularColor");
                    int u_shiny = program.getUniformLocation("materials[" + i + "].shininess");

                    Material asset = region.getMaterial(i);

                    Texture diffuseTexture = asset.resolveDiffuse();
                    Texture normalTexture = asset.resolveNormal();
                    Texture specularTexture = null;//TODO: Implement specular textures

                    program.setUniformf(u_diffuseC, asset.getDiffuseColor());
                    program.setUniformf(u_specularC, asset.getSpecularColor());
                    program.setUniformf(u_ambientC, asset.getAmbientColor());
                    program.setUniformf(u_shiny, asset.getShininess());

                    bindTexture(u_diffuseT, diffuseTexture != null ? diffuseTexture : blankTexture);
                    bindTexture(u_normalT, normalTexture != null ? normalTexture : blankNormal);
                    bindTexture(u_specularT, specularTexture != null ? specularTexture : blankTexture);
                }

                int MAX_POINT_LIGHTS = 4;
                int lightCount = 0;
                for(CellNode cellNode : region.getParent().children()) {
                    if(cellNode instanceof LightNode) {
                        if(lightCount >= MAX_POINT_LIGHTS) {
                            break;
                        }
                        LightNode node = (LightNode) cellNode;

                        Vector3 pos = node.getTransform().getTranslation(new Vector3());

                        int u_position = program.getUniformLocation("lights[" + lightCount + "].position");
                        int u_direction = program.getUniformLocation("lights[" + lightCount + "].direction");
                        int u_diffuse = program.getUniformLocation("lights[" + lightCount + "].diffuse");
                        int u_specular = program.getUniformLocation("lights[" + lightCount + "].specular");

                        program.setUniformf(u_position, pos);
                        program.setUniformf(u_direction, node.direction);
                        program.setUniformf(u_diffuse, node.diffuse);
                        program.setUniformf(u_specular, node.specular);

                        lightCount++;
                    }
                }
                int numLights = program.getUniformLocation("u_numLights");

                program.setUniformi(numLights, lightCount);
            }
        }

        public void bindTexture(int uniform, Texture t) {
            int unit = context.textureBinder.bind(t);
            program.setUniformi(uniform, unit);
        }

        @Override
        public void init() {
            if (!program.isCompiled()) {
                System.err.println("Error with TerrainShader: " + program.getLog());
                System.exit(1);
            }
        }

        @Override
        public int compareTo(final Shader other) {
            return 0;
        }

        @Override
        public boolean canRender(final Renderable renderable) {
            return true;
        }

        @Override
        public void end() {
            context.textureBinder.end();
            program.end();
        }

        @Override
        public void dispose() {
            program.dispose();
        }
    }
}
