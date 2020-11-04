package net.coderbot.iris.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class OutlineShaders extends Shader {
    public OutlineShaders(Path shaderPackPath) {
        super(shaderPackPath);
    }

    @Override
    public Path getFileName(Path shaderPackPath) {
        return Paths.get(shaderPackPath + "/gbuffers_basic");
    }

    @Override
    public GlProgram createShaders(GlShader fragment, GlShader vertex) throws IOException {
        final int programRef = GlStateManager.createProgram();
        GlProgram program = new GlProgram() {
            @Override
            public int getProgramRef() {
                return programRef;
            }

            @Override
            public void markUniformsDirty() {

            }

            @Override
            public GlShader getVertexShader() {
                return vertex;
            }

            @Override
            public GlShader getFragmentShader() {
                return fragment;
            }
        };
        GlProgramManager.linkProgram(program);
        return program;
    }
}
