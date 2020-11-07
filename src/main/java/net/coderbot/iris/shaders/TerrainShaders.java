package net.coderbot.iris.shaders;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TerrainShaders extends Shader {

    TerrainShaders(Path shaderPackPath) {
        super(shaderPackPath);
    }


    @Override
    protected String getFileName() {
        return "gbuffers_textured";
    }

}
