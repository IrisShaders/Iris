package net.coderbot.iris.shaders;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudShaders extends Shader {
    CloudShaders(Path shaderPackPath) {
        super(shaderPackPath);
    }


    @Override
    protected String getFileName() {
        return "gbuffers_clouds";
    }

}
