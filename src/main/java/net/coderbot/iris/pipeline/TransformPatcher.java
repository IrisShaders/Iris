package net.coderbot.iris.pipeline;

import java.util.Collection;
import java.util.function.*;
import java.util.stream.*;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.pattern.*;
import org.apache.logging.log4j.*;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.*;
import io.github.douira.glsl_transformer.ast.StringNode;
import io.github.douira.glsl_transformer.core.*;
import io.github.douira.glsl_transformer.core.target.*;
import io.github.douira.glsl_transformer.print.filter.*;
import io.github.douira.glsl_transformer.transform.*;
import io.github.douira.glsl_transformer.tree.ExtendedContext;
import io.github.douira.glsl_transformer.util.CompatUtil;
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

	private static enum Patch {
		ATTRIBUTES, SODIUM_TERRAIN
	}

	private static class Parameters extends JobParameters {
		public final Patch patch;
		public final ShaderType type;

		public Parameters(Patch patch, ShaderType type) {
			this.patch = patch;
			this.type = type;
		}

		@Override
		public boolean equals(JobParameters other) {
			if (other instanceof Parameters) {
				Parameters otherParams = (Parameters) other;
				return otherParams.patch == patch && otherParams.type == type;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return patch.hashCode() ^ type.hashCode();
		}
	}

	private static class AttributeParameters extends Parameters {
		public final boolean hasGeometry;
		public final InputAvailability inputs;

		public AttributeParameters(Patch patch, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
			super(patch, type);
			this.hasGeometry = hasGeometry;
			this.inputs = inputs;
		}
	}

	private static abstract class MainWrapperDynamic<R extends Parameters> extends WrapIdentifier<R> {
		protected abstract String getMainContent();

		@Override
		protected Collection<String> getDetectionResults() {
			return CompatUtil.listOf("irisMain");
		}

		@Override
		protected Collection<String> getInjectionExternalDeclarations() {
			return CompatUtil.listOf("void main() { " + getMainContent() + "\nirisMain(); }");
		}

		@Override
		protected String getWrapTarget() {
			return "main";
		}

		@Override
		protected InjectionPoint getInjectionLocation() {
			return InjectionPoint.BEFORE_EOF;
		}
	}

	static {
		/**
		 * PREV TODO: Only do the NewLines patches if the source code isn't from
		 * gbuffers_lines
		 */

		TokenFilter<Parameters> parseTokenFilter = new ChannelFilter<Parameters>(TokenChannel.PREPROCESSOR) {
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
		LifecycleUser<Parameters> detectReserved = new SearchTerminals<Parameters>()
				.singleTarget(
						new ThrowTargetImpl<Parameters>(
								"iris_",
								"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))
				.requireFullMatch(false);

		// #region patchAttributes
		LifecycleUser<Parameters> replaceEntityColorDeclaration = new Transformation<Parameters>() {
			@Override
			protected void setupGraph() {
				addEndDependent(new WalkPhase<Parameters>() {
					ParseTreePattern entityColorPattern;

					@Override
					public void init() {
						entityColorPattern = compilePattern(
								"uniform vec4 entityColor;",
								GLSLParser.RULE_externalDeclaration);
					}

					@Override
					public void enterExternalDeclaration(ExternalDeclarationContext ctx) {
						ParseTreeMatch match = entityColorPattern.match(ctx);
						if (match.succeeded()) {
							removeNode(ctx);
						}
					}
				});

				if (getJobParameters().type == ShaderType.GEOMETRY) {
					chainDependent(new SearchTerminals<Parameters>().singleTarget(
							new ParsedReplaceTargetImpl<>("entityColor", "entityColor[0]", GLSLParser::expression)));
				}

				chainDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						switch (getJobParameters().type) {
							case VERTEX:
								injectExternalDeclarations(InjectionPoint.BEFORE_DECLARATIONS,
										"uniform sampler2D iris_overlay;",
										"varying vec4 entityColor;");
								break;
							case GEOMETRY:
								injectExternalDeclarations(InjectionPoint.BEFORE_DECLARATIONS,
										"out vec4 entityColorGS;",
										"in vec4 entityColor[];");
								break;
							case FRAGMENT:
								injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "varying vec4 entityColor;");
								break;
						}
					}
				});
			}
		};

		LifecycleUser<Parameters> wrapOverlay = new MainWrapperDynamic<Parameters>() {
			@Override
			protected String getMainContent() {
				return getJobParameters().type == ShaderType.VERTEX
						? "vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
								"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);"
						: "entityColorGS = entityColor[0];";
			}
		};

		LifecycleUser<Parameters> renameEntityColorFragment = new SearchTerminals<Parameters>() {
			@Override
			public boolean isActive() {
				return ((AttributeParameters) getJobParameters()).hasGeometry;
			}
		}.singleTarget(new TerminalReplaceTargetImpl<>("entityColor", "entityColorGS"));

		// #endregion patchAttributes

		// #region patchSodiumTerrain
		// see SodiumTerrainPipeline for the original patcher
		LifecycleUser<Parameters> wrapFTransform = new WrapIdentifier<Parameters>()
				.wrapTarget("fTransform")
				.detectionResult("iris_ftransform")
				.injectionLocation(InjectionPoint.BEFORE_FUNCTIONS)
				.injectionExternalDeclaration("vec4 iris_ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

		LifecycleUser<Parameters> wrapVertex = new WrapIdentifier<Parameters>() {
			@Override
			protected ActivatableLifecycleUser<Parameters> getWrapResultDetector() {
				return new SearchTerminals<Parameters>()
						.targets(
								Stream.of("a_Pos", "u_ModelScale", "d_ModelOffset")
										.map(WrapThrowTargetImpl<Parameters>::new)
										.collect(Collectors.toList()));
			}

			@Override
			protected ActivatableLifecycleUser<Parameters> getInjector() {
				return RunPhase.withInjectExternalDeclarations(injectionLocation(),
						"attribute vec3 a_Pos;",
						"uniform vec3 u_ModelScale;",
						"attribute vec4 d_ModelOffset;");
			}
		}
				.wrapTarget("gl_Vertex")
				.parsedReplacement("vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS);

		LifecycleUser<Parameters> wrapMultiTexCoord = new WrapIdentifier<Parameters>() {
			@Override
			protected ActivatableLifecycleUser<Parameters> getWrapResultDetector() {
				return new SearchTerminals<Parameters>()
						.targets(
								Stream.of("a_TexCoord", "u_TextureScale")
										.map((detect) -> new WrapThrowTargetImpl<Parameters>(detect))
										.collect(Collectors.toList()));
			}

			@Override
			protected ActivatableLifecycleUser<Parameters> getInjector() {
				return RunPhase.withInjectExternalDeclarations(injectionLocation(),
						"uniform vec2 u_TextureScale;",
						"attribute vec2 a_TexCoord;");
			}
		}
				.wrapTarget("gl_MultiTexCoord0")
				.parsedReplacement("vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS);

		LifecycleUser<Parameters> wrapColor = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_Color")
				.detectionResult("a_Color")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("attribute vec4 a_Color;");

		LifecycleUser<Parameters> wrapNormal = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_Normal")
				.detectionResult("a_Normal")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("attribute vec3 a_Normal;");

		LifecycleUser<Parameters> wrapModelViewMatrix = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_ModelViewMatrix")
				.detectionResult("u_ModelViewMatrix")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("uniform mat4 u_ModelViewMatrix;");

		LifecycleUser<Parameters> wrapModelViewProjectionMatrix = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_ModelViewProjectionMatrix")
				.detectionResult("u_ModelViewProjectionMatrix")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("uniform mat4 u_ModelViewProjectionMatrix;");

		LifecycleUser<Parameters> wrapNormalMatrix = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_NormalMatrix")
				.detectionResult("u_NormalMatrix")
				.parsedReplacement("mat3(u_NormalMatrix)")
				.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("uniform mat4 u_NormalMatrix;");

		LifecycleUser<Parameters> replaceTextureMatrix0 = new Transformation<Parameters>() {
			{
				addEndDependent(new WalkPhase<Parameters>() {
					ParseTreePattern textureMatrixPattern;

					@Override
					public void init() {
						textureMatrixPattern = compilePattern("gl_TextureMatrix[0]", GLSLParser.RULE_expression);
					}

					@Override
					public void enterArrayAccessExpression(ArrayAccessExpressionContext ctx) {
						ParseTreeMatch match = textureMatrixPattern.match(ctx);
						if (match.succeeded()) {
							replaceNode(ctx, "mat4(1.0)", GLSLParser::expression);
						}
					}
				});
			}
		};

		/**
		 * Implements BuiltinUniformReplacementTransformer and does a little more. Note
		 * that the main walk phase uses the fact that the order of invocation is
		 * enterMemberAccessExpression, enterMultiplicativeExpression,
		 * enterArrayAccessExpression in the targeted expression.
		 */
		LifecycleUser<Parameters> replaceBuiltinUniforms = new Transformation<Parameters>() {
			static final String lightmapCoordsExpression = "a_LightCoord";
			static final String irisLightmapTexMat = "iris_LightmapTextureMatrix";
			static final String texCoordFallbackReplacement = "vec4(" + lightmapCoordsExpression + " * 255.0, 0.0, 1.0)";

			boolean needsLightmapTexMatInjection;

			@Override
			public void resetState() {
				needsLightmapTexMatInjection = false;
			}

			{
				// make sure the lightmap coords expression doesn't exist in the code yet
				addEndDependent(new SearchTerminals<Parameters>()
						.addTarget(new WrapThrowTargetImpl<>(lightmapCoordsExpression))
						.addTarget(new WrapThrowTargetImpl<>(irisLightmapTexMat)));

				// find accesses to gl_TextureMatrix[1] or gl_TextureMatrix[2] in combination
				// with gl_MultiTexCoord1 or gl_MultiTexCoord2 and replace them with
				// lightmapCoordsExpression or a vector wrapper depending on the context
				chainDependent(new WalkPhase<Parameters>() {
					ParseTreePattern accessPattern;
					ParseTreePattern bareMultPattern;
					ParseTreePattern extraPattern;
					ParseTreePattern textureMatrixPattern;

					private void checkPatternMatch(ParseTreePattern pattern, ExtendedContext ctx,
							Consumer<ParseTreeMatch> action) {
						ParseTreeMatch match = pattern.match(ctx);
						if (match.succeeded()) {
							String texCoord = match.get("texCoord").getText();
							String texMatrixIndex = match.get("texMatrixIndex").getText();
							if ((texCoord.equals("gl_MultiTexCoord1") || texCoord.equals("gl_MultiTexCoord2"))
									&& (texMatrixIndex.equals("1") || texMatrixIndex.equals("2"))) {
								action.accept(match);
							}
						}
					}

					@Override
					public void init() {
						accessPattern = compilePattern(
								"(gl_TextureMatrix[<texMatrixIndex:expression>] * <texCoord:IDENTIFIER>).<member:IDENTIFIER>",
								GLSLParser.RULE_expression);
						bareMultPattern = compilePattern(
								"gl_TextureMatrix[<texMatrixIndex:expression>] * <texCoord:IDENTIFIER>",
								GLSLParser.RULE_expression);
						extraPattern = compilePattern("<texCoord:IDENTIFIER>.xy / 255.0", GLSLParser.RULE_expression);
						textureMatrixPattern = compilePattern("gl_TextureMatrix[<texMatrixIndex:expression>]",
								GLSLParser.RULE_expression);
					}

					@Override
					public void enterMemberAccessExpression(MemberAccessExpressionContext ctx) {
						checkPatternMatch(accessPattern, ctx, (match) -> {
							String member = match.get("member").getText();
							if (member.equals("st") || member.equals("xy")) {
								replaceNode(ctx, lightmapCoordsExpression, GLSLParser::expression);
							} else if (member.equals("s")) {
								replaceNode(ctx, lightmapCoordsExpression + ".s", GLSLParser::expression);
							}
						});
					}

					@Override
					public void enterMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
						checkPatternMatch(bareMultPattern, ctx, (match) -> replaceNode(
								ctx, "vec4(" + lightmapCoordsExpression + ", 0.0, 1.0)", GLSLParser::expression));

						// PREV NOTE
						// NB: Technically this isn't a correct transformation (it changes the values
						// slightly), however the shader code being replaced isn't correct to begin with
						// since it doesn't properly apply the centering / scaling transformation like
						// gl_TextureMatrix[1] would. Therefore, I think this is acceptable.
						// This code shows up in Sildur's shaderpacks.
						ParseTreeMatch match = extraPattern.match(ctx);
						if (match.succeeded()) {
							String texCoord = match.get("texCoord").getText();
							if (texCoord.equals("gl_MultiTexCoord1") || texCoord.equals("gl_MultiTexCoord2")) {
								replaceNode(ctx, lightmapCoordsExpression, GLSLParser::expression);
							}
						}
					}

					@Override
					public void enterArrayAccessExpression(ArrayAccessExpressionContext ctx) {
						ParseTreeMatch match = textureMatrixPattern.match(ctx);
						if (match.succeeded()) {
							String texMatrixIndex = match.get("texMatrixIndex").getText();
							if (texMatrixIndex.equals("1") || texMatrixIndex.equals("2")) {
								replaceNode(ctx, new StringNode(irisLightmapTexMat));
								needsLightmapTexMatInjection = true;
							}
						}
					}
				});

				chainDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(InjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
					}

					@Override
					public boolean isActive() {
						return needsLightmapTexMatInjection;
					}
				});

				chainConcurrentDependent(
						RunPhase.withInjectExternalDeclarations(InjectionPoint.BEFORE_FUNCTIONS, "attribute vec2 a_LightCoord;"));

				chainConcurrentDependent(new SearchTerminals<Parameters>()
						.addTarget(new ParsedReplaceTargetImpl<>("gl_MultiTexCoord1",
								texCoordFallbackReplacement, GLSLParser::expression))
						.addTarget(new ParsedReplaceTargetImpl<>("gl_MultiTexCoord2",
								texCoordFallbackReplacement, GLSLParser::expression)));
			}
		};
		// #endregion patchSodiumTerrain

		manager = new TransformationManager<Parameters>(new Transformation<Parameters>() {
			@Override
			protected void setupGraph() {
				Patch patch = getJobParameters().patch;
				ShaderType type = getJobParameters().type;

				addEndDependent(detectReserved);

				// patchAttributes
				if (patch == Patch.ATTRIBUTES) {
					addEndDependent(replaceEntityColorDeclaration);

					if (type == ShaderType.VERTEX || type == ShaderType.GEOMETRY) {
						addEndDependent(wrapOverlay);
					} else if (type == ShaderType.FRAGMENT) {
						addEndDependent(renameEntityColorFragment);
					}
				}

				// patchSodiumTerrain
				if (patch == Patch.SODIUM_TERRAIN) {
					if (type == ShaderType.VERTEX) {
						addEndDependent(wrapFTransform);
						addEndDependent(wrapVertex);
						// gl_Vertex should be replaced after ftransform since it uses gl_Vertex itself
						addDependent(wrapFTransform, wrapVertex);
						addEndDependent(wrapMultiTexCoord);
						addEndDependent(wrapColor);
						addEndDependent(wrapNormal);
						addEndDependent(replaceBuiltinUniforms);
					}

					if (type == ShaderType.VERTEX || type == ShaderType.FRAGMENT) {
						addEndDependent(wrapModelViewMatrix);
						addEndDependent(wrapModelViewProjectionMatrix);
						addEndDependent(wrapNormalMatrix);
						addEndDependent(replaceTextureMatrix0);
					}
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

	/**
	 * AttributeShaderTransformer.patch(source, type,
	 * hasGeometry, inputs)
	 */
	public static String patchAttributes(String source, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
		// return inspectPatch(source,
		// "TYPE: " + type + " HAS_GEOMETRY: " + hasGeometry,
		// () -> transform(source, new AttributeParameters(Patch.ATTRIBUTES, type,
		// hasGeometry, inputs)));

		// routing through original patcher until changes to AttributeShaderTransformer
		// can be caught up in TransformPatcher
		return AttributeShaderTransformer.patch(source, type, hasGeometry, inputs);
	}

	/**
	 * type == ShaderType.VERTEX ?
	 * SodiumTerrainPipeline.transformVertexShader(source) :
	 * SodiumTerrainPipeline.transformFragmentShader(source)
	 */
	public static String patchSodiumTerrain(String source, ShaderType type) {
		return inspectPatch(source,
				"TYPE: " + type,
				() -> transform(source, new Parameters(Patch.SODIUM_TERRAIN, type)));
	}
}
