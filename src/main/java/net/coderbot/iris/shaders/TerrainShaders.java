package net.coderbot.iris.shaders;

import net.coderbot.iris.shaderpack.ShaderPack;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TerrainShaders extends Shader {

    @Override
    protected String getFileName() {
        return "gbuffers_textured";
    }

    @Override
    protected ShaderPack.ProgramSource getProgramSource(ShaderPack pack) {
        return pack.getGbuffersTextured();
    }

}
