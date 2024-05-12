package net.irisshaders.iris.pipeline.programs;

import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;

public interface ShaderInstanceInterface {
	void iris$createExtraShaders(ResourceProvider factory, String name) throws IOException;
}
