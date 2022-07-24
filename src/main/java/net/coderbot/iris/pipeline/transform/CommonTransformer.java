package net.coderbot.iris.pipeline.transform;

import io.github.douira.glsl_transformer.ast.node.TranslationUnit;
import io.github.douira.glsl_transformer.ast.node.VersionStatement;
import io.github.douira.glsl_transformer.ast.node.VersionStatement.Profile;
import io.github.douira.glsl_transformer.ast.node.expression.Expression;
import io.github.douira.glsl_transformer.ast.node.expression.unary.FunctionCallExpression;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.query.Root;
import io.github.douira.glsl_transformer.ast.query.match.AutoHintedMatcher;
import io.github.douira.glsl_transformer.ast.query.match.Matcher;
import io.github.douira.glsl_transformer.ast.transform.ASTInjectionPoint;
import io.github.douira.glsl_transformer.ast.transform.ASTTransformer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.shader.ShaderType;

// Order fixed
public class CommonTransformer {
	public static final AutoHintedMatcher<Expression> glTextureMatrix0 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[0]", Matcher.expressionPattern);
	public static final AutoHintedMatcher<Expression> glTextureMatrix1 = new AutoHintedMatcher<>(
			"gl_TextureMatrix[1]", Matcher.expressionPattern);

	public static void transform(
			ASTTransformer<?> t,
			TranslationUnit tree,
			Root root,
			Parameters parameters) {
		// fix version
		fixVersion(tree);

		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?

		// transformations.define("gl_FogFragCoord", "iris_FogFragCoord");
		root.rename("gl_FogFragCoord", "iris_FogFragCoord");

		// TODO: This doesn't handle geometry shaders... How do we do that?
		if (parameters.type == ShaderType.VERTEX) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "out
			// float iris_FogFragCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"out float iris_FogFragCoord;");
		} else if (parameters.type == ShaderType.FRAGMENT) {
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in
			// float iris_FogFragCoord;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"in float iris_FogFragCoord;");
		}

		if (parameters.type == ShaderType.VERTEX) {
			// TODO: This is incorrect and is just the bare minimum needed for SEUS v11 &
			// Renewed to compile. It works because they don't actually use gl_FrontColor
			// even though they write to it.

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4
			// iris_FrontColor;");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"vec4 iris_FrontColor;");

			// transformations.define("gl_FrontColor", "iris_FrontColor");
			root.rename("gl_FrontColor", "iris_FrontColor");
		}

		if (parameters.type == ShaderType.FRAGMENT) {
			// TODO: Find a way to properly support gl_FragColor, see TransformPatcherOld
			// which implements this
			if (root.identifierIndex.has("gl_FragColor")) {
				Iris.logger.warn(
						"[Triforce Patcher] gl_FragColor is not supported yet, please use gl_FragData! Assuming that the shaderpack author intended to use gl_FragData[0]...");
				// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
				// gl_FragColor iris_FragData[0]");
				root.replaceReferenceExpressions(t, "gl_FragColor", "iris_FragData[0]");
			}

			// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
			// gl_FragData iris_FragData");
			root.rename("gl_FragData", "iris_FragData");

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
			// "layout (location = 0) out vec4 iris_FragData[8];");
			tree.parseAndInjectNode(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"layout (location = 0) out vec4 iris_FragData[8];");
		}

		if (parameters.type == ShaderType.VERTEX || parameters.type == ShaderType.FRAGMENT) {
			for (StorageQualifier qualifier : root.nodeIndex.get(StorageQualifier.class)) {
				// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
				// attribute in");
				if (qualifier.storageType == StorageType.ATTRIBUTE) {
					qualifier.storageType = StorageType.IN;
				} else if (qualifier.storageType == StorageType.VARYING) {
					qualifier.storageType = parameters.type == ShaderType.VERTEX
							// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
							// varying out"); (VERTEX)
							? StorageType.OUT
							// transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define
							// varying in"); (FRAGMENT)
							: StorageType.IN;
				}
			}
		}

