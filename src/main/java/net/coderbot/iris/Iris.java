package net.coderbot.iris;

import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.pipeline.ShaderPipeline;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class Iris implements ClientModInitializer {
	public static final String MODID = "iris";
	public static final Logger logger = LogManager.getLogger(MODID);
	private static ShaderPack currentPack;
	private static ShaderPipeline pipeline;
	private static IrisConfig irisConfig;

	@Override
	public void onInitializeClient() {
		irisConfig = new IrisConfig();
		try {
			irisConfig.createAndLoadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("Using shaderpack " + irisConfig.getShaderPackName());
		try {
			//optifine shaderpacks have all files in the shaders dir while internal iris shaders do not.
			currentPack = new ShaderPack(irisConfig.isInternal() ? irisConfig.getShaderPackPath() : irisConfig.getShaderPackPath().resolve("shaders"));
		} catch (IOException e) {
			logger.error(String.format("Failed to load shaderpack \"%s\"!", irisConfig.getShaderPackName()) + e);
			throw new RuntimeException(String.format("Failed to load shaderpack \"%s\"!", irisConfig.getShaderPackName()), e);
		}
	}

	public static ShaderPipeline getPipeline() {
		if (pipeline == null) {
			pipeline = new ShaderPipeline(Objects.requireNonNull(currentPack));
		}

		return pipeline;
	}

	public static IrisConfig getIrisConfig() {
		return irisConfig;
	}
}
