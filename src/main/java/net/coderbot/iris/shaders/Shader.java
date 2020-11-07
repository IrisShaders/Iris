package net.coderbot.iris.shaders;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.uniforms.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A class that represents a single shader
 */
public abstract class Shader {
    private Program program;
    Shader(){
    }

    /**
     * A simple shader utility method that calls and creates shaders.
     * Is identical to {@link Iris#useTerrainShaders()}, but takes in consideration fallback shaders
     */
    final void useShaders() {
        if (program == null) {
            ShaderPack.ProgramSource gbuffersTexturedSource = getProgramSource(Iris.getShaderPack());

            Objects.requireNonNull(gbuffersTexturedSource.getVertexSource());
            Objects.requireNonNull(gbuffersTexturedSource.getFragmentSource());
            ProgramBuilder builder;
            try {
                builder = ProgramBuilder.begin(getFileName(),
                        gbuffersTexturedSource.getVertexSource().orElse(null),
                        gbuffersTexturedSource.getFragmentSource().orElse(null));
            } catch (IOException e) {
                throw new RuntimeException("Shader compilation failed!", e);
            }

            CommonUniforms.addCommonUniforms(builder);
            CelestialUniforms.addCelestialUniforms(builder);
            SystemTimeUniforms.addSystemTimeUniforms(builder);
            ViewportUniforms.addViewportUniforms(builder);
            WorldTimeUniforms.addWorldTimeUniforms(builder);
           program = builder.build();
        }
           program.use();
        }

    /**
     * The file name of the target shader
     * @return the name of the shader.
     */
    protected abstract String getFileName();

    /**
     * Returns the fallback shader that is used if the current shader is not present in the pack.
     * @return the fallback shader name.
     */
    protected String getFallbackShader(){
        return "gbuffers_basic";
    }

    /**
     *
     * @param pack the current user's shaderpack instance
     * @return the program source
     */
    protected abstract ShaderPack.ProgramSource getProgramSource(ShaderPack pack);
}
