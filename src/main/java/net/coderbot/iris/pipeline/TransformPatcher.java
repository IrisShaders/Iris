package net.coderbot.iris.pipeline;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.pattern.*;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.*;
import io.github.douira.glsl_transformer.core.*;
import io.github.douira.glsl_transformer.core.target.*;
import io.github.douira.glsl_transformer.print.filter.*;
import io.github.douira.glsl_transformer.transform.*;
import io.github.douira.glsl_transformer.transform.TransformationPhase.InjectionPoint;
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

	private static abstract class MainWrapperDynamic<R extends Parameters> extends WrapIdentifierExternalDeclaration<R> {
		protected abstract String getMainContent();

		@Override
		protected String getInjectionContent() {
			// inserts the alpha test, it is not null because it shouldn't be
			return "void main() { " + getMainContent() + "\nirisMain(); }";
		}

		@Override
		protected InjectionPoint getInjectionLocation() {
			return InjectionPoint.BEFORE_EOF;
		}

		@Override
		protected String getWrapResultDynamic() {
			return "irisMain";
		}

		@Override
		protected String getWrapTargetDynamic() {
			return "main";
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
		TransformationPhase<Parameters> detectReserved = new SearchTerminalsImpl<Parameters>(SearchTerminals.IDENTIFIER,
				new ThrowTargetImpl<Parameters>(
						"iris_",
						"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported.")) {
			{
				allowInexactMatches();
			}
		};

		// #region patchAttributes
		Transformation<Parameters> replaceEntityColorDeclaration = new Transformation<Parameters>() {
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
					chainDependent(new SearchTerminalsImpl<Parameters>(
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

		Transformation<Parameters> wrapOverlay = new MainWrapperDynamic<Parameters>() {
			@Override
			protected String getMainContent() {
				return getJobParameters().type == ShaderType.VERTEX
						? "vec4 overlayColor = texture2D(iris_overlay, (gl_TextureMatrix[2] * gl_MultiTexCoord2).xy);\n" +
								"entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);"
						: "entityColorGS = entityColor[0];";
			}

			@Override
			protected boolean isActiveDynamic() {
				return true;
			}
		};

		TransformationPhase<Parameters> renameEntityColorFragment = new SearchTerminalsImpl<Parameters>(
				new TerminalReplaceTargetImpl<>("entityColor", "entityColorGS")) {
			@Override
			protected boolean isActive() {
				return ((AttributeParameters) getJobParameters()).hasGeometry;
			}
		};
		// #endregion patchAttributes

		// #region patchSodiumTerrain
		// see SodiumTerrainPipeline for the original patcher
		Transformation<Parameters> wrapFTransform = WrapIdentifier.<Parameters>withExternalDeclaration(
				"ftransform",
				"iris_ftransform",
				"iris_ftransform",
				InjectionPoint.BEFORE_FUNCTIONS,
				"vec4 iris_ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
		// TODO: the other wrappers

		Transformation<Parameters> wrapModelViewMatrix = WrapIdentifier.<Parameters>withExternalDeclaration(
				"gl_ModelViewMatrix",
				"u_ModelViewMatrix",
				"u_ModelViewMatrix",
				InjectionPoint.BEFORE_DECLARATIONS,
				"uniform mat4 u_ModelViewMatrix;");
		Transformation<Parameters> wrapModelViewProjectionMatrix = WrapIdentifier
				.<Parameters>withExternalDeclaration(
						"gl_ModelViewProjectionMatrix",
						"u_ModelViewProjectionMatrix",
						"u_ModelViewProjectionMatrix",
						InjectionPoint.BEFORE_DECLARATIONS,
						"uniform mat4 u_ModelViewProjectionMatrix;");
		Transformation<Parameters> wrapNormalMatrix = WrapIdentifier
				.<Parameters>withExternalDeclaration(
						"gl_NormalMatrix",
						"u_NormalMatrix",
						"mat3(u_NormalMatrix)",
						InjectionPoint.BEFORE_DECLARATIONS,
						"uniform mat4 u_NormalMatrix;");
		Transformation<Parameters> replaceTextureMatrix = new Transformation<Parameters>() {
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
						// TODO: the vertex-exclusive transformations
						addEndDependent(wrapFTransform);
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

	@Override
	public String patchAttributesInternal(String source, ShaderType type, boolean hasGeometry) {
		return manager.transform(source, new AttributeParameters(Patch.ATTRIBUTES, type, hasGeometry));
	}

	@Override
	public String patchSodiumTerrainInternal(String source, ShaderType type) {
		return manager.transform(source, new Parameters(Patch.SODIUM_TERRAIN, type));
	}
}
