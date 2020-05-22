package com.gengine.editor.tool;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.lwjgl.opengl.GL11;

public class GlyphShader implements Shader {

    private final ShaderProgram program;
    private final int u_worldTrans;
    private final int u_projViewTrans;
    private final int u_diffuseColor;

    public GlyphShader() {

        final String vertexShader = Gdx.files.internal("assets/glyph_v.glsl").readString();
        final String fragmentShader = Gdx.files.internal("assets/glyph_f.glsl").readString();

        this.program = new ShaderProgram(vertexShader, fragmentShader);
        if (!program.isCompiled()) {
            System.err.println("Error with TerrainShader: " + program.getLog());
            System.exit(1);
        } else {
            Gdx.app.log("init", "TerrainShader compiled " + program.getLog() + "");
        }

        u_worldTrans = program.getUniformLocation("u_worldTrans");
        u_projViewTrans = program.getUniformLocation("u_projViewTrans");
        u_diffuseColor = program.getUniformLocation("u_diffuseColor");
    }

    @Override
    public void init() {
        if (!program.isCompiled()) {
            System.err.println("Error with GlyphShader: " + program.getLog());
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
    public void begin(Camera camera, RenderContext context) {
        int depthFunc = GL20.GL_LEQUAL;
        float depthRangeNear = 0f;
        float depthRangeFar = 1f;
        boolean depthMask = true;

        program.begin();
        program.setUniformMatrix(u_projViewTrans, camera.combined);

        context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar);
        context.setDepthMask(depthMask);
    }

    @Override
    public void render(Renderable renderable) {
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        if (renderable.material.has(ColorAttribute.Diffuse)) {
            ColorAttribute colorAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
            program.setUniformf(u_diffuseColor, colorAttr.color);
        } else {
            program.setUniformf(u_diffuseColor, Color.MAGENTA);
        }
       // GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        renderable.meshPart.render(program);
    }

    @Override
    public void end() {
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        program.end();
    }

    @Override
    public void dispose() {
        program.dispose();
    }
}
