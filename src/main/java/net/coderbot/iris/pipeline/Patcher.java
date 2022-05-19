package net.coderbot.iris.pipeline;

import java.util.function.Supplier;

import org.apache.logging.log4j.*;

import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gl.shader.ShaderType;

public abstract class Patcher {
	public static Patcher INSTANCE = new TransformPatcher();

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

	public final String patchExample(String source, ShaderType type, Object thing) {
		return inspectPatch(source,
				"TYPE: " + type + " THING: " + thing,
				() -> patchExampleInternal(source, type,thing ));
	}

	protected abstract String patchExampleInternal(String source, ShaderType type, Object thing);
}
