package net.coderbot.iris.pipeline.newshader;

import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public interface ShaderInstanceInterface {
	void iris$createGeometryShader(ResourceProvider factory, String name) throws IOException;
}