		// addition: patch texture uniform to be gtexture but without touching it's use
		// as a function
		root.process(root.identifierIndex.getStream("texture")
				.filter(id -> !(id.getParent() instanceof FunctionCallExpression)),
				id -> id.setName("gtexture"));

		// TODO: Add similar functions for all legacy texture sampling functions
		if (parameters.type == ShaderType.FRAGMENT) {
			// GLSL 1.50 Specification, Section 8.7:
			// In all functions below, the bias parameter is optional for fragment shaders.
			// The bias parameter is not accepted in a vertex or geometry shader.

			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4
			// texture2D(sampler2D sampler, vec2 coord, float bias) { return
			// texture(sampler, coord, bias); }");
			// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4
			// texture3D(sampler3D sampler, vec3 coord, float bias) { return
			// texture(sampler, coord, bias); }");
			tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
					"vec4 texture2D(sampler2D sampler, vec2 coord, float bias) { return texture(sampler, coord, bias); }",
					"vec4 texture3D(sampler3D sampler, vec3 coord, float bias) { return texture(sampler, coord, bias); }");
		}

		// This must be defined and valid in all shader passes, including composite
		// passes. A shader that relies on this behavior is SEUS v11 - it reads
		// gl_Fog.color and breaks if it is not properly defined.

		// transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
		// "uniform float iris_FogDensity;\n" +
		// "uniform float iris_FogStart;\n" +
		// "uniform float iris_FogEnd;\n" +
		// "uniform vec4 iris_FogColor;\n" +
		// "\n" +
		// "struct iris_FogParameters {\n" +
		// " vec4 color;\n" +
		// " float density;\n" +
		// " float start;\n" +
		// " float end;\n" +
		// " float scale;\n" +
		// "};\n" +
		// "\n" +
		// "iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor,
		// iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd -
		// iris_FogStart));\n" +
		// "\n" +
		// "#define gl_Fog iris_Fog");
		root.rename("gl_Fog", "iris_Fog");
		tree.parseAndInjectNodes(t, ASTInjectionPoint.BEFORE_FUNCTIONS,
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
				"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));",
				"vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }",
				"vec4 texture3D(sampler3D sampler, vec3 coord) { return texture(sampler, coord); }",
				"vec4 texture2DLod(sampler2D sampler, vec2 coord, float lod) { return textureLod(sampler, coord, lod); }",
				"vec4 texture3DLod(sampler3D sampler, vec3 coord, float lod) { return textureLod(sampler, coord, lod); }",
				"vec4 shadow2D(sampler2DShadow sampler, vec3 coord) { return vec4(texture(sampler, coord)); }",
				"vec4 shadow2DLod(sampler2DShadow sampler, vec3 coord, float lod) { return vec4(textureLod(sampler, coord, lod)); }",
				"ivec4 texture2D(isampler2D sampler, ivec2 coord) { return texture(sampler, coord); }",
				"uvec4 texture2D(usampler2D sampler, uvec2 coord) { return texture(sampler, coord); }",
				"vec4 texture2DGrad(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texture2DGradARB(sampler2D sampler, vec2 coord, vec2 dPdx, vec2 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texture3DGrad(sampler3D sampler, vec3 coord, vec3 dPdx, vec3 dPdy) { return textureGrad(sampler, coord, dPdx, dPdy); }",
				"vec4 texelFetch2D(sampler2D sampler, ivec2 coord, int lod) { return texelFetch(sampler, coord, lod); }",
				"vec4 texelFetch3D(sampler3D sampler, ivec3 coord, int lod) { return texelFetch(sampler, coord, lod); }");
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
		if (versionStatement.version >= 200) {
			if (profile != Profile.COMPATIBILITY) {
				throw new IllegalStateException(
						"Expected \"compatibility\" after the GLSL version: #version " + versionStatement.version + " "
								+ profile);
			}
			versionStatement.profile = Profile.CORE;
		} else {
			versionStatement.version = 330;
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
		// transformations.define("ftransform", "iris_ftransform");
		root.rename("ftransform", "iris_ftransform");
	}
}
