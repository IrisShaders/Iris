package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.BuiltinUniformReplacementTransformer;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class TriforceSodiumPatcher {
	public static String patch(String source, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs) {
		if (source.contains("moj_import")) {
			throw new IllegalStateException("Iris shader programs may not use moj_import directives.");
		}

		if (source.contains("iris_")) {
			throw new IllegalStateException("Detected a potential reference to unstable and internal Iris shader interfaces (iris_). This isn't currently supported.");
		}

		StringTransformations transformations = new StringTransformations(source);

		fixVersion(transformations);

		if (type == ShaderType.FRAGMENT) {
			if (transformations.contains("irisMain")) {
				throw new IllegalStateException("Shader already contains \"irisMain\"???");
			}

			// Create our own main function to wrap the existing main function, so that we can run the alpha test at the
			// end.
			transformations.replaceExact("main", "irisMain");
			transformations.injectLine(Transformations.InjectionPoint.END, "void main() {\n" +
					"    irisMain();\n" +
					"\n" +
					alpha.toExpression("    ") +
					"}");
		}

		// TODO: More solid way to handle texture matrices
		transformations.replaceExact("gl_TextureMatrix[0]", "mat4(1.0)");

		// TODO: Other fog things
		// TODO: fogDensity isn't actually implemented!
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform float iris_FogDensity;\n" +
				"uniform float u_FogStart;\n" +
				"uniform float u_FogEnd;\n" +
				"uniform vec4 u_FogColor;\n" +
				"\n" +
				"struct iris_FogParameters {\n" +
				"    vec4 color;\n" +
				"    float density;\n" +
				"    float start;\n" +
				"    float end;\n" +
				"    float scale;\n" +
				"};\n" +
				"\n" +
				"iris_FogParameters iris_Fog = iris_FogParameters(u_FogColor, iris_FogDensity, u_FogStart, u_FogEnd, 1.0 / (u_FogEnd - u_FogStart));");

		transformations.define("gl_Fog", "iris_Fog");

		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
		transformations.define("gl_FogFragCoord", "iris_FogFragCoord");

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "out float iris_FogFragCoord;");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in float iris_FogFragCoord;");
		}

		if (type == ShaderType.VERTEX) {
			// TODO: This is incorrect and is just the bare minimum needed for SEUS v11 & Renewed to compile
			// It works because they don't actually use gl_FrontColor even though they write to it.
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 iris_FrontColor;");
			transformations.define("gl_FrontColor", "iris_FrontColor");
		}

		transformations.define("gl_ProjectionMatrix", "u_ProjectionMatrix");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 u_ProjectionMatrix;");

		if (type == ShaderType.VERTEX) {
			if (inputs.hasTex()) {
				transformations.define("gl_MultiTexCoord0", "vec4(a_TexCoord * u_TextureScale, 0.0, 1.0)");
				transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform float u_TextureScale;");
				transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in vec2 a_TexCoord;");
			} else {
				transformations.define("gl_MultiTexCoord0", "vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (inputs.hasLight()) {
				new BuiltinUniformReplacementTransformer("a_LightCoord").apply(transformations);
				transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in vec2 a_LightCoord;");
			} else {
				transformations.define("gl_MultiTexCoord1", "vec4(0.0, 0.0, 0.0, 1.0)");
			}

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs, other texture coordinates are not valid inputs.
			for (int i = 2; i < 8; i++) {
				transformations.define("gl_MultiTexCoord" + i, " vec4(0.0, 0.0, 0.0, 1.0)");
			}
		}

		if (inputs.hasColor()) {
			// TODO: Handle the fragment shader here
			transformations.define("gl_Color", "a_Color");

			if (type == ShaderType.VERTEX) {
				transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in vec4 a_Color;");
			}
		} else {
			transformations.define("gl_Color", "vec4(1.0)");
		}

		if (type == ShaderType.VERTEX) {
			if (inputs.hasNormal()) {
				transformations.define("gl_Normal", "a_Normal");
				transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "in vec3 a_Normal;");
			} else {
				transformations.define("gl_Normal", "vec3(0.0, 0.0, 1.0)");
			}
		}

		// TODO: Should probably add the normal matrix as a proper uniform that's computed on the CPU-side of things
		transformations.define("gl_NormalMatrix", "mat3(u_NormalMatrix)");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 u_NormalMatrix;");

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 u_ModelViewMatrix;");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "uniform mat4 u_ModelViewProjectionMatrix;");

		// TODO: All of the transformed variants of the input matrices, preferably computed on the CPU side...
		transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define gl_ModelViewMatrix u_ModelViewMatrix");
		transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define gl_ModelViewProjectionMatrix u_ModelViewProjectionMatrix");

		if (type == ShaderType.VERTEX) {
			// TODO: this breaks Vaporwave-Shaderpack since it expects that vertex positions will be aligned to chunks.
			transformations.define("gl_Vertex", "vec4(getVertexPosition(), 1.0)");

			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE,
					"struct DrawParameters {\n" +
					"    vec4 Offset;\n" +
					"};\n" +
					"layout(std140) uniform ubo_DrawParameters {\n" +
					"    DrawParameters Chunks[256];\n" +
					"};\n" +
					"in vec4 a_Pos; // The position of the vertex around the model origin\n" +
					"uniform float u_ModelScale;\n" +
					"uniform float u_ModelOffset;\n" +
					"uniform vec3 u_CameraTranslation;");

			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec3 getVertexPosition() { vec3 vertexPosition = a_Pos.xyz * u_ModelScale + u_ModelOffset; vec3 chunkOffset = Chunks[int(a_Pos.w)].Offset.xyz; return chunkOffset + vertexPosition + u_CameraTranslation; }");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
		}

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define attribute in");
			transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define varying out");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define varying in");
		}

		if (type == ShaderType.FRAGMENT) {
			if (transformations.contains("gl_FragColor")) {
				// TODO: Find a way to properly support gl_FragColor
				Iris.logger.warn("[Triforce Patcher] gl_FragColor is not supported yet, please use gl_FragData! Assuming that the shaderpack author intended to use gl_FragData[0]...");
				transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define gl_FragColor iris_FragData[0]");
			}

			transformations.injectLine(Transformations.InjectionPoint.DEFINES, "#define gl_FragData iris_FragData");
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "out vec4 iris_FragData[8];");
		}

		// TODO: Add similar functions for all legacy texture sampling functions
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }");

		if (type == ShaderType.FRAGMENT) {
			// GLSL 1.50 Specification, Section 8.7:
			//    In all functions below, the bias parameter is optional for fragment shaders.
			//    The bias parameter is not accepted in a vertex or geometry shader.
			transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 texture2D(sampler2D sampler, vec2 coord, float bias) { return texture(sampler, coord, bias); }");
		}

		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 texture2DLod(sampler2D sampler, vec2 coord, float lod) { return textureLod(sampler, coord, lod); }");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 shadow2D(sampler2DShadow sampler, vec3 coord) { return vec4(texture(sampler, coord)); }");
		transformations.injectLine(Transformations.InjectionPoint.BEFORE_CODE, "vec4 shadow2DLod(sampler2DShadow sampler, vec3 coord, float lod) { return vec4(textureLod(sampler, coord, lod)); }");

		//System.out.println(transformations.toString());

		// Just being careful
		transformations.define("ftransform", "iris_ftransform");

		// NB: This is needed on macOS or else the driver will refuse to compile most packs making use of these
		// constants.
		ProgramBuilder.MACRO_CONSTANTS.getDefineStrings().forEach(defineString ->
				transformations.injectLine(Transformations.InjectionPoint.DEFINES, defineString + "\n"));

		return transformations.toString();
	}

	private static void fixVersion(Transformations transformations) {
		String prefix = transformations.getPrefix();
		int split = prefix.indexOf("#version");
		String beforeVersion = prefix.substring(0, split);
		String actualVersion = prefix.substring(split + "#version".length()).trim();

		if (actualVersion.endsWith(" core")) {
			throw new IllegalStateException("Transforming a shader that is already built against the core profile???");
		}

		if (!actualVersion.startsWith("1")) {
			if (actualVersion.endsWith("compatibility")) {
				actualVersion = actualVersion.substring(0, actualVersion.length() - "compatibility".length()).trim() + " core";
			} else {
				throw new IllegalStateException("Expected \"compatibility\" after the GLSL version: #version " + actualVersion);
			}
		} else {
			actualVersion = "150 core";
		}

		beforeVersion = beforeVersion.trim();

		if (!beforeVersion.isEmpty()) {
			beforeVersion += "\n";
		}

		transformations.setPrefix(beforeVersion + "#version " + actualVersion + "\n");
	}
}
