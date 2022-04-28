package net.coderbot.iris.pipeline;

import net.coderbot.iris.gl.shader.ShaderType;

public interface Patcher {
	static Patcher INSTANCE = new TransformPatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();

	// static Logger LOGGER = LogManager.getLogger(TransformPatcher.class);

	public default String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		// LOGGER.debug("INPUT: " + source);
		String patched = patchAttributesInternal(source, type, hasGeometry);
		// LOGGER.debug("AGENT: " + getClass().getSimpleName() + " TYPE: " + type + " HAS_GEOMETRY: " + hasGeometry);
		// LOGGER.debug("PATCHED: " + patched);
		return patched;
	}

	public String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);
}
