package net.coderbot.iris.pipeline.transform;

import java.util.function.Supplier;

import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.SemanticException;
import io.github.douira.glsl_transformer.core.target.ThrowTargetImpl;
import io.github.douira.glsl_transformer.print.filter.ChannelFilter;
import io.github.douira.glsl_transformer.print.filter.TokenChannel;
import io.github.douira.glsl_transformer.print.filter.TokenFilter;
import io.github.douira.glsl_transformer.transform.LifecycleUser;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.TransformationManager;
import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.patcher.AttributeShaderTransformer;

/**
 * The transform patcher (triforce 2) uses glsl-transformer to do shader
 * transformation.
 * 
 * NOTE: This patcher expects (and ensures) that the string doesn't contain any
 * (!) preprocessor directives. The only allowed ones are #extension and #pragma
 * as they are considered "parsed" directives. If any other directive appears in
 * the string, it will throw.
 * 
 * TODO: JCPP has to be configured to remove preprocessor directives entirely
 */
public class TransformPatcher {
	static Logger LOGGER = LogManager.getLogger(TransformPatcher.class);
	private static TransformationManager<Parameters> manager;

	/**
	 * PREV TODO: Only do the NewLines patches if the source code isn't from
	 * gbuffers_lines
	 */

	static TokenFilter<Parameters> parseTokenFilter = new ChannelFilter<Parameters>(TokenChannel.PREPROCESSOR) {
		@Override
		public boolean isTokenAllowed(Token token) {
			if (!super.isTokenAllowed(token)) {
				throw new SemanticException("Unparsed preprocessor directives such as '" + token.getText()
						+ "' may not be present at this stage of shader processing!");
			}
			return true;
		}
	};

	// setup the transformations and even loose phases if necessary
	static LifecycleUser<Parameters> detectReserved = new SearchTerminals<Parameters>()
			.singleTarget(
					new ThrowTargetImpl<Parameters>(
							"iris_",
							"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))
			.requireFullMatch(false);

	static {

		LifecycleUser<Parameters> sodiumTerrainTransformation = new SodiumTerrainTransformation();
		LifecycleUser<Parameters> attributeTransformation = new AttributeTransformation();

		manager = new TransformationManager<Parameters>(new Transformation<Parameters>() {
			@Override
			protected void setupGraph() {
				Patch patch = getJobParameters().patch;

				addEndDependent(detectReserved);

				if (patch == Patch.ATTRIBUTES) {
					addEndDependent(attributeTransformation);
				}

				if (patch == Patch.SODIUM_TERRAIN) {
					addEndDependent(sodiumTerrainTransformation);
				}
			}
		});

		manager.setParseTokenFilter(parseTokenFilter);
	}

	private static String inspectPatch(String source, String patchInfo, Supplier<String> patcher) {
		if (source == null) {
			return null;
		}

		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("INPUT: " + source + " END INPUT");
		}

		long time = System.currentTimeMillis();
		String patched = patcher.get();

		if (IrisLogging.ENABLE_SPAM) {
			LOGGER.debug("INFO: " + patchInfo);
			LOGGER.debug("TIME: patching took " + (System.currentTimeMillis() - time) + "ms");
			LOGGER.debug("PATCHED: " + patched + " END PATCHED");
		}
		return patched;
	}

	private static String transform(String source, Parameters parameters) {
		return manager.transform(source, parameters);
	}

	public static String patchAttributes(String source, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
		return inspectPatch(source,
				"TYPE: " + type + " HAS_GEOMETRY: " + hasGeometry,
				// routing through original patcher until changes to AttributeShaderTransformer
				// can be caught up in TransformPatcher
				() -> AttributeShaderTransformer.patch(source, type, hasGeometry, inputs));
		// () -> transform(source, new AttributeParameters(Patch.ATTRIBUTES, type,
		// hasGeometry, inputs)));
	}

	public static String patchSodiumTerrain(String source, ShaderType type) {
		return inspectPatch(source,
				"TYPE: " + type,
				() -> transform(source, new Parameters(Patch.SODIUM_TERRAIN, type)));
		// () -> type == ShaderType.VERTEX
		// ? SodiumTerrainPipeline.transformVertexShader(source)
		// : SodiumTerrainPipeline.transformFragmentShader(source));
	}
}
