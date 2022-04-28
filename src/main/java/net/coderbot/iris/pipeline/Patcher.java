package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public interface Patcher {
	// static Patcher INSTANCE = new TriforcePatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();
	static Patcher INSTANCE = new TransformPatcher();

	// static Logger LOGGER = LogManager.getLogger(TransformPatcher.class);

	public default String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		// LOGGER.debug("INPUT: " + source);
		String patched = patchAttributesInternal(source, type, hasGeometry);
		// LOGGER.debug("AGENT: " + getClass().getSimpleName() + " TYPE: " + type + " HAS_GEOMETRY: " + hasGeometry);
		// LOGGER.debug("PATCHED: " + patched);
		return patched;
	}

	public String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);

	public String patchVanilla(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry);

	public String patchSodium(String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale);

	public String patchComposite(String source, ShaderType type);
}
