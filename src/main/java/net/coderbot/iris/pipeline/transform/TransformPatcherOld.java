package net.coderbot.iris.pipeline.transform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ArrayAccessExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.ExternalDeclarationContext;
import io.github.douira.glsl_transformer.GLSLParser.MemberAccessExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.MultiplicativeExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.GLSLParser.VersionStatementContext;
import io.github.douira.glsl_transformer.cst.core.CachePolicy;
import io.github.douira.glsl_transformer.cst.core.SearchTerminals;
import io.github.douira.glsl_transformer.cst.core.SemanticException;
import io.github.douira.glsl_transformer.cst.core.WrapIdentifier;
import io.github.douira.glsl_transformer.cst.core.target.HandlerTarget;
import io.github.douira.glsl_transformer.cst.core.target.HandlerTargetImpl;
import io.github.douira.glsl_transformer.cst.core.target.ParsedReplaceTarget;
import io.github.douira.glsl_transformer.cst.core.target.ParsedReplaceTargetImpl;
import io.github.douira.glsl_transformer.cst.core.target.TerminalReplaceTargetImpl;
import io.github.douira.glsl_transformer.cst.core.target.ThrowTargetImpl;
import io.github.douira.glsl_transformer.cst.core.target.WrapThrowTargetImpl;
import io.github.douira.glsl_transformer.cst.node.StringNode;
import io.github.douira.glsl_transformer.cst.token_filter.ChannelFilter;
import io.github.douira.glsl_transformer.cst.token_filter.TokenChannel;
import io.github.douira.glsl_transformer.cst.token_filter.TokenFilter;
import io.github.douira.glsl_transformer.cst.transform.CSTInjectionPoint;
import io.github.douira.glsl_transformer.cst.transform.CSTTransformer;
import io.github.douira.glsl_transformer.cst.transform.RunPhase;
import io.github.douira.glsl_transformer.cst.transform.Transformation;
import io.github.douira.glsl_transformer.cst.transform.WalkPhase;
import io.github.douira.glsl_transformer.cst.transform.lifecycle.LifecycleUser;
import io.github.douira.glsl_transformer.job_parameter.JobParameters;
import io.github.douira.glsl_transformer.tree.ExtendedContext;
import io.github.douira.glsl_transformer.tree.TreeMember;
import io.github.douira.glsl_transformer.util.CompatUtil;
import net.coderbot.iris.IrisLogging;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
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

public class TransformPatcherOld {
	private static Logger LOGGER = LogManager.getLogger(TransformPatcherOld.class);
	private static CSTTransformer<Parameters> transformer;

	private static enum Patch {
		ATTRIBUTES, VANILLA_REGULAR, VANILLA_WITH_ATTRIBUTE_TRANSFORM, SODIUM, COMPOSITE
	}

	private static class Parameters extends JobParameters {
		public final Patch patch;
		public final ShaderType type;

		public Parameters(Patch patch, ShaderType type) {
			this.patch = patch;
			this.type = type;
		}

