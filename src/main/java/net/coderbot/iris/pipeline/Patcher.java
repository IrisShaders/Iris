package net.coderbot.iris.pipeline;

import java.util.function.Supplier;

import org.apache.logging.log4j.*;

import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public abstract class Patcher {
	public static Patcher INSTANCE = new TransformPatcher();
	// static Patcher INSTANCE = new TriforcePatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();

	static Logger LOGGER = LogManager.getLogger(Patcher.class);

	protected abstract String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);

	protected abstract String patchVanillaInternal(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry);

	protected abstract String patchSodiumInternal(String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale);

	protected abstract String patchCompositeInternal(String source, ShaderType type);

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

	public final String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> patchAttributesInternal(source, type, hasGeometry));
	}

	public final String patchVanilla(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> patchVanilla(source, type, alpha, hasChunkOffset, inputs, hasGeometry));
	}

	public final String patchSodium(
			String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type,
				() -> patchSodium(source, type, alpha, inputs, positionScale, positionOffset, textureScale));
	}

	public final String patchComposite(String source, ShaderType type) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type,
				() -> patchComposite(source, type));
	}
}
