package net.coderbot.iris.pipeline;

import com.google.common.collect.ImmutableSet;

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
 * A separate TransformationManager is created for each ShaderType.
 * That makes each of them more efficient as they don't need to run unnecessary
 * transformation phases.
 * 
 * NOTE: This patcher expects the string to not contain any (!) preprocessor
 * directives. The only allowed ones are #extension and #pragma as they are
 * considered "parsed" directives. If any other directive appears in the string,
 * it will throw.
 * 
 * TODO: JCPP has to be configured to remove preprocessor directives entirely
 * 
 * TODO: good examples for more complex transformation in triforce patcher?
 * ideas: BuiltinUniformReplacementTransformer, defines/replacements with loops,
 * replacements that account for whitespace like the one for gl_TextureMatrix
 */
public class TransformPatcher implements Patcher {
	private TransformationManager<Parameters> manager;

	private static enum Patch {
		ATTRIBUTES
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
			return "void main() { irisMain(); " + getMainContent() + "}";
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
				ImmutableSet.of(
						new ThrowTargetImpl<Parameters>(
								"moj_import", "Iris shader programs may not use moj_import directives."),
						new ThrowTargetImpl<Parameters>(
								"iris_",
								"Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported."))) {
			{
				allowInexactMatches();
			}
		};

		Transformation<Parameters> replaceEntityColorDeclaration = new Transformation<Parameters>() {
			{
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

				chainDependent(new SearchTerminalsImpl<Parameters>(
						new ParsedReplaceTargetImpl<>("entityColor", "entityColor[0]", GLSLParser::expression)) {
					@Override
					protected boolean isActive() {
						return getJobParameters().type == ShaderType.GEOMETRY;
					}
				});

				chainDependent(new RunPhase<Parameters>() {
					@Override
					protected void run(TranslationUnitContext ctx) {
						switch (getJobParameters().type) {
							case VERTEX:
								injectExternalDeclarations(InjectionPoint.BEFORE_DECLARATIONS,
										"out vec4 entityColor;",
										"uniform sampler2D iris_overlay;",
										"in ivec2 iris_UV1;");
								break;
							case GEOMETRY:
								injectExternalDeclarations(InjectionPoint.BEFORE_DECLARATIONS,
										"in vec4 entityColor[];",
										"out vec4 entityColorGS;");
								break;
							case FRAGMENT:
								injectExternalDeclaration(InjectionPoint.BEFORE_DECLARATIONS, "in vec4 entityColor;");
								break;
						}
					}
				});
			}
		};

		Transformation<Parameters> wrapOverlay = new MainWrapperDynamic<TransformPatcher.Parameters>() {
			@Override
			protected String getMainContent() {
				return (getJobParameters().type == ShaderType.VERTEX
						? "	vec4 overlayColor = texelFetch(iris_overlay, iris_UV1, 0);\n" +
								"	entityColor = vec4(overlayColor.rgb, 1.0 - overlayColor.a);\n"
						: "	 entityColorGS = entityColor[0];\n")
						+ "  irisMain();\n";
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
			}
		});

		manager.setParseTokenFilter(parseTokenFilter);
	}

	@Override
	public String patchAttributes(String source, ShaderType type, boolean hasGeometry) {
		return manager.transform(source, new AttributeParameters(Patch.ATTRIBUTES, type, hasGeometry));
	}
}
