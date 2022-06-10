package net.coderbot.iris.pipeline.transform;

import java.util.Collection;

import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import com.google.common.collect.ImmutableList;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ExternalDeclarationContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.core.CachePolicy;
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

class AttributeTransformation extends Transformation<Parameters> {
	@Override
	protected void setupGraph() {
		ShaderType type = getJobParameters().type;

		addEndDependent(replaceMultiTexCoord12);
		addEndDependent(replaceMultiTexCoord0);
		if (type == ShaderType.VERTEX) {
			addEndDependent(replaceMultiTexCoord3);
		}

		// TODO: patchTextureMatrices

		// TODO: patchOverlayColor

		addEndDependent(replaceEntityColorDeclaration);

		if (type == ShaderType.VERTEX || type == ShaderType.GEOMETRY) {
			addEndDependent(wrapOverlay);
		} else if (type == ShaderType.FRAGMENT) {
			addEndDependent(renameEntityColorFragment);
		}
	}

	static final LifecycleUser<Parameters> replaceMultiTexCoord12;
	static final LifecycleUser<Parameters> replaceMultiTexCoord0;
	static final LifecycleUser<Parameters> replaceMultiTexCoord3;

	static final LifecycleUser<Parameters> replaceEntityColorDeclaration;
	static final LifecycleUser<Parameters> wrapOverlay;
	static final LifecycleUser<Parameters> renameEntityColorFragment;

	static {
		String multiTexCoordReplacement = "vec4(240.0, 240.0, 0.0, 1.0)";
		replaceMultiTexCoord12 = new SearchTerminals<Parameters>() {
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

		replaceMultiTexCoord0 = new SearchTerminals<Parameters>() {
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
		replaceMultiTexCoord3 = new Transformation<Parameters>() {
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
						.activation(() -> foundMixTexCoord3 && !foundMCMidTexCoord));

				chainConcurrentDependent(RunPhase.withInjectExternalDeclarations(
						InjectionPoint.BEFORE_FUNCTIONS, "attribute vec4 mc_midTexCoord;"));
			}
		};

		// does some of patchOverlayColor
		replaceEntityColorDeclaration = new Transformation<Parameters>() {
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

		wrapOverlay = new MainWrapper<Parameters>() {
			@Override
			protected String getMainContent() {
				return getJobParameters().type == ShaderType.VERTEX
						? "vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
								"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\nirisMain();"
						: "entityColorGS = entityColor[0];\nirisMain();";
			}
		};

		renameEntityColorFragment = new SearchTerminals<Parameters>() {
			@Override
			public boolean isActive() {
				return ((AttributeParameters) getJobParameters()).hasGeometry;
			}
		}.singleTarget(new TerminalReplaceTargetImpl<>("entityColor", "entityColorGS"));
	}
}
