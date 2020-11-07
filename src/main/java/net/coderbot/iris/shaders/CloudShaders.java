package net.coderbot.iris.shaders;

import net.coderbot.iris.shaderpack.ShaderPack;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudShaders extends Shader {

    @Override
    protected String getFileName() {
        return "gbuffers_clouds";
    }

    @Override
    protected ShaderPack.ProgramSource getProgramSource(ShaderPack pack) {
        return pack.getGbuffersClouds();
    }

}
