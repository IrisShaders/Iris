package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.blending.AlphaTest;
import java.util.function.Supplier;

import org.apache.logging.log4j.*;

import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public interface Patcher {
	// static Patcher INSTANCE = new TriforcePatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();
	static Patcher INSTANCE = new TransformPatcher();

	static Logger LOGGER = LogManager.getLogger(Patcher.class);

	static String inspectPatch(String source, String patchInfo, Supplier<String> patcher) {
		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("INPUT: " + source);
		}
		String patched = patcher.get();
		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("PATCH INFO: " + patchInfo);
			LOGGER.debug("PATCHED: " + patched);
		}
		return patched;
	}

	 String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);

	 String patchVanillaInternal(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry);

	 String patchSodiumInternal(String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale);

	 String patchCompositeInternal(String source, ShaderType type);

	default String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> patchAttributesInternal(source, type, hasGeometry));
	}

	default String patchVanilla(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> patchVanilla(source, type, alpha, hasChunkOffset, inputs, hasGeometry));
	}

	default String patchSodium(
			String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type,
				() -> patchSodium(source, type, alpha, inputs, positionScale, positionOffset, textureScale));
	}

	default String patchComposite(String source, ShaderType type) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type,
				() -> patchComposite(source, type));
	}
}
