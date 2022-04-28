package net.coderbot.iris.pipeline;

import java.util.function.Supplier;

import org.apache.logging.log4j.*;

import net.coderbot.iris.gl.shader.ShaderType;

public interface Patcher {
	static boolean INSPECT_PATCH = false;
	
	static Patcher INSTANCE = new TransformPatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();

	static Logger LOGGER = LogManager.getLogger(Patcher.class);

	static String inspectPatch(String source, String patchInfo, Supplier<String> patcher) {
		if (INSPECT_PATCH) {
			LOGGER.debug("INPUT: " + source);
		}
		String patched = patcher.get();
		if (INSPECT_PATCH) {
			LOGGER.debug("PATCH INFO: " + patchInfo);
			LOGGER.debug("PATCHED: " + patched);
		}
		return patched;
	}

	default String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		return inspectPatch(source,
				"AGENT: " + getClass().getSimpleName() + " TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> patchAttributesInternal(source, type, hasGeometry));
	}

	String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);
}
