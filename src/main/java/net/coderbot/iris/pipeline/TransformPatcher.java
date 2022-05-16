package net.coderbot.iris.pipeline;

import java.util.stream.*;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.pattern.*;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.*;
import io.github.douira.glsl_transformer.core.*;
import io.github.douira.glsl_transformer.core.target.*;
import io.github.douira.glsl_transformer.print.filter.*;
import io.github.douira.glsl_transformer.transform.*;
import net.coderbot.iris.gl.shader.ShaderType;

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
public class TransformPatcher extends Patcher {

	private TransformationManager<Parameters> manager;

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

		public AttributeParameters(Patch patch, ShaderType type, boolean hasGeometry) {
			super(patch, type);
			this.hasGeometry = hasGeometry;
		}
	}

	private static abstract class MainWrapperDynamic<R extends Parameters> extends WrapIdentifier<R> {
		protected abstract String getMainContent();

		@Override
		protected String getDetectionResult() {
			return "irisMain";
		}

		@Override
		protected String getWrapTarget() {
			return "main";
		}

		@Override
		protected InjectionPoint getInjectionLocation() {
			return InjectionPoint.BEFORE_EOF;
		}

		@Override
		protected String getInjectionExternalDeclaration() {
			// inserts the alpha test, it is not null because it shouldn't be
			return "void main() { " + getMainContent() + "\nirisMain(); }";
		}
	}

	{
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

		LifecycleUser<Parameters> replaceTextureMatrix = new Transformation<Parameters>() {
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

						//TODO implement BuiltinUniformReplacementTransformer
					}

					if (type == ShaderType.VERTEX || type == ShaderType.FRAGMENT) {
						addEndDependent(wrapModelViewMatrix);
						addEndDependent(wrapModelViewProjectionMatrix);
						addEndDependent(wrapNormalMatrix);
						addEndDependent(replaceTextureMatrix);
					}
				}
			}
		});

		manager.setParseTokenFilter(parseTokenFilter);
	}

	private String transform(String source, Parameters parameters) {
		String result = manager.transform(source, parameters);
		// TODO: optionally logging here
		return result;
	}

	@Override
	public String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry) {
		return transform(source, new AttributeParameters(Patch.ATTRIBUTES, type, hasGeometry));
	}

	@Override
	public String patchSodiumTerrainInternal(String source, ShaderType type) {
		return transform(source, new Parameters(Patch.SODIUM_TERRAIN, type));
	}
}
