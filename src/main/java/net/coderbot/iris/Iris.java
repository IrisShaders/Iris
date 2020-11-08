package net.coderbot.iris;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import net.coderbot.iris.pipeline.ShaderPipeline;
import net.coderbot.iris.shaderpack.ShaderPack;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	private static ShaderPack internal;
	private static ShaderPipeline pipeline;

	@Override
	public void onInitializeClient() {
		Path internalShaderpackPath = FabricLoader.getInstance().getModContainer("iris")
				.orElseThrow(() -> new RuntimeException("Iris doesn't exist???")).getRootPath();

		try {
			internal = new ShaderPack(internalShaderpackPath);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load internal shaderpack!", e);
		}
	}

	public static ShaderPipeline getPipeline() {
		if (pipeline == null) {
			pipeline = new ShaderPipeline(Objects.requireNonNull(internal));
		}

		return pipeline;
	}
}
