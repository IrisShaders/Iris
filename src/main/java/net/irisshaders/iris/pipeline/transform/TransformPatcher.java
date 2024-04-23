package net.irisshaders.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.Profile;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.VersionStatement;
import io.github.douira.glsl_transformer.ast.print.PrintType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.RootSupplier;
import io.github.douira.glsl_transformer.ast.transform.EnumASTTransformer;
import io.github.douira.glsl_transformer.ast.transform.TransformationException;
import io.github.douira.glsl_transformer.parser.ParsingException;
import io.github.douira.glsl_transformer.token_filter.ChannelFilter;
import io.github.douira.glsl_transformer.token_filter.TokenChannel;
import io.github.douira.glsl_transformer.token_filter.TokenFilter;
import io.github.douira.glsl_transformer.util.LRUCache;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.parameter.ComputeParameters;
import net.irisshaders.iris.pipeline.transform.parameter.Parameters;
import net.irisshaders.iris.pipeline.transform.parameter.SodiumParameters;
import net.irisshaders.iris.pipeline.transform.parameter.TextureStageParameters;
import net.irisshaders.iris.pipeline.transform.parameter.VanillaParameters;
import net.irisshaders.iris.pipeline.transform.transformer.CommonTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.CompatibilityTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.CompositeCoreTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.CompositeTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.DHTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.SodiumCoreTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.SodiumTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.TextureTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.VanillaCoreTransformer;
import net.irisshaders.iris.pipeline.transform.transformer.VanillaTransformer;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import org.antlr.v4.runtime.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The transform patcher (triforce 2) uses glsl-transformer's ASTTransformer to
 * do shader transformation.
 * <p>
 * The TransformPatcher does caching on the source string and associated
 * parameters. For this to work, all objects contained in a parameter must have
 * an equals method and they must never be changed after having been used for
 * patching. Since the cache also contains the source string, it doesn't need to
 * be disabled when developing shaderpacks. However, when changes are made to
 * the patcher, the cache should be disabled with {@link #useCache}.
 * <p>
 * NOTE: This patcher expects (and ensures) that the string doesn't contain any
 * (!) preprocessor directives. The only allowed ones are #extension and #pragma
 * as they are considered "parsed" directives. If any other directive appears in
 * the string, it will throw.
 */
public class TransformPatcher {
	private static final boolean useCache = true;
	private static final Map<CacheKey, Map<PatchShaderType, String>> cache = new LRUCache<>(400);
	private static final List<String> internalPrefixes = List.of("iris_", "irisMain", "moj_import");
	private static final Pattern versionPattern = Pattern.compile("^.*#version\\s+(\\d+)", Pattern.DOTALL);
	private static final EnumASTTransformer<Parameters, PatchShaderType> transformer;
	static Logger LOGGER = LogManager.getLogger(TransformPatcher.class);
	// TODO: Only do the NewLines patches if the source code isn't from
	// gbuffers_lines (what does this mean?)
	static TokenFilter<Parameters> parseTokenFilter = new ChannelFilter<>(TokenChannel.PREPROCESSOR) {
		@Override
		public boolean isTokenAllowed(Token token) {
			if (!super.isTokenAllowed(token)) {
				throw new IllegalArgumentException("Unparsed preprocessor directives such as '" + token.getText()
					+ "' may not be present at this stage of shader processing!");
			}
			return true;
		}
	};

	static {
		transformer = new EnumASTTransformer<>(PatchShaderType.class) {
			{
				setRootSupplier(RootSupplier.PREFIX_UNORDERED_ED_EXACT);
			}

			@Override
			public TranslationUnit parseTranslationUnit(Root rootInstance, String input) {
				// parse #version directive using an efficient regex before parsing so that the
				// parser can be set to the correct version
				Matcher matcher = versionPattern.matcher(input);
				if (!matcher.find()) {
					throw new IllegalArgumentException(
						"No #version directive found in source code! See debugging.md for more information.");
				}
				transformer.getLexer().version = Version.fromNumber(Integer.parseInt(matcher.group(1)));

				return super.parseTranslationUnit(rootInstance, input);
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
				internalPrefixes.stream()
					.flatMap(root.getPrefixIdentifierIndex()::prefixQueryFlat)
					.findAny()
					.ifPresent(id -> {
						throw new IllegalArgumentException(
							"Detected a potential reference to unstable and internal Iris shader interfaces (iris_, irisMain and moj_import). This isn't currently supported. Violation: "
								+ id.getName() + ". See debugging.md for more information.");
					});

				root.indexBuildSession(() -> {
					VersionStatement versionStatement = tree.getVersionStatement();
					if (versionStatement == null) {
						throw new IllegalStateException("Missing the version statement!");
					}
					Profile profile = versionStatement.profile;
					Version version = versionStatement.version;
					if (Objects.requireNonNull(parameters.patch) == Patch.COMPUTE) {// we can assume the version is at least 400 because it's a compute shader
						versionStatement.profile = Profile.CORE;
						CommonTransformer.transform(transformer, tree, root, parameters, true);
					} else {// handling of Optifine's special core profile mode
						boolean isLine = (parameters.patch == Patch.VANILLA && ((VanillaParameters) parameters).isLines());

						if (profile == Profile.CORE || version.number >= 150 && profile == null || isLine) {
							// patch the version number to at least 330
							if (version.number < 330) {
								versionStatement.version = Version.GLSL33;
							}

							switch (parameters.patch) {
								case COMPOSITE:
									CompositeCoreTransformer.transform(transformer, tree, root, parameters);
									break;
								case SODIUM:
									SodiumParameters sodiumParameters = (SodiumParameters) parameters;
									SodiumCoreTransformer.transform(transformer, tree, root, sodiumParameters);
									break;
								case VANILLA:
									VanillaCoreTransformer.transform(transformer, tree, root, (VanillaParameters) parameters);
									break;
								default:
									throw new UnsupportedOperationException("Unknown patch type: " + parameters.patch);
							}

							if (parameters.type == PatchShaderType.FRAGMENT) {
								CompatibilityTransformer.transformFragmentCore(transformer, tree, root, parameters);
							}
						} else {
							// patch the version number to at least 330
							if (version.number < 330) {
								versionStatement.version = Version.GLSL33;
							}
							versionStatement.profile = Profile.CORE;

							switch (parameters.patch) {
								case COMPOSITE:
									CompositeTransformer.transform(transformer, tree, root, parameters);
									break;
								case SODIUM:
									SodiumParameters sodiumParameters = (SodiumParameters) parameters;
									SodiumTransformer.transform(transformer, tree, root, sodiumParameters);
									break;
								case VANILLA:
									VanillaTransformer.transform(transformer, tree, root, (VanillaParameters) parameters);
									break;
								case DH:
									DHTransformer.transform(transformer, tree, root, parameters);
									break;
								default:
									throw new UnsupportedOperationException("Unknown patch type: " + parameters.patch);
							}
						}
					}
					TextureTransformer.transform(transformer, tree, root,
						parameters.getTextureStage(), parameters.getTextureMap());
					CompatibilityTransformer.transformEach(transformer, tree, root, parameters);
				});
			}

			// the compatibility transformer does a grouped transformation
			CompatibilityTransformer.transformGrouped(transformer, trees, parameters);
		});
		transformer.setTokenFilter(parseTokenFilter);
	}

	private static Map<PatchShaderType, String> transformInternal(
		String name,
		Map<PatchShaderType, String> inputs,
		Parameters parameters) {
		try {
			return transformer.transform(inputs, parameters);
		} catch (TransformationException | ParsingException | IllegalStateException | IllegalArgumentException e) {
			// print the offending programs and rethrow to stop the loading process
			ShaderPrinter.printProgram("errored_" + name).addSources(inputs).print();
			throw new ShaderCompileException(name, e);
		}
	}

	private static Map<PatchShaderType, String> transform(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment,
														  Parameters parameters) {
		// stop if all are null
		if (vertex == null && geometry == null && tessControl == null && tessEval == null && fragment == null) {
			return null;
		}

		// check if this has been cached
		CacheKey key;
		Map<PatchShaderType, String> result = null;
		if (useCache) {
			key = new CacheKey(parameters, vertex, geometry, tessControl, tessEval, fragment);
			if (cache.containsKey(key)) {
				result = cache.get(key);
			}
		}

		// if there is no cache result, transform the shaders
		if (result == null) {
			transformer.setPrintType(Iris.getIrisConfig().areDebugOptionsEnabled() ? PrintType.INDENTED : PrintType.SIMPLE);
			EnumMap<PatchShaderType, String> inputs = new EnumMap<>(PatchShaderType.class);
			inputs.put(PatchShaderType.VERTEX, vertex);
			inputs.put(PatchShaderType.GEOMETRY, geometry);
			inputs.put(PatchShaderType.TESS_CONTROL, tessControl);
			inputs.put(PatchShaderType.TESS_EVAL, tessEval);
			inputs.put(PatchShaderType.FRAGMENT, fragment);

			result = transformInternal(name, inputs, parameters);
			if (useCache) {
				cache.put(key, result);
			}
		}
		return result;
	}

	private static Map<PatchShaderType, String> transformCompute(String name, String compute, Parameters parameters) {
		// stop if all are null
		if (compute == null) {
			return null;
		}

		// check if this has been cached
		CacheKey key;
		Map<PatchShaderType, String> result = null;
		if (useCache) {
			key = new CacheKey(parameters, compute);
			if (cache.containsKey(key)) {
				result = cache.get(key);
			}
		}

		// if there is no cache result, transform the shaders
		if (result == null) {
			transformer.setPrintType(Iris.getIrisConfig().areDebugOptionsEnabled() ? PrintType.INDENTED : PrintType.SIMPLE);
			EnumMap<PatchShaderType, String> inputs = new EnumMap<>(PatchShaderType.class);
			inputs.put(PatchShaderType.COMPUTE, compute);

			result = transformInternal(name, inputs, parameters);
			if (useCache) {
				cache.put(key, result);
			}
		}
		return result;
	}

	public static Map<PatchShaderType, String> patchVanilla(
		String name, String vertex, String geometry, String tessControl, String tessEval, String fragment,
		AlphaTest alpha, boolean isLines,
		boolean hasChunkOffset,
		ShaderAttributeInputs inputs,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		return transform(name, vertex, geometry, tessControl, tessEval, fragment,
			new VanillaParameters(Patch.VANILLA, textureMap, alpha, isLines, hasChunkOffset, inputs, geometry != null, tessControl != null || tessEval != null));
	}


	public static Map<PatchShaderType, String> patchDH(
		String name, String vertex, String tessControl, String tessEval, String geometry, String fragment,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		return transform(name, vertex, geometry, tessControl, tessEval, fragment,
			new Parameters(Patch.DH, textureMap) {
				@Override
				public TextureStage getTextureStage() {
					return TextureStage.GBUFFERS_AND_SHADOW;
				}
			});
	}

	public static Map<PatchShaderType, String> patchSodium(String name, String vertex, String geometry, String tessControl, String tessEval, String fragment,
														   AlphaTest alpha, ShaderAttributeInputs inputs,
														   Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		return transform(name, vertex, geometry, tessControl, tessEval, fragment,
			new SodiumParameters(Patch.SODIUM, textureMap, alpha, inputs));
	}

	public static Map<PatchShaderType, String> patchComposite(
		String name, String vertex, String geometry, String fragment,
		TextureStage stage,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		return transform(name, vertex, geometry, null, null, fragment, new TextureStageParameters(Patch.COMPOSITE, stage, textureMap));
	}

	public static String patchCompute(
		String name, String compute,
		TextureStage stage,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		return transformCompute(name, compute, new ComputeParameters(Patch.COMPUTE, stage, textureMap))
			.getOrDefault(PatchShaderType.COMPUTE, null);
	}

	private static class CacheKey {
		final Parameters parameters;
		final String vertex;
		final String geometry;
		final String tessControl;
		final String tessEval;
		final String fragment;
		final String compute;

		public CacheKey(Parameters parameters, String vertex, String geometry, String tessControl, String tessEval, String fragment) {
			this.parameters = parameters;
			this.vertex = vertex;
			this.geometry = geometry;
			this.tessControl = tessControl;
			this.tessEval = tessEval;
			this.fragment = fragment;
			this.compute = null;
		}

		public CacheKey(Parameters parameters, String compute) {
			this.parameters = parameters;
			this.vertex = null;
			this.geometry = null;
			this.tessControl = null;
			this.tessEval = null;
			this.fragment = null;
			this.compute = compute;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
			result = prime * result + ((vertex == null) ? 0 : vertex.hashCode());
			result = prime * result + ((geometry == null) ? 0 : geometry.hashCode());
			result = prime * result + ((tessControl == null) ? 0 : tessControl.hashCode());
			result = prime * result + ((tessEval == null) ? 0 : tessEval.hashCode());
			result = prime * result + ((fragment == null) ? 0 : fragment.hashCode());
			result = prime * result + ((compute == null) ? 0 : compute.hashCode());
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
			if (geometry == null) {
				if (other.geometry != null)
					return false;
			} else if (!geometry.equals(other.geometry))
				return false;
			if (tessControl == null) {
				if (other.tessControl != null)
					return false;
			} else if (!tessControl.equals(other.tessControl))
				return false;
			if (tessEval == null) {
				if (other.tessEval != null)
					return false;
			} else if (!tessEval.equals(other.tessEval))
				return false;
			if (fragment == null) {
				if (other.fragment != null)
					return false;
			} else if (!fragment.equals(other.fragment))
				return false;
			if (compute == null) {
				return other.compute == null;
			} else return compute.equals(other.compute);
		}
	}
}
