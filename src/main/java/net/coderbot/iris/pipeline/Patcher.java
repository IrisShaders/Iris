package net.coderbot.iris.pipeline;

import java.util.function.Supplier;

import org.apache.logging.log4j.*;

import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.shader.ShaderType;

public abstract class Patcher {
	public static Patcher INSTANCE = new TransformPatcher();
	// static Patcher INSTANCE = new AttributeShaderTransformer();

	static Logger LOGGER = LogManager.getLogger(Patcher.class);

	private String inspectPatch(String source, String patchInfo, Supplier<String> patcher) {
		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("INPUT: " + source + " END INPUT");
		}
		String patched = patcher.get();
		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("AGENT: " + getClass().getSimpleName() + " INFO: " + patchInfo);
			LOGGER.debug("PATCHED: " + patched + " END PATCHED");
		}
		return patched;
	}

	public final String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		return inspectPatch(source,
				"TYPE: " + type + " HAS_GEOMETRY: " + hasGeometry,
				() -> patchAttributesInternal(source, type, hasGeometry));
	}

	public final String patchSodiumTerrain(String source, ShaderType type) {
		return inspectPatch(source,
				"TYPE: " + type,
				() -> patchSodiumTerrainInternal(source, type));
	}

	protected abstract String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry);

	protected abstract String patchSodiumTerrainInternal(String source, ShaderType type);
}
