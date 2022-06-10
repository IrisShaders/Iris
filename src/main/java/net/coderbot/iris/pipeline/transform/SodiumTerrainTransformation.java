package net.coderbot.iris.pipeline.transform;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;

import com.google.common.collect.ImmutableList;

import io.github.douira.glsl_transformer.GLSLParser;
import io.github.douira.glsl_transformer.GLSLParser.ArrayAccessExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.MemberAccessExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.MultiplicativeExpressionContext;
import io.github.douira.glsl_transformer.GLSLParser.TranslationUnitContext;
import io.github.douira.glsl_transformer.ast.StringNode;
import io.github.douira.glsl_transformer.core.SearchTerminals;
import io.github.douira.glsl_transformer.core.WrapIdentifier;
import io.github.douira.glsl_transformer.core.target.ParsedReplaceTargetImpl;
import io.github.douira.glsl_transformer.core.target.WrapThrowTargetImpl;
import io.github.douira.glsl_transformer.transform.ActivatableLifecycleUser;
import io.github.douira.glsl_transformer.transform.InjectionPoint;
import io.github.douira.glsl_transformer.transform.LifecycleUser;
import io.github.douira.glsl_transformer.transform.RunPhase;
import io.github.douira.glsl_transformer.transform.Transformation;
import io.github.douira.glsl_transformer.transform.WalkPhase;
import io.github.douira.glsl_transformer.tree.ExtendedContext;
import net.coderbot.iris.gl.shader.ShaderType;

public class SodiumTerrainTransformation extends Transformation<Parameters> {
	@Override
	protected void setupGraph() {
		ShaderType type = getJobParameters().type;

		if (type == ShaderType.VERTEX) {
			addEndDependent(wrapFTransform);
			addEndDependent(wrapVertex);
			// gl_Vertex should be replaced after ftransform since it uses gl_Vertex itself
			addDependent(wrapFTransform, wrapVertex);
			addEndDependent(wrapMultiTexCoord);
			addEndDependent(wrapColor);
			addEndDependent(wrapNormal);
			addEndDependent(replaceLightmapForSodium);
		}

		if (type == ShaderType.VERTEX || type == ShaderType.FRAGMENT) {
			addEndDependent(wrapModelViewMatrix);
			addEndDependent(wrapModelViewProjectionMatrix);
			addEndDependent(wrapNormalMatrix);
			addEndDependent(replaceTextureMatrix0);
		}
	}

	// see SodiumTerrainPipeline for the original patcher
	static final LifecycleUser<Parameters> wrapFTransform = new WrapIdentifier<Parameters>()
			.wrapTarget("ftransform")
			.detectionResult("iris_ftransform")
			.injectionLocation(InjectionPoint.BEFORE_FUNCTIONS)
			.injectionExternalDeclaration("vec4 iris_ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");

	static final LifecycleUser<Parameters> wrapVertex = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_Vertex")
			.detectionResults(ImmutableList.of("a_Pos", "u_ModelScale", "d_ModelOffset"))
			.parsedReplacement("vec4((a_Pos * u_ModelScale) + d_ModelOffset.xyz, 1.0)")
			.injectionExternalDeclarations(ImmutableList.of(
					"attribute vec3 a_Pos;",
					"uniform vec3 u_ModelScale;",
					"attribute vec4 d_ModelOffset;"))
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS);

	static final LifecycleUser<Parameters> wrapMultiTexCoord = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_MultiTexCoord0")
			.detectionResults(ImmutableList.of("a_TexCoord", "u_TextureScale"))
			.parsedReplacement("vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)")
			.injectionExternalDeclarations(ImmutableList.of(
					"uniform vec2 u_TextureScale;",
					"attribute vec2 a_TexCoord;"))
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS);

	static final LifecycleUser<Parameters> wrapColor = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_Color")
			.detectionResult("a_Color")
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
			.injectionExternalDeclaration("attribute vec4 a_Color;");

	static final LifecycleUser<Parameters> wrapNormal = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_Normal")
			.detectionResult("a_Normal")
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
			.injectionExternalDeclaration("attribute vec3 a_Normal;");

	static final LifecycleUser<Parameters> wrapModelViewMatrix = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_ModelViewMatrix")
			.detectionResult("u_ModelViewMatrix")
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
			.injectionExternalDeclaration("uniform mat4 u_ModelViewMatrix;");

	static final LifecycleUser<Parameters> wrapModelViewProjectionMatrix = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_ModelViewProjectionMatrix")
			.detectionResult("u_ModelViewProjectionMatrix")
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
			.injectionExternalDeclaration("uniform mat4 u_ModelViewProjectionMatrix;");

	static final LifecycleUser<Parameters> wrapNormalMatrix = new WrapIdentifier<Parameters>()
			.wrapTarget("gl_NormalMatrix")
			.detectionResult("u_NormalMatrix")
			.parsedReplacement("mat3(u_NormalMatrix)")
			.injectionLocation(InjectionPoint.BEFORE_DECLARATIONS)
			.injectionExternalDeclaration("uniform mat4 u_NormalMatrix;");

	static final LifecycleUser<Parameters> replaceTextureMatrix0 = new Transformation<Parameters>() {
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

	static final LifecycleUser<Parameters> replaceLightmapForSodium;

	static {
		/**
		 * Implements BuiltinUniformReplacementTransformer and does a little more. Note
		 * that the main walk phase uses the fact that the order of invocation is
		 * enterMemberAccessExpression, enterMultiplicativeExpression,
		 * enterArrayAccessExpression in the targeted expression.
		 */
		replaceLightmapForSodium = new Transformation<Parameters>() {
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
	}
}
