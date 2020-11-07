package net.coderbot.iris.shaders;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OutlineShaders extends Shader {
    OutlineShaders(Path shaderPackPath) {
        super(shaderPackPath);
    }



    @Override
    protected String getFileName() {
        return "gbuffers_basic";
    }

}
