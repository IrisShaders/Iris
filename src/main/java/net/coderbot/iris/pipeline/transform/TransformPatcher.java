package net.coderbot.iris.pipeline.transform;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.transform.EnumASTTransformer;
import io.github.douira.glsl_transformer.cst.core.SemanticException;
import io.github.douira.glsl_transformer.cst.token_filter.ChannelFilter;
import io.github.douira.glsl_transformer.cst.token_filter.TokenChannel;
import io.github.douira.glsl_transformer.cst.token_filter.TokenFilter;
import io.github.douira.glsl_transformer.util.LRUCache;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.pipeline.PatchedShaderPrinter;

/**
 * The transform patcher (triforce 2) uses glsl-transformer's ASTTransformer to
 * do shader transformation.
 * 
 * The TransformPatcher does caching on the source string and associated
 * parameters. For this to work, all objects contained in a parameter must have
 * an equals method and they must never be changed after having been used for
 * patching. Since the cache also contains the source string, it doesn't need to
 * be disabled when developing shaderpacks. However, when changes are made to
 * the patcher, the cache should be disabled with {@link #useCache}.
 *
 * NOTE: This patcher expects (and ensures) that the string doesn't contain any
 * (!) preprocessor directives. The only allowed ones are #extension and #pragma
 * as they are considered "parsed" directives. If any other directive appears in
 * the string, it will throw.
 */
public class TransformPatcher {
	static Logger LOGGER = LogManager.getLogger(TransformPatcher.class);
	private static EnumASTTransformer<Parameters, PatchShaderType> transformer;
	private static final boolean useCache = true;
	private static final Map<CacheKey, Map<PatchShaderType, String>> cache =  new LRUCache<>(400);

	private static class CacheKey {
		final Parameters parameters;
		final String vertex;
		final String geometry;
		final String fragment;

		public CacheKey(Parameters parameters, String vertex, String geometry, String fragment) {
			this.parameters = parameters;
			this.vertex = vertex;
			this.geometry = geometry;
			this.fragment = fragment;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fragment == null) ? 0 : fragment.hashCode());
			result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
			result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
			result = prime * result + ((vertex == null) ? 0 : vertex.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheKey other = (CacheKey) obj;
			if (fragment == null) {
				if (other.fragment != null)
					return false;
			} else if (!fragment.equals(other.fragment))
				return false;
			if (geometry == null) {
				if (other.geometry != null)
					return false;
			} else if (!geometry.equals(other.geometry))
				return false;
			if (parameters == null) {
				if (other.parameters != null)
					return false;
			} else if (!parameters.equals(other.parameters))
				return false;
			if (vertex == null) {
				if (other.vertex != null)
					return false;
			} else if (!vertex.equals(other.vertex))
				return false;
			return true;
		}
	}

	// TODO: Only do the NewLines patches if the source code isn't from
	// gbuffers_lines (what does this mean?)

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

	static {
		transformer = new EnumASTTransformer<Parameters, PatchShaderType>(PatchShaderType.class) {
			@Override
			public TranslationUnit parseTranslationUnit(String input) throws RecognitionException {
				// parse #version directive using an efficient regex before parsing so that the
				// parser can be set to the correct version
				Matcher matcher = versionPattern.matcher(input);
				if (!matcher.find()) {
					throw new IllegalArgumentException("No #version directive found in source code! See debugging.md for more information.");
				}
				Version version = Version.fromNumber(Integer.parseInt(matcher.group(1)));
				transformer.getLexer().version = version;

				return super.parseTranslationUnit(input);
			}
		};
		transformer.setTransformation((trees, parameters) -> {
			for (PatchShaderType type : PatchShaderType.values()) {
				TranslationUnit tree = trees.get(type);
				if (tree == null) {
					continue;
				}
				tree.outputOptions.enablePrintInfo();

				parameters.type = type;
				Root root = tree.getRoot();

				// check for illegal references to internal Iris shader interfaces
				Optional<Identifier> violation = root.identifierIndex.prefixQueryFlat("iris_").findAny();
				if (!violation.isPresent()) {
					violation = root.identifierIndex.prefixQueryFlat("irisMain").findAny();
				}
				violation.ifPresent(id -> {
					throw new SemanticException(
							"Detected a potential reference to unstable and internal Iris shader interfaces (iris_ and irisMain). This isn't currently supported. Violation: "
									+ id.getName() + ". See debugging.md for more information.");
				});

				Root.indexBuildSession(tree, () -> {
					switch (parameters.patch) {
						case ATTRIBUTES:
							AttributeTransformer.transform(transformer, tree, root, (AttributeParameters) parameters);
							break;
						case SODIUM_TERRAIN:
							SodiumTerrainTransformer.transform(transformer, tree, root, parameters);
							break;
						case COMPOSITE:
							CompositeTransformer.transform(transformer, tree, root);
							break;
					}
					CompatibilityTransformer.transformEach(transformer, tree, root, parameters);
				});
			}

			// the compatibility transformer does a grouped transformation
			CompatibilityTransformer.transformGrouped(transformer, trees, parameters);
		});
		transformer.setParseTokenFilter(parseTokenFilter);
	}

	private static final Pattern versionPattern = Pattern.compile("^.*#version\\s+(\\d+)", Pattern.DOTALL);

	private static Map<PatchShaderType, String> transform(String vertex, String geometry, String fragment, Parameters parameters) {
		// stop if all are null
		if (vertex == null && geometry == null && fragment == null) {
			return null;
		}

		// check if this has been cached
		CacheKey key;
		Map<PatchShaderType, String> result = null;
		if (useCache) {
			key = new CacheKey(parameters, vertex, geometry, fragment);
			if (cache.containsKey(key)) {
				result = cache.get(key);
			}
		}

		// if there is no cache result, transform the shaders
		if (result == null) {
			transformer.setPrintType(PatchedShaderPrinter.prettyPrintShaders ? PrintType.INDENTED : PrintType.SIMPLE);
			EnumMap<PatchShaderType, String> inputs = new EnumMap<>(PatchShaderType.class);
			inputs.put(PatchShaderType.VERTEX, vertex);
			inputs.put(PatchShaderType.GEOMETRY, geometry);
			inputs.put(PatchShaderType.FRAGMENT, fragment);
			result = transformer.transform(inputs, parameters);
			if (useCache) {
				cache.put(key, result);
			}
		}

		return result;
	}

	public static Map<PatchShaderType, String> patchAttributes(String vertex, String geometry, String fragment, InputAvailability inputs) {
		return transform(vertex, geometry, fragment, new AttributeParameters(Patch.ATTRIBUTES, geometry != null, inputs));
	}

	public static Map<PatchShaderType, String> patchSodiumTerrain(String vertex, String geometry, String fragment) {
		return transform(vertex, geometry, fragment, new Parameters(Patch.SODIUM_TERRAIN));
	}

	public static Map<PatchShaderType, String> patchComposite(String vertex, String geometry, String fragment) {
		return transform(vertex, geometry, fragment, new Parameters(Patch.COMPOSITE));
	}
}
