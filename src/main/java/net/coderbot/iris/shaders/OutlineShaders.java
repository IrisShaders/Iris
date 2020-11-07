package net.coderbot.iris.shaders;

import net.coderbot.iris.shaderpack.ShaderPack;

import java.nio.file.Path;
import java.nio.file.Paths;

public class OutlineShaders extends Shader {




    @Override
    protected String getFileName() {
        return "gbuffers_basic";
    }

    @Override
    protected ShaderPack.ProgramSource getProgramSource(ShaderPack pack) {
        return pack.getGbuffersBasic();
    }

}
