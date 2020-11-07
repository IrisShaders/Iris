package net.coderbot.iris.shaders;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.Program;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.uniforms.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public abstract class Shader {
    private Path shaderPackPath;
    private Program program;
    Shader(Path shaderPackPath){
        this.shaderPackPath = shaderPackPath;
    }

    /**
     * A simple shader utility method that calls and creates shaders.
     * Is identical to {@link Iris#useTerrainShaders()}, but takes in consideration fallback shaders
     * @throws IOException if the Shader cannot be compiled
     */
    final void useShaders() throws IOException {
        if (program == null) {
            InputStream vertexStream = new FileInputStream(shaderPackPath + "/" + getFileName() + ".vsh");
            InputStream fragmentStream = new FileInputStream(shaderPackPath + "/" + getFileName() + ".fsh");
            ProgramBuilder builder;

            try {
                builder = ProgramBuilder.begin(getFileName(), vertexStream, fragmentStream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize Iris!", e);
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
     * Returns the fallback shader that is used if this shader is not present.
     * @return the fallback shader name.
     */
    protected String getFallbackShader(){
        return "gbuffers_basic";
    }
}
