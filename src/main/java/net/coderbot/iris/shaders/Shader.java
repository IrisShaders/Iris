package net.coderbot.iris.shaders;

import net.coderbot.iris.Iris;
import net.coderbot.iris.uniforms.Uniforms;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Shader {
    private Path shaderPackPath;
    private boolean shadersUsed = false;
    private Uniforms uniforms;
    private GlProgram program;
    public Shader(Path shaderPackPath){
        this.shaderPackPath = shaderPackPath;
    }
    public final void useShaders() throws IOException {
        if (!shadersUsed) {
            InputStream vertexStream = new FileInputStream(this.getFileName(shaderPackPath) + ".vsh");
            InputStream fragmentStream = new FileInputStream(this.getFileName(shaderPackPath) + ".fsh");
            GlShader vertexShader = GlShader.createFromResource(GlShader.Type.VERTEX, this.getFileName(shaderPackPath) + ".vsh", vertexStream, "");
            GlShader fragmentShader = GlShader.createFromResource(GlShader.Type.FRAGMENT, this.getFileName(shaderPackPath) + ".fsh", fragmentStream, "");

            program = createShaders(fragmentShader, vertexShader);
            uniforms = new Uniforms(program);
            shadersUsed = true;
        }
            GlProgramManager.useProgram(program.getProgramRef());
            uniforms.update();
        }
    public abstract Path getFileName(Path shaderPackPath);
    public abstract GlProgram createShaders(GlShader fragment, GlShader vertex) throws IOException;
}
