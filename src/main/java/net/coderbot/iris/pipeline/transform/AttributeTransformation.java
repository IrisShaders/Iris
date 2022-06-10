package net.coderbot.iris.pipeline.transform;

import java.util.Collection;

import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import com.google.common.collect.ImmutableList;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ExternalDeclarationContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.core.CachePolicy;
import io.github.douira.glsl_transformer.core.CachingSupplier;
import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.target.HandlerTarget;
import io.github.douira.glsl_transformer.core.target.HandlerTargetImpl;
import io.github.douira.glsl_transformer.core.target.ParsedReplaceTargetImpl;
import io.github.douira.glsl_transformer.core.target.TerminalReplaceTargetImpl;
import io.github.douira.glsl_transformer.transform.InjectionPoint;
import io.github.douira.glsl_transformer.transform.LifecycleUser;
import io.github.douira.glsl_transformer.transform.RunPhase;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.WalkPhase;
import io.github.douira.glsl_transformer.tree.TreeMember;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.shader.ShaderType;

/**
 * Replaces what AttributeShaderTransformer does but using glsl-transformer to
 * do it more robustly.
 * 
 * TODO: Breaks shadows on Complementary
 */
class AttributeTransformation extends Transformation<Parameters> {
	{
		addEndDependent(replaceMultiTexCoord);
		addEndDependent(patchOverlayColor);
		addEndDependent(patchTextureMatrices);
	}

	static final LifecycleUser<Parameters> replaceMultiTexCoord = new Transformation<Parameters>() {
		@Override
		protected void setupGraph() {
			ShaderType type = getJobParameters().type;

			addEndDependent(replaceMultiTexCoord12);
			addEndDependent(replaceMultiTexCoord0);
			if (type == ShaderType.VERTEX) {
				addEndDependent(replaceMultiTexCoord3);
			}
		}

		// PREV NOTE:
		// gl_MultiTexCoord1 and gl_MultiTexCoord2 are both ways to refer to the
		// lightmap texture coordinate.
		// See https://github.com/IrisShaders/Iris/issues/1149

		String multiTexCoordReplacement = "vec4(240.0, 240.0, 0.0, 1.0)";
		final LifecycleUser<Parameters> replaceMultiTexCoord12 = new SearchTerminals<Parameters>() {
			@Override
			protected Collection<HandlerTarget<Parameters>> getTargets() {
				InputAvailability inputs = ((AttributeParameters) getJobParameters()).inputs;
				if (inputs.lightmap) {
					return ImmutableList.of(
							new TerminalReplaceTargetImpl<>("gl_MultiTexCoord1", "gl_MultiTexCoord2"));
				} else {
					return ImmutableList.of(
							new TerminalReplaceTargetImpl<>("gl_MultiTexCoord1", multiTexCoordReplacement),
							new TerminalReplaceTargetImpl<>("gl_MultiTexCoord2", multiTexCoordReplacement));
				}
			}
		}.targets(CachePolicy.ON_JOB);

		final LifecycleUser<Parameters> replaceMultiTexCoord0 = new SearchTerminals<Parameters>() {
			@Override
			protected Collection<HandlerTarget<Parameters>> getTargets() {
				InputAvailability inputs = ((AttributeParameters) getJobParameters()).inputs;
				if (inputs.texture) {
					return ImmutableList.of(
							new TerminalReplaceTargetImpl<>("gl_MultiTexCoord0", multiTexCoordReplacement));
				}
				return null; // disables target tests on null
			}
		}.targets(CachePolicy.ON_JOB);

		// PREV NOTE:
		// gl_MultiTexCoord3 is a super legacy alias of mc_midTexCoord. We don't do this
		// replacement if
		// we think mc_midTexCoord could be defined just we can't handle an existing
		// declaration robustly.
		//
		// But basically the proper way to do this is to define mc_midTexCoord only if
		// it's not defined, and if
		// it is defined, figure out its type, then replace all occurrences of
		// gl_MultiTexCoord3 with the correct
		// conversion from mc_midTexCoord's declared type to vec4.

		// this implementation mirrors the current "simple" one from
		// AttributeShaderTransformer and doesn't do any type things
		final LifecycleUser<Parameters> replaceMultiTexCoord3 = new Transformation<Parameters>() {
			private boolean foundMixTexCoord3;
			private boolean foundMCMidTexCoord;

			@Override
			public void resetState() {
				foundMixTexCoord3 = false;
				foundMCMidTexCoord = false;
			}

			{
				chainDependent(new SearchTerminals<Parameters>()
						.addTarget(new HandlerTargetImpl<Parameters>("gl_MultiTexCoord3") {
							@Override
							public void handleResult(TreeMember node, String match) {
								foundMixTexCoord3 = true;
							}
						})
						.addTarget(new HandlerTargetImpl<Parameters>("mc_midTexCoord") {
							@Override
							public void handleResult(TreeMember node, String match) {
								foundMCMidTexCoord = true;
							}
						}));

				chainDependent(new SearchTerminals<Parameters>()
						.singleTarget(new TerminalReplaceTargetImpl<>("gl_MultiTexCoord3", "mc_midTexCoord"))
						.activation(this::doReplacement));

				chainConcurrentDependent(RunPhase.<Parameters>withInjectExternalDeclarations(
						InjectionPoint.BEFORE_FUNCTIONS, "attribute vec4 mc_midTexCoord;")
						.activation(this::doReplacement));
			}

			private boolean doReplacement() {
				return foundMixTexCoord3 && !foundMCMidTexCoord;
			}
		};
	};