		public AlphaTest getAlphaTest() {
			return null;
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

	private static class OverlayParameters extends Parameters {
		public final boolean hasGeometry;

		public OverlayParameters(Patch patch, ShaderType type, boolean hasGeometry) {
			super(patch, type);
			this.hasGeometry = hasGeometry;
		}
	}

	private static class AttributeParameters extends OverlayParameters {
		public final InputAvailability inputAvailability;

		public AttributeParameters(Patch patch, ShaderType type, boolean hasGeometry, InputAvailability inputAvailability) {
			super(patch, type, hasGeometry);
			this.inputAvailability = inputAvailability;
		}
	}

	/**
	 * This extends AttributeParameters so that the attribute transformation code
	 * can cast the job parameter object to AttributeParameters without issues even
	 * if it's actually a VanillaParameters instance.
	 */
	private static class VanillaParameters extends OverlayParameters {
		public final AlphaTest alpha;
		public final boolean hasChunkOffset;
		public final ShaderAttributeInputs inputs;

		public VanillaParameters(Patch patch, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
				ShaderAttributeInputs inputs, boolean hasGeometry) {
			super(patch, type, hasGeometry);
			this.alpha = alpha;
			this.hasChunkOffset = hasChunkOffset;
			this.inputs = inputs;
		}

		@Override
		public AlphaTest getAlphaTest() {
			return alpha;
		}
	}

	private static class SodiumParameters extends Parameters {
		public final AlphaTest alpha;
		public final ShaderAttributeInputs inputs;
		public final float positionScale;
		public final float positionOffset;
		public final float textureScale;

		public SodiumParameters(Patch patch, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
				float positionScale, float positionOffset, float textureScale) {
			super(patch, type);
			this.alpha = alpha;
			this.inputs = inputs;
			this.positionScale = positionScale;
			this.positionOffset = positionOffset;
			this.textureScale = textureScale;
		}

		@Override
		public AlphaTest getAlphaTest() {
			return alpha;
		}
	}

	private enum FragColorOutput {
		COLOR, DATA, CUSTOM
	}

	private enum ModelViewMatrixType {
		CHUNK_OFFSET, NEW_LINES, DEFAULT
	}

	/**
	 * Users of this transformation have to insert irisMain(); themselves because it
	 * can appear at varying positions in the new string.
	 */
	private static abstract class MainWrapper<R extends Parameters> extends WrapIdentifier<R> {
		protected abstract String getMainContent();

		{
			detectionResult("irisMain");
			wrapTarget("main");
			injectionLocation(CSTInjectionPoint.END);
			injectionExternalDeclarations(CachePolicy.ON_JOB);
		}

		@Override
		protected Collection<String> getInjectionExternalDeclarations() {
			return CompatUtil.listOf("void main() { " + getMainContent() + " }");
		}
	}

	static {
		// PREV TODO: Only do the NewLines patches if the source code isn't from
		// gbuffers_lines

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
				.addTarget(
						new ThrowTargetImpl<Parameters>(
								"iris_",
								"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))
				.addTarget(
						new ThrowTargetImpl<Parameters>(
								"moj_import",
								"Iris shader programs may not use moj_import directives."))
				.requireFullMatch(false);

		LifecycleUser<Parameters> fixVersion = new RunPhase<Parameters>() {
			@Override
			protected void run(TranslationUnitContext ctx) {
				VersionStatementContext versionStatement = ctx.versionStatement();
				if (versionStatement == null) {
					throw new IllegalStateException("Missing the version statement!");
				}

				String profile = Optional.ofNullable(versionStatement.profile)
						.map(terminal -> terminal.getText())
						.orElse("");
				int version = Integer.parseInt(versionStatement.NR_INTCONSTANT().getText());

				if (profile.equals("core")) {
					throw new IllegalStateException(
							"Transforming a shader that is already built against the core profile???");
				}

				if (version >= 200) {
					if (!profile.equals("compatibility")) {
						throw new IllegalStateException(
								"Expected \"compatibility\" after the GLSL version: #version " + version + " "
										+ profile);
					}
				} else {
					version = 150;
				}
				profile = "core";

				replaceNode(versionStatement, "#version " + version + " " + profile + "\n",
						GLSLParser::versionStatement);
			}
		};

		// TODO: update patchAttributes to current regex-based parser's behavior
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
								injectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
										"uniform sampler2D iris_overlay;",
										"varying vec4 entityColor;");
								break;
							case GEOMETRY:
								injectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
										"out vec4 entityColorGS;",
										"in vec4 entityColor[];");
								break;
							case FRAGMENT:
								injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS, "varying vec4 entityColor;");
								break;
						}
					}
				});
			}
		};

		LifecycleUser<Parameters> wrapOverlay = new MainWrapper<Parameters>() {
			@Override
			protected String getMainContent() {
				return getJobParameters().type == ShaderType.VERTEX
						? "vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
								"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\nirisMain();"
						: "entityColorGS = entityColor[0];\nirisMain();";
			}
		};

		LifecycleUser<Parameters> renameEntityColorFragment = new SearchTerminals<Parameters>() {
			@Override
			public boolean isActive() {
				return ((AttributeParameters) getJobParameters()).hasGeometry;
			}
		}
				.singleTarget(new TerminalReplaceTargetImpl<>("entityColor", "entityColorGS"));
		// #endregion patchAttributes

		// #region patchCommon
		/**
		 * PREV NOTE:
		 * This must be defined and valid in all shader passes, including composite
		 * passes. A shader that relies on this behavior is SEUS v11 - it reads
		 * gl_Fog.color and breaks if it is not properly defined.
		 */
		LifecycleUser<Parameters> wrapFogSetup = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_Fog")
				.detectionResult("iris_Fog")
				.injectionLocation(CSTInjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclarations(CompatUtil.listOf(
						"uniform float iris_FogDensity;",
						"uniform float iris_FogStart;",
						"uniform float iris_FogEnd;",
						"uniform vec4 iris_FogColor;",
						"struct iris_FogParameters { vec4 color; float density; float start; float end; float scale; };",
						"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));"));

		// PREV TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
		// PREV TODO: This doesn't handle geometry shaders... How do we do that?
		// NOTE: expects VERTEX or FRAGMENT
		LifecycleUser<Parameters> wrapFogFragCoord = new WrapIdentifier<Parameters>() {
			@Override
			protected Collection<String> getInjectionExternalDeclarations() {
				if (getJobParameters().type == ShaderType.VERTEX) {
					return CompatUtil.listOf("out float iris_FogFragCoord;");
				} else if (getJobParameters().type == ShaderType.FRAGMENT) {
					return CompatUtil.listOf("in float iris_FogFragCoord;");
				}
				throw new IllegalStateException("Unexpected shader type: " + getJobParameters().type);
			}
		}
				.wrapTarget("gl_FogFragCoord")
				.detectionResult("iris_FogFragCoord")
				.injectionLocation(CSTInjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclarations(CachePolicy.ON_FIXED_PARAMETER_CHANGE);

		/**
		 * PREV TODO: This is incorrect and is just the bare minimum needed for SEUS v11
		 * & Renewed to compile. It works because they don't actually use gl_FrontColor
		 * even though they write to it.
		 */
		LifecycleUser<Parameters> wrapFrontColor = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_FrontColor")
				.detectionResult("iris_FrontColor")
				.injectionLocation(CSTInjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("vec4 iris_FrontColor;");

		// TODO: the following procedure is not implemented yet for now as it's quite
		// involved. It fixes locations of outs in fragment shaders
		// (to be called fixFragLayouts)
		// 1. find all the outs and determine their location
		// 2. check if there is a single non-located out that could receive location
		// 3. add location 0 to that declaration

		LifecycleUser<Parameters> replaceStorageQualifierVertex = new Transformation<Parameters>() {
			{
				addEndDependent(new SearchTerminals<Parameters>()
						.terminalTokenType(GLSLParser.ATTRIBUTE)
						.singleTarget(new TerminalReplaceTargetImpl<Parameters>("attribute", "in")));
				addEndDependent(new SearchTerminals<Parameters>()
						.terminalTokenType(GLSLParser.VARYING)
						.singleTarget(new TerminalReplaceTargetImpl<Parameters>("varying", "out")));
			}
		};

		LifecycleUser<Parameters> replaceStorageQualifierFragment = new SearchTerminals<Parameters>()
				.terminalTokenType(GLSLParser.VARYING)
				.singleTarget(new TerminalReplaceTargetImpl<Parameters>("varying", "in"));

		// PREV TODO: Add similar functions for all legacy texture sampling functions
		LifecycleUser<Parameters> injectTextureFunctions = RunPhase.withInjectExternalDeclarations(
				CSTInjectionPoint.BEFORE_DECLARATIONS,
				"vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }",
				"vec4 texture3D(sampler3D sampler, vec3 coord) { return texture(sampler, coord); }",
				"vec4 texture2DLod(sampler2D sampler, vec2 coord, float lod) { return textureLod(sampler, coord, lod); }",
				"vec4 texture3DLod(sampler3D sampler, vec3 coord, float lod) { return textureLod(sampler, coord, lod); }",
				"vec4 shadow2D(sampler2DShadow sampler, vec3 coord) { return vec4(texture(sampler, coord)); }",
				"vec4 shadow2DLod(sampler2DShadow sampler, vec3 coord, float lod) { return vec4(textureLod(sampler, coord, lod)); }",
				"vec4 texture2DGrad(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texture2DGradARB(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texture3DGrad(sampler3D sampler, vec3 coord, vec3 dPdx, vec3 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texelFetch2D(sampler2D sampler, ivec2 coord, int lod) { return texelFetch(sampler, coord, lod); }",
				"vec4 texelFetch3D(sampler3D sampler, ivec3 coord, int lod) { return texelFetch(sampler, coord, lod); }");

		// #endregion patchCommon

		/**
		 * PREV NOTE:
		 * GLSL 1.50 Specification, Section 8.7:
		 * In all functions below, the bias parameter is optional for fragment shaders.
		 * The bias parameter is not accepted in a vertex or geometry shader.
		 */
		LifecycleUser<Parameters> injectTextureFunctionsFragment = RunPhase.withInjectExternalDeclarations(
				CSTInjectionPoint.BEFORE_DECLARATIONS,
				"vec4 texture2D(sampler2D sampler, vec2 coord, float bias) { return texture(sampler, coord, bias); }",
				"vec4 texture3D(sampler3D sampler, vec3 coord, float bias) { return texture(sampler, coord, bias); }");

		LifecycleUser<Parameters> wrapFragColorOutput = new Transformation<Parameters>() {
			private FragColorOutput type;
			private boolean usesFragColor;
			private boolean usesFragData;
			private boolean usesCustomPossible;
			private boolean usesCustom;
			private String fragColorWrapResult;
			private String fragColorWrapTarget;

			// a list of the declared custom names
			private Collection<String> declaredCustomNames;

			// the single custom name that has been used in the code
			private String usedCustomName;

			@Override
			public void resetState() {
				type = null;
				usesFragColor = false;
				usesFragData = false;
				usesCustomPossible = true;
				usesCustom = false;
			}

			{
				/**
				 * 1. detect use of gl_FragColor, gl_FragData or custom color outputs
				 * custom: find which color outputs write to location 0
				 * syntax: out vec4 <IDENTIFIER>; combined with actual use
				 * throw if there is more than one at the same location being used
				 * (multiple declaration at the same location without multiple use is fine)
				 * 1a. throw if more than one of these options is being used
				 */

				// detect use of custom color outputs like
				// "layout (location = 0) out vec4 <IDENTIFIER>"
				addEndDependent(new WalkPhase<Parameters>() {
					ParseTreePattern customColorOutPattern;
					ParseTreePattern illegalOutPattern;

					@Override
					public void init() {
						customColorOutPattern = compilePattern(
								"layout (location = 0) out vec4 <name:IDENTIFIER>;",
								GLSLParser.RULE_externalDeclaration);
						illegalOutPattern = compilePattern(
								"out vec4 <name:IDENTIFIER>;",
								GLSLParser.RULE_externalDeclaration);
					}

					// if it hasn't thrown yet, using custom is still possible.
					// init the datastructures for custom name detection
					@Override
					protected void beforeWalk(TranslationUnitContext ctx) {
						declaredCustomNames = new ArrayList<>();
						usedCustomName = null;
					}

					// always run the before walk to update the possible variable
					@Override
					public boolean isActiveBeforeWalk() {
						return true;
					}

					// only run the walk and after walk if using custom is possible at all
					@Override
					public boolean isActive() {
						return usesCustomPossible;
					}

					// using custom names is not possible anymore if none are declared
					@Override
					protected void afterWalk(TranslationUnitContext ctx) {
						usesCustomPossible = !declaredCustomNames.isEmpty();
					}

					@Override
					public void enterExternalDeclaration(ExternalDeclarationContext ctx) {
						ParseTreeMatch illegalMatch = illegalOutPattern.match(ctx);
						if (illegalMatch.succeeded()) {
							throw new SemanticException(
									"The declaration with the name '" + illegalMatch.get("name").getText()
											+ "' is missing a location specifier!");
						}

						ParseTreeMatch match = customColorOutPattern.match(ctx);
						if (match.succeeded()) {
							declaredCustomNames.add(match.get("name").getText());
						}
					}
				});

				addEndDependent(new SearchTerminals<Parameters>()
						// detect use of gl_FragColor
						.addTarget(
								new HandlerTargetImpl<Parameters>("gl_FragColor") {
									@Override
									public void handleResult(TreeMember node, String match) {
										usesFragColor = true;
									}
								})
						// detect use of gl_FragData
						.addTarget(new HandlerTargetImpl<Parameters>("gl_FragData") {
							@Override
							public void handleResult(TreeMember node, String match) {
								usesFragData = true;
							}
						}));

				// throw if there more than one of the two integrated methods is being used
				prependDependency(RunPhase.withRun(() -> {
					if (usesFragColor && usesFragData) {
						throw new SemanticException("gl_FragColor and gl_FragData can't be used at the same time!");
					}
				}));

				// if we are doing custom, check if any of the declared names are being used
				// (and if they are being used multiple times)
				prependDependency(new SearchTerminals<Parameters>() {
					@Override
					protected Collection<HandlerTarget<Parameters>> getTargets() {
						return declaredCustomNames.stream()
								.map(name -> new HandlerTargetImpl<Parameters>(name) {
									@Override
									public void handleResult(TreeMember node, String match) {
										// name is being used, make sure it's the only one
										if (usedCustomName == null) {
											usedCustomName = name;
										} else if (usedCustomName != name) {
											// a second name is being used, this is illegal
											throw new SemanticException(
													"More than two custom color output names can't be used as the same time! '"
															+ name + "' and '"
															+ usedCustomName + "' were used at the same time.");
										}
									}
								})
								.collect(Collectors.toList());
					}

					// only run the walk if there are any names to find but run the after walk for
					// determining the final frag color type
					@Override
					public boolean isActive() {
						return usesCustomPossible;
					}
				});

				prependDependency(RunPhase.withRun(() -> {
					// check if any declared custom names were actually used
					usesCustom = usedCustomName != null;
					usesCustomPossible = usesCustom;

					// throw if it's being used together with one of the other two methods
					// we know that only one of the two can be true at this point
					if (usesCustom && (usesFragColor || usesFragData)) {
						throw new SemanticException("Custom color outputs can't be used at the same time as "
								+ (usesFragColor ? "gl_FragColor" : "gl_FragData") + "!");
					}

					// finally it's clear which one method is being used
					type = usesFragColor
							? FragColorOutput.COLOR
							: usesFragData
									? FragColorOutput.DATA
									: FragColorOutput.CUSTOM;

					fragColorWrapResult = type == FragColorOutput.COLOR ? "iris_FragColor" : "iris_FragData";
					fragColorWrapTarget = type == FragColorOutput.COLOR ? "gl_FragColor" : "gl_FragData";
				}));

				/**
				 * 2. wrap their use: create a new output like
				 * "out vec4 iris_FragColor/iris_FragData[8];"
				 * (throw if the replacement target is present already)
				 * 3. redirect gl_Frag* to the newly created output (replace identifiers)
				 */
				chainConcurrentDependent(new WrapIdentifier<Parameters>() {
					@Override
					protected Collection<String> getDetectionResults() {
						return CompatUtil.listOf(fragColorWrapResult);
					}

					@Override
					protected Collection<String> getInjectionExternalDeclarations() {
						return CompatUtil.listOf(
								"out vec4 "
										+ (type == FragColorOutput.COLOR ? "iris_FragColor" : "iris_FragData[8]")
										+ ";");
					}

					@Override
					protected String getWrapTarget() {
						return fragColorWrapTarget;
					}

					@Override
					public boolean isActive() {
						return type == FragColorOutput.COLOR || type == FragColorOutput.DATA;
					}
				}
						.detectionResults(CachePolicy.ON_JOB)
						.injectionExternalDeclarations(CachePolicy.ON_FIXED_PARAMETER_CHANGE)
						.wrapTarget(CachePolicy.ON_FIXED_PARAMETER_CHANGE));

				/**
				 * 4. if alpha test is given, apply it with iris_FragColor/iris_FragData[0].
				 **/
				chainConcurrentDependent(new MainWrapper<Parameters>() {
					@Override
					protected String getMainContent() {
						return getJobParameters().getAlphaTest().toExpression(
								type == FragColorOutput.COLOR ? "gl_FragColor.a" : "gl_FragData[0].a", "", "");
					}

					@Override
					public boolean isActive() {
						Patch patch = getJobParameters().patch;
						return (patch == Patch.VANILLA_REGULAR || patch == Patch.SODIUM)
								&& getJobParameters().getAlphaTest() != null;
					}
				});
			}
		};

		LifecycleUser<Parameters> wrapProjMatrixVanilla = new WrapIdentifier<Parameters>()
				.wrapTarget("gl_ProjectionMatrix")
				.detectionResult("iris_ProjMat")
				.injectionLocation(CSTInjectionPoint.BEFORE_DECLARATIONS)
				.injectionExternalDeclaration("uniform mat4 iris_ProjMat;");

		LifecycleUser<Parameters> replaceExcessMultiTexCoord = new SearchTerminals<Parameters>()
				.singleTarget(
						new TerminalReplaceTargetImpl<>("gl_MultiTexCoord", "vec4(0.0, 0.0, 0.0, 1.0)"))
				.requireFullMatch(false);

		LifecycleUser<Parameters> wrapAttributeInputsVanillaVertex = new Transformation<Parameters>() {
			boolean hasTex;
			boolean hasLight;
			boolean hasNormalAndIsNotNewLines;

			@Override
			public void resetState() {
				ShaderAttributeInputs attributeInputs = ((VanillaParameters) getJobParameters()).inputs;
				hasTex = attributeInputs.hasTex();
				hasLight = attributeInputs.hasLight();
				hasNormalAndIsNotNewLines = attributeInputs.hasNormal() && !attributeInputs.isNewLines();
			}

			{
				addEndDependent(new SearchTerminals<Parameters>()
						.singleTarget(new WrapThrowTargetImpl<Parameters>("iris_UV0"))
						.activation(() -> hasTex));
				addEndDependent(new SearchTerminals<Parameters>()
						.singleTarget(new WrapThrowTargetImpl<Parameters>("iris_UV2"))
						.activation(() -> hasLight));

				prependDependency(new SearchTerminals<Parameters>()
						.addTarget(new ParsedReplaceTarget<Parameters>("gl_MultiTexCoord0") {
							@Override
							protected String getNewContent(TreeMember node, String match) {
								return hasTex ? "vec4(iris_UV0, 0.0, 1.0)" : "vec4(0.5, 0.5, 0.0, 1.0)";
							}

							@Override
							protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
									String match) {
								return GLSLParser::expression;
							}
						})
						.addTarget(new ParsedReplaceTarget<Parameters>("gl_MultiTexCoord1") {
							@Override
							protected String getNewContent(TreeMember node, String match) {
								return hasLight ? "vec4(iris_UV2, 0.0, 1.0)" : "vec4(240.0, 240.0, 0.0, 1.0)";
							}

							@Override
							protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
									String match) {
								return GLSLParser::expression;
							}
						})
						.addTarget(new ParsedReplaceTarget<Parameters>("gl_Normal") {
							@Override
							protected String getNewContent(TreeMember node, String match) {
								// the source is a little confusing
								return hasNormalAndIsNotNewLines ? "vec3(0.0, 0.0, 1.0)" : "iris_Normal";
							}

							@Override
							protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
									String match) {
								return GLSLParser::expression;
							}
						}));

				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS, "in vec2 iris_UV0;");
					}
				}.activation(() -> hasTex));
				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS, "in ivec2 iris_UV2;");
					}
				}.activation(() -> hasLight));
				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Normal;");
					}
				}.activation(() -> hasNormalAndIsNotNewLines));

				chainConcurrentDependent(replaceExcessMultiTexCoord);
			}
		};

		// TODO: in triforce this is confusing because iris_Color is used even when
		// hasColor is false in which case it's not defined anywhere
		Transformation<Parameters> wrapColorVanilla = new Transformation<Parameters>() {
			boolean hasColor;

			@Override
			public void resetState() {
				ShaderAttributeInputs attributeInputs = ((VanillaParameters) getJobParameters()).inputs;
				hasColor = attributeInputs.hasColor();
			}

			{
				addEndDependent(new SearchTerminals<Parameters>().singleTarget(
						new WrapThrowTargetImpl<Parameters>("iris_ColorModulator")));
				addEndDependent(new SearchTerminals<Parameters>() {
					@Override
					public boolean isActive() {
						return hasColor;
					}
				}.singleTarget(new WrapThrowTargetImpl<Parameters>("iris_Color")));

				prependDependency(new Transformation<Parameters>() {
					{
						addEndDependent(
								new SearchTerminals<Parameters>().singleTarget(new ParsedReplaceTarget<Parameters>("gl_Color") {
									@Override
									protected String getNewContent(TreeMember node, String match) {
										return hasColor ? "(iris_Color * iris_ColorModulator)" : "iris_ColorModulator";
									}

									@Override
									protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
											String match) {
										return GLSLParser::expression;
									}
								}));

						addEndDependent(RunPhase.withInjectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"uniform vec4 iris_ColorModulator;"));
					}
				});
			}
		};

		// PREV TODO: More solid way to handle texture matrices
		// TODO: test if this actually works
		Transformation<Parameters> wrapTextureMatricesVanilla = new Transformation<Parameters>() {
			boolean replacementHappened;

			@Override
			public void resetState() {
				replacementHappened = false;
			}

			{
				addEndDependent(new SearchTerminals<Parameters>()
						.addTarget(new WrapThrowTargetImpl<Parameters>("iris_TextureMat"))
						.addTarget(new WrapThrowTargetImpl<Parameters>("iris_LightmapTextureMatrix")));

				prependDependency(new WalkPhase<Parameters>() {
					ParseTreePattern pattern;

					@Override
					public void init() {
						pattern = compilePattern("gl_TextureMatrix[<index:expression>]", GLSLParser.RULE_expression);
					}

					@Override
					public void enterArrayAccessExpression(ArrayAccessExpressionContext ctx) {
						ParseTreeMatch match = pattern.match(ctx);
						if (match.succeeded()) {
							int index = Integer.parseInt(match.get("index").getText());
							String replacement = null;
							if (index == 0) {
								replacement = "iris_TextureMat";
							}
							if (index == 1) {
								replacement = "iris_LightmapTextureMatrix";
							}
							if (replacement != null) {
								replaceNode(ctx, replacement, GLSLParser::expression);
								replacementHappened = true;
							}
						}
					}
				});

				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"uniform mat4 iris_TextureMat;",
								"uniform mat4 iris_LightmapTextureMatrix;");
					}

					@Override
					public boolean isActive() {
						return replacementHappened;
					}
				});
			}
		};

		// PREV TODO: Should probably add the normal matrix as a proper uniform that's
		// computed on the CPU-side of things
		Transformation<Parameters> wrapModelViewMatrixVanilla = new Transformation<Parameters>() {
			String modelViewMatrixResult;
			ModelViewMatrixType type;

			{
				// determine the model view matrix result and what the mode is
				addEndDependent(RunPhase.withRun(() -> {
					VanillaParameters vanillaParameters = (VanillaParameters) getJobParameters();
					if (vanillaParameters.hasChunkOffset) {
						modelViewMatrixResult = "(iris_ModelViewMat * _iris_internal_translate(iris_ChunkOffset))";
						type = ModelViewMatrixType.CHUNK_OFFSET;
					} else if (vanillaParameters.inputs.isNewLines()) {
						modelViewMatrixResult = "(iris_VIEW_SCALE * iris_ModelViewMat)";
						type = ModelViewMatrixType.NEW_LINES;
					} else {
						modelViewMatrixResult = "iris_ModelViewMat";
						type = ModelViewMatrixType.DEFAULT;
					}
				}));

				// detect pre-existing injection results
				chainDependent(new SearchTerminals<Parameters>() {
					@Override
					protected Collection<HandlerTarget<Parameters>> getTargets() {
						Collection<HandlerTarget<Parameters>> targets = new ArrayList<>(3);
						targets.add(new WrapThrowTargetImpl<Parameters>("iris_ModelViewMat"));

						if (type == ModelViewMatrixType.CHUNK_OFFSET) {
							targets.add(new WrapThrowTargetImpl<Parameters>("iris_ChunkOffset"));
							targets.add(new WrapThrowTargetImpl<Parameters>("_iris_internal_translate"));
						} else if (type == ModelViewMatrixType.NEW_LINES) {
							targets.add(new WrapThrowTargetImpl<Parameters>("iris_VIEW_SCALE"));
							targets.add(new WrapThrowTargetImpl<Parameters>("iris_VIEW_SHRINK"));
						}

						return targets;
					}
				}.targets(CachePolicy.ON_JOB));

				// do variable replacements
				chainDependent(new SearchTerminals<Parameters>()
						.addTarget(
								new ParsedReplaceTarget<Parameters>("gl_ModelViewMatrix") {
									@Override
									protected String getNewContent(TreeMember node, String match) {
										return modelViewMatrixResult;
									}

									@Override
									protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
											String match) {
										return GLSLParser::expression;
									}
								})
						.addTarget(
								new ParsedReplaceTarget<Parameters>("gl_NormalMatrix") {
									@Override
									protected String getNewContent(TreeMember node, String match) {
										return "mat3(transpose(inverse(" + modelViewMatrixResult + ")))";
									}

									@Override
									protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
											String match) {
										return GLSLParser::expression;
									}
								})
						.addTarget(
								new ParsedReplaceTarget<Parameters>("gl_ModelViewProjectionMatrix") {
									@Override
									protected String getNewContent(TreeMember node, String match) {
										return "(gl_ProjectionMatrix * " + modelViewMatrixResult + ")";
									}

									@Override
									protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node,
											String match) {
										return GLSLParser::expression;
									}
								}));

				// do fixed and conditional injections
				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"uniform mat4 iris_ModelViewMat;");

						if (type == ModelViewMatrixType.CHUNK_OFFSET) {
							injectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
									"uniform vec3 iris_ChunkOffset;",
									"mat4 _iris_internal_translate(vec3 offset) {\n" +
											"    // NB: Column-major order\n" +
											"    return mat4(1.0, 0.0, 0.0, 0.0,\n" +
											"                0.0, 1.0, 0.0, 0.0,\n" +
											"                0.0, 0.0, 1.0, 0.0,\n" +
											"                offset.x, offset.y, offset.z, 1.0);\n" +
											"}");
						} else if (type == ModelViewMatrixType.NEW_LINES) {
							injectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
									"const float iris_VIEW_SHRINK = 1.0 - (1.0 / 256.0);",
									"const mat4 iris_VIEW_SCALE = mat4(\n" +
											"    iris_VIEW_SHRINK, 0.0, 0.0, 0.0,\n" +
											"    0.0, iris_VIEW_SHRINK, 0.0, 0.0,\n" +
											"    0.0, 0.0, iris_VIEW_SHRINK, 0.0,\n" +
											"    0.0, 0.0, 0.0, 1.0\n" +
											");");
						}
					}
				});
			}
		};

		Transformation<Parameters> vertexPositionWrapVanilla = new Transformation<Parameters>() {
			boolean isNewLines;
			String glVertexResult;

			{
				addEndDependent(RunPhase.withRun(() -> {
					isNewLines = ((VanillaParameters) getJobParameters()).inputs.isNewLines();
					glVertexResult = isNewLines ? "vec4(iris_Position + iris_vertex_offset, 1.0)"
							: "vec4(iris_Position, 1.0)";
				}));

				chainDependent(new SearchTerminals<Parameters>()
						.singleTarget(new ParsedReplaceTarget<Parameters>("gl_Vertex") {
							protected String getNewContent(TreeMember node, String match) {
								return glVertexResult;
							}

							protected Function<GLSLParser, ExtendedContext> getParseMethod(TreeMember node, String match) {
								return GLSLParser::expression;
							}
						}));

				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"vec3 iris_vertex_offset = vec3(0.0);");
					}

					@Override
					public boolean isActive() {
						return isNewLines;
					}
				});

				chainConcurrentDependent(new MainWrapper<Parameters>() {
					{
						chainDependency(RunPhase.withInjectExternalDeclarations(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"uniform vec2 iris_ScreenSize;",
								"uniform float iris_LineWidth;",
								"// Widen the line into a rectangle of appropriate width\n" +
										"// Isolated from Minecraft's rendertype_lines.vsh\n" +
										"// Both arguments are positions in NDC space (the same space as gl_Position)\n"
										+
										"void iris_widen_lines(vec4 linePosStart, vec4 linePosEnd) {\n" +
										"    vec3 ndc1 = linePosStart.xyz / linePosStart.w;\n" +
										"    vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;\n" +
										"\n" +
										"    vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * iris_ScreenSize);\n"
										+
										"    vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * iris_LineWidth / iris_ScreenSize;\n"
										+
										"\n" +
										"    if (lineOffset.x < 0.0) {\n" +
										"        lineOffset *= -1.0;\n" +
										"    }\n" +
										"\n" +
										"    if (gl_VertexID % 2 == 0) {\n" +
										"        gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);\n"
										+
										"    } else {\n" +
										"        gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);\n"
										+
										"    }\n" +
										"}\n"));
					}

					@Override
					protected String getMainContent() {
						return "    iris_vertex_offset = iris_Normal;\n" +
								"    irisMain();\n" +
								"    vec4 linePosEnd = gl_Position;\n" +
								"    gl_Position = vec4(0.0);\n\n" +
								"    iris_vertex_offset = vec3(0.0);\n" +
								"    irisMain();\n" +
								"    vec4 linePosStart = gl_Position;\n\n" +
								"    iris_widen_lines(linePosStart, linePosEnd);";
					}

					@Override
					public boolean isActive() {
						return isNewLines;
					}
				});

				chainConcurrentDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS, "in vec3 iris_Position;");
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_DECLARATIONS,
								"vec4 ftransform() { return gl_ModelViewProjectionMatrix * " + glVertexResult + "; }");
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
						injectExternalDeclaration(CSTInjectionPoint.BEFORE_FUNCTIONS, "uniform mat4 iris_LightmapTextureMatrix;");
					}

					@Override
					public boolean isActive() {
						return needsLightmapTexMatInjection;
					}
				});

				chainConcurrentDependent(
						RunPhase.withInjectExternalDeclarations(CSTInjectionPoint.BEFORE_FUNCTIONS, "attribute vec2 a_LightCoord;"));

				chainConcurrentDependent(new SearchTerminals<Parameters>()
						.addTarget(new ParsedReplaceTargetImpl<>("gl_MultiTexCoord1",
								texCoordFallbackReplacement, GLSLParser::expression))
						.addTarget(new ParsedReplaceTargetImpl<>("gl_MultiTexCoord2",
								texCoordFallbackReplacement, GLSLParser::expression)));
			}
		};
		// #endregion patchSodiumTerrain

		transformer = new CSTTransformer<Parameters>(new Transformation<Parameters>() {
			@Override
			protected void setupGraph() {
				Patch patch = getJobParameters().patch;
				ShaderType type = getJobParameters().type;

				addEndDependent(detectReserved);

				// patchAttributes
				if (patch == Patch.ATTRIBUTES || patch == Patch.VANILLA_WITH_ATTRIBUTE_TRANSFORM) {
					addEndDependent(replaceEntityColorDeclaration);

					if (type == ShaderType.VERTEX || type == ShaderType.GEOMETRY) {
						addEndDependent(wrapOverlay);
					} else if (type == ShaderType.FRAGMENT) {
						addEndDependent(renameEntityColorFragment);
					}
				}

				if (patch != Patch.ATTRIBUTES) {
					addEndDependent(fixVersion);
					addEndDependent(wrapFogSetup);
					addEndDependent(injectTextureFunctions);

					if (type == ShaderType.VERTEX) {
						addEndDependent(wrapFogFragCoord);
						addEndDependent(wrapFrontColor);
						addEndDependent(replaceStorageQualifierVertex);
					}

					else if (type == ShaderType.FRAGMENT) {
						addEndDependent(wrapFogFragCoord);
						addEndDependent(replaceStorageQualifierFragment);
						addEndDependent(injectTextureFunctionsFragment);

						// does frag color handling and the alpha test as well
						addEndDependent(wrapFragColorOutput);
					}

					// patchVanilla
					if (patch == Patch.VANILLA_REGULAR || patch == Patch.VANILLA_WITH_ATTRIBUTE_TRANSFORM) {
						addEndDependent(wrapProjMatrixVanilla);
						addEndDependent(wrapColorVanilla);
						addEndDependent(wrapTextureMatricesVanilla);
						addEndDependent(wrapModelViewMatrixVanilla);

						if (type == ShaderType.VERTEX) {
							addEndDependent(wrapAttributeInputsVanillaVertex);
							addEndDependent(vertexPositionWrapVanilla);
						}
					}

					// patchSodium
					if (patch == Patch.SODIUM) {

					}

					// patchComposite
					if (patch == Patch.COMPOSITE) {

					}
				}
			}
		});

		transformer.setParseTokenFilter(parseTokenFilter);
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
		return transformer.transform(source, parameters);
	}

	public static String patchAttributes(String source, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
		return inspectPatch(source,
				" TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> AttributeShaderTransformer.patch(source, type, hasGeometry, inputs));
		// () -> transform(source, new AttributeParameters(Patch.ATTRIBUTES, type,
		// hasGeometry, inputs)));
	}

	public static String patchVanilla(
			String source, ShaderType type, AlphaTest alpha,
			boolean hasChunkOffset, ShaderAttributeInputs inputs, boolean hasGeometry) {
		return inspectPatch(source,
				" TYPE: " + type + "HAS_GEOMETRY: " + hasGeometry,
				() -> transform(source,
						new VanillaParameters(
								inputs.hasOverlay() ? Patch.VANILLA_WITH_ATTRIBUTE_TRANSFORM : Patch.VANILLA_REGULAR,
								type, alpha, hasChunkOffset, inputs, hasGeometry)));
	}

	public static String patchSodium(
			String source, ShaderType type, AlphaTest alpha,
			ShaderAttributeInputs inputs, float positionScale, float positionOffset, float textureScale) {
		return inspectPatch(source,
				" TYPE: " + type,
				() -> transform(source,
						new SodiumParameters(Patch.SODIUM,
								type, alpha, inputs, positionScale, positionOffset, textureScale)));
	}

	public static String patchComposite(String source, ShaderType type) {
		return inspectPatch(source,
				" TYPE: " + type,
				() -> transform(source, new Parameters(Patch.COMPOSITE, type)));
	}
}
