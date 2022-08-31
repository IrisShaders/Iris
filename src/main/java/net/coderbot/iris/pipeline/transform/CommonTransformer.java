package net.coderbot.iris.pipeline.transform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.douira.glsl_transformer.ast.node.Identifier;
import io.github.douira.glsl_transformer.ast.node.Profile;
import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.Version;
import io.github.douira.glsl_transformer.ast.node.VersionStatement;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.LiteralExpression;
import io.github.douira.glsl_transformer.ast.node.expression.ReferenceExpression;
import io.github.douira.glsl_transformer.ast.node.expression.binary.ArrayAccessExpression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTParser;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.shader.ShaderType;

// Order fixed
public class CommonTransformer {
	public static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[0]", Matcher.expressionPattern);
	public static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[1]", Matcher.expressionPattern);

	private static final AutoHintedMatcher<Expression> glFragDataI = new AutoHintedMatcher<>(
			"gl_FragData[index]", Matcher.expressionPattern) {
		{
			markClassedPredicateWildcard("index",
					pattern.getRoot().identifierIndex.getOne("index").getAncestor(ReferenceExpression.class),
					LiteralExpression.class,
					literalExpression -> literalExpression.isInteger() && literalExpression.isPositive());
		}
	};

	private static final List<Expression> replaceExpressions = new ArrayList<>();
	private static final List<Long> replaceIndexes = new ArrayList<>();

	private static void renameFunctionCall(Root root, String oldName, String newName) {
		root.process(root.identifierIndex.getStream(oldName)
				.filter(id -> id.getParent() instanceof FunctionCallExpression),
				id -> id.setName(newName));
	}

	private static void renameAndWrapShadow(ASTParser t, Root root, String oldName, String innerName) {
		root.process(root.identifierIndex.getStream(oldName)
				.filter(id -> id.getParent() instanceof FunctionCallExpression),
				id -> {
					FunctionCallExpression functionCall = (FunctionCallExpression) id.getParent();
					functionCall.getFunctionName().setName(innerName);
					FunctionCallExpression wrapper = (FunctionCallExpression) t.parseExpression(id, "vec4()");
					functionCall.replaceBy(wrapper);
					wrapper.getParameters().add(functionCall);
				});
	}