	static final LifecycleUser<Parameters> patchTextureMatrices = new Transformation<Parameters>() {

		final LifecycleUser<Parameters> replaceGlTextureMatrix = new SearchTerminals<Parameters>()
				.singleTarget(new TerminalReplaceTargetImpl<>("gl_TextureMatrix", "iris_TextureMatrix"));
		final LifecycleUser<Parameters> textureMatrixInjections = new RunPhase<Parameters>() {
			@Override
			protected void run(TranslationUnitContext ctx) {
				InputAvailability inputs = ((AttributeParameters) getJobParameters()).inputs;
				injectExternalDeclarations(InjectionPoint.BEFORE_FUNCTIONS,
						"const float iris_ONE_OVER_256 = 0.00390625;\n",
						"const float iris_ONE_OVER_32 = iris_ONE_OVER_256 * 8;\n",
						inputs.lightmap
								? "mat4 iris_LightmapTextureMatrix = gl_TextureMatrix[2];\n"
								: "mat4 iris_LightmapTextureMatrix =" + // column major
										"mat4(iris_ONE_OVER_256, 0.0, 0.0, 0.0," +
										"     0.0, iris_ONE_OVER_256, 0.0, 0.0," +
										"     0.0, 0.0, iris_ONE_OVER_256, 0.0," +
										"     iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_32, iris_ONE_OVER_256);",
						"mat4 iris_TextureMatrix[8] = mat4[8](" +
								"gl_TextureMatrix[0]," +
								"iris_LightmapTextureMatrix," +
								"mat4(1.0)," +
								"mat4(1.0)," +
								"mat4(1.0)," +
								"mat4(1.0)," +
								"mat4(1.0)," +
								"mat4(1.0)" +
								");\n");
			}
		};

		{
			addEndDependent(replaceGlTextureMatrix);
			addEndDependent(textureMatrixInjections);
		}
	};

	// Add entity color -> overlay color attribute support.
	static final LifecycleUser<Parameters> patchOverlayColor = new Transformation<Parameters>() {
		@Override
		protected void setupGraph() {
			ShaderType type = getJobParameters().type;

			addEndDependent(replaceEntityColorDeclaration);

			if (type == ShaderType.VERTEX || type == ShaderType.GEOMETRY) {
				addEndDependent(wrapOverlayMain);
			} else if (type == ShaderType.FRAGMENT) {
				chainDependent(renameEntityColorFragment);
			}
		}

		// PREV TODO:
		// TODO: We're exposing entityColor to this stage even if it isn't declared in
		// this stage. But this is needed for the pass-through behavior.

		final LifecycleUser<Parameters> replaceEntityColorDeclaration = new Transformation<Parameters>() {
			private boolean foundEntityColor;

			@Override
			public void resetState() {
				foundEntityColor = false;
			}

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
							foundEntityColor = true;
						}
					}
				});

				// replace read references to grab the color from the first vertex.
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
								// if entityColor is not declared as a uniform, we don't make it available
								if (foundEntityColor) {
									injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "varying vec4 entityColor;");
								}
								break;
						}
					}
				});
			}
		};

		// Create our own main function to wrap the existing main function, so that we
		// can pass through the overlay color at the end to the fragment stage.
		final MainWrapper<Parameters> wrapOverlayMain = new MainWrapper<Parameters>() {
			@Override
			protected String getMainContent() {
				return getJobParameters().type == ShaderType.VERTEX
						? "vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[1] * gl_MultiTexCoord1).xy);\n" +
								"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\nirisMain_overlayColor();"
						: "entityColorGS = entityColor[0];\nirisMain();";
			}

			@Override
			protected Collection<String> getDetectionResults() {
				return ImmutableList.of(
						getJobParameters().type == ShaderType.VERTEX
								? "irisMain_overlayColor"
								: "irisMain");
			}

			{
				detectionResults(CachingSupplier.of(CachePolicy.ON_FIXED_PARAMETER_CHANGE, this::getDetectionResults));
			}
		};

		// Different output name to avoid a name collision in the geometry shader.
		final LifecycleUser<Parameters> renameEntityColorFragment = new SearchTerminals<Parameters>() {
			@Override
			public boolean isActive() {
				return ((AttributeParameters) getJobParameters()).hasGeometry;
			}
		}.singleTarget(new TerminalReplaceTargetImpl<>("entityColor", "entityColorGS"));
	};
}
