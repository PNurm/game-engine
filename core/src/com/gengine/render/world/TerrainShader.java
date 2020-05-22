package com.gengine.render.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.gengine.core.model.Material;
import com.gengine.core.model.terrain.TerrainNodeAttribute;
import com.gengine.core.world.WorldManager;
import com.gengine.core.world.node.TerrainNode;
import com.gengine.util.Backgrounds;
import com.gengine.core.world.node.LightNode;
import com.gengine.core.world.node.CellNode;

import static com.gengine.Constants.MULTITEXTURE_COUNT;

/**
 * Simple shader for an object with a texture
 */
public class TerrainShader implements Shader {

    public static boolean DEBUG_NORMALS = false;
    public static boolean WIREFRAME = false;
    private final int u_cameraPosition;
    private final WorldManager worldManager;

    private ShaderProgram program;

    private int u_worldTrans;
    private int u_projViewTrans;
    private int u_normalMatrix;

    private int u_diffuseColor;
    private int u_alphaTest;

    public Renderable renderable;
    private Camera camera;
    private RenderContext context;

    private final String vertexShader;
    private final String fragmentShader;
    private int u_blendTexture;

    public TerrainShader(WorldManager worldManager, final Renderable renderable) {
        this.renderable = renderable;
        this.worldManager = worldManager;
        vertexShader = Gdx.files.internal("assets/world_v.glsl").readString();
        fragmentShader = Gdx.files.internal("assets/world_f.glsl").readString();

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
        u_diffuseColor = program.getUniformLocation("u_diffuseColor");
        u_alphaTest = program.getUniformLocation("u_alphaTest");

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

    final static Texture blankTexture = Backgrounds.createSolidBackgroundT(Color.WHITE, 32, 32);
    final static Texture blankNormal = Backgrounds.createSolidBackgroundT(new Color(0x008080FF), 32, 32);

    protected void assignCellUniforms(Renderable renderable) {

        final TerrainNodeAttribute regionAttr = (TerrainNodeAttribute) renderable.material
                .get(TerrainNodeAttribute.ATTRIBUTE_REGION);

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