	public static void transform(
			ASTParser t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// fix version
		fixVersion(tree);

		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?

		root.rename("gl_FogFragCoord", "iris_FogFragCoord");

		// TODO: This doesn't handle geometry shaders... How do we do that?
		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"out float iris_FogFragCoord;");
		} else if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"in float iris_FogFragCoord;");
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX) {
			// TODO: This is incorrect and is just the bare minimum needed for SEUS v11 &
			// Renewed to compile. It works because they don't actually use gl_FrontColor
			// even though they write to it.

			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"vec4 iris_FrontColor;");
			root.rename("gl_FrontColor", "iris_FrontColor");
		}

		if (parameters.type.glShaderType == ShaderType.FRAGMENT) {
			// TODO: Find a way to properly support gl_FragColor, see TransformPatcherOld
			// which implements this
			if (root.identifierIndex.has("gl_FragColor")) {
				Iris.logger.warn(
						"[Patcher] gl_FragColor is not supported yet, please use gl_FragData! Assuming that the shaderpack author intended to use gl_FragData[0]...");
				root.replaceReferenceExpressions(t, "gl_FragColor", "iris_FragData[0]");
			}

			// change gl_FragData[i] to iris_FragDatai
			replaceExpressions.clear();
			replaceIndexes.clear();
			Set<Long> replaceIndexesSet = new HashSet<>();
			for (Identifier id : root.identifierIndex.get("gl_FragData")) {
				ArrayAccessExpression accessExpression = id.getAncestor(ArrayAccessExpression.class);
				if (accessExpression == null || !glFragDataI.matchesExtract(accessExpression)) {
					continue;
				}
				replaceExpressions.add(accessExpression);
				long index = glFragDataI.getNodeMatch("index", LiteralExpression.class).getInteger();
				replaceIndexes.add(index);
				replaceIndexesSet.add(index);
			}
			for (int i = 0; i < replaceExpressions.size(); i++) {
				replaceExpressions.get(i).replaceByAndDelete(
						t.parseExpression(replaceExpressions.get(i), "iris_FragData" + replaceIndexes.get(i)));
			}
			for (long index : replaceIndexesSet) {
				tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
						"layout (location = " + index + ") out vec4 iris_FragData" + index + ";");
			}
			replaceExpressions.clear();
			replaceIndexes.clear();
		}

		if (parameters.type.glShaderType == ShaderType.VERTEX || parameters.type.glShaderType == ShaderType.FRAGMENT) {
			for (StorageQualifier qualifier : root.nodeIndex.get(StorageQualifier.class)) {
				if (qualifier.storageType == StorageType.ATTRIBUTE) {
					qualifier.storageType = StorageType.IN;
				} else if (qualifier.storageType == StorageType.VARYING) {
					qualifier.storageType = parameters.type.glShaderType == ShaderType.VERTEX
							? StorageType.OUT
							: StorageType.IN;
				}
			}
		}

		// addition: rename all uses of texture to gtexture if it's *not* used as a
		// function call
		root.process(root.identifierIndex.getStream("texture")
				.filter(id -> !(id.getParent() instanceof FunctionCallExpression)),
				id -> id.setName("gtexture"));

		// This must be defined and valid in all shader passes, including composite
		// passes. A shader that relies on this behavior is SEUS v11 - it reads
		// gl_Fog.color and breaks if it is not properly defined.
		root.rename("gl_Fog", "iris_Fog");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_DECLARATIONS,
				"uniform float iris_FogDensity;",
				"uniform float iris_FogStart;",
				"uniform float iris_FogEnd;",
				"uniform vec4 iris_FogColor;",
				"struct iris_FogParameters {" +
						"vec4 color;" +
						"float density;" +
						"float start;" +
						"float end;" +
						"float scale;" +
						"};",
				"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));");

		// TODO: Add similar functions for all legacy texture sampling functions
		renameFunctionCall(root, "texture2D", "texture");
		renameFunctionCall(root, "texture3D", "texture");
		renameFunctionCall(root, "texture2DLod", "textureLod");
		renameFunctionCall(root, "texture3DLod", "textureLod");
		renameFunctionCall(root, "texture2DGrad", "textureGrad");
		renameFunctionCall(root, "texture2DGradARB", "textureGrad");
		renameFunctionCall(root, "texture3DGrad", "textureGrad");
		renameFunctionCall(root, "texelFetch2D", "texelFetch");
		renameFunctionCall(root, "texelFetch3D", "texelFetch");
		renameFunctionCall(root, "textureSize2D", "textureSize");

		renameAndWrapShadow(t, root, "shadow2D", "texture");
		renameAndWrapShadow(t, root, "shadow2DLod", "textureLod");
	}

	public static void fixVersion(TranslationUnit tree) {
		VersionStatement versionStatement = tree.getVersionStatement();
		if (versionStatement == null) {
			throw new IllegalStateException("Missing the version statement!");
		}
		Profile profile = versionStatement.profile;
		if (profile == Profile.CORE) {
			throw new IllegalStateException(
					"Transforming a shader that is already built against the core profile???");
		}
		if (versionStatement.version.number >= 200) {
			if (profile != Profile.COMPATIBILITY) {
				throw new IllegalStateException(
						"Expected \"compatibility\" after the GLSL version: #version " + versionStatement.version + " "
								+ profile);
			}
			versionStatement.profile = Profile.CORE;
		} else {
			versionStatement.version = Version.GL33;
			versionStatement.profile = Profile.CORE;
		}
	}

	public static void applyIntelHd4000Workaround(Root root) {
		// This is a driver bug workaround that seems to be needed on HD 4000 and HD
		// 2500 drivers.
		//
		// Without this, they will see the call to ftransform() without taking into
		// account the fact that we've defined the function ourselves, and thus will
		// allocate attribute location 0 to gl_Vertex since the driver thinks that we
		// are referencing gl_Vertex by calling the compatibility-profile ftransform()
		// function - despite using a core profile shader version where that function
		// does not exist.
		//
		// Then, when we try to bind the attribute locations for our vertex format, the
		// shader will fail to link. By renaming the function, we avoid the driver bug
		// here, since it no longer thinks that we're trying to call the compatibility
		// profile fransform() function.
		//
		// See: https://github.com/IrisShaders/Iris/issues/441
		//
		// Note that this happens after we've added our ftransform function - so that
		// both all the calls and our function are renamed in one go.
		root.rename("ftransform", "iris_ftransform");
	}

	public static void replaceGlMultiTexCoordBounded(
			ASTParser t,
			Root root,
			int minimum,
			int maximum) {
		root.replaceReferenceExpressions(t,
				root.identifierIndex.prefixQueryFlat("gl_MultiTexCoord")
						.filter(id -> {
							int index = Integer.parseInt(id.getName().substring("gl_MultiTexCoord".length()));
							return index >= minimum && index <= maximum;
						}),
				"vec4(0.0, 0.0, 0.0, 1.0)");
	}
}
