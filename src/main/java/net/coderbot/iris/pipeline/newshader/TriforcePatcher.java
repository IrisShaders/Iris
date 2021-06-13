package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class TriforcePatcher {
	public static String patch(String source, ShaderType type, AlphaTest alpha, boolean hasChunkOffset, ShaderAttributeInputs inputs) {
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
		transformations.replaceExact("gl_TextureMatrix[0]", "iris_TextureMat");
		transformations.replaceExact("gl_TextureMatrix[1]", "iris_LightmapTextureMatrix");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 iris_LightmapTextureMatrix;");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 iris_TextureMat;");

		// TODO: Other fog things
		// TODO: fogDensity isn't actually implemented!
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform float iris_FogDensity;\n" +
				"uniform float iris_FogStart;\n" +
				"uniform float iris_FogEnd;\n" +
				"uniform vec4 iris_FogColor;\n" +
				"\n" +
				"struct iris_FogParameters {\n" +
				"    vec4 color;\n" +
				"    float density;\n" +
				"    float start;\n" +
				"    float end;\n" +
				"    float scale;\n" +
				"};\n" +
				"\n" +
				"iris_FogParameters iris_Fog = iris_FogParameters(iris_FogColor, iris_FogDensity, iris_FogStart, iris_FogEnd, 1.0 / (iris_FogEnd - iris_FogStart));\n" +
				"\n" +
				"#define gl_Fog iris_Fog");

		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FogFragCoord iris_FogFragCoord");

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "out float iris_FogFragCoord;");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in float iris_FogFragCoord;");
		}

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 iris_FrontColor;");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FrontColor iris_FrontColor");
		}

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_ProjectionMatrix iris_ProjMat");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 iris_ProjMat;");

		if (type == ShaderType.VERTEX) {
			if (inputs.hasTex()) {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord0 vec4(UV0, 0.0, 1.0)");
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec2 UV0;");
			} else {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord0 vec4(0.0, 0.0, 0.0, 1.0)");
			}

			if (inputs.hasLight()) {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord1 vec4(UV2, 0.0, 1.0)");
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in ivec2 UV2;");
			} else {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord1 vec4(0.0, 0.0, 0.0, 1.0)");
			}

			// gl_MultiTexCoord0 and gl_MultiTexCoord1 are the only valid inputs, other texture coordinates are not valid inputs.
			for (int i = 2; i < 8; i++) {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord" + i + " vec4(0.0, 0.0, 0.0, 1.0)");
			}
		}

		// TODO: Patching should take in mind cases where there are not color or normal vertex attributes

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform vec4 iris_ColorModulator;");

		if (inputs.hasColor()) {
			// TODO: Handle the fragment shader here
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Color (Color * iris_ColorModulator)");

			if (type == ShaderType.VERTEX) {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec4 Color;");
			}
		} else {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Color iris_ColorModulator");
		}

		if (type == ShaderType.VERTEX) {
			if (inputs.hasNormal()) {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Normal Normal");
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec3 Normal;");
			} else {
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Normal vec3(0.0, 0.0, 1.0)");
			}
		}

		// TODO: Should probably add the normal matrix as a proper uniform that's computed on the CPU-side of things
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_NormalMatrix mat3(transpose(inverse(gl_ModelViewMatrix)))");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 iris_ModelViewMat;");

		if (hasChunkOffset) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform vec3 iris_ChunkOffset;");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "mat4 _iris_internal_translate(vec3 offset) {\n" +
					"    // NB: Column-major order\n" +
					"    return mat4(1.0, 0.0, 0.0, 0.0,\n" +
					"                0.0, 1.0, 0.0, 0.0,\n" +
					"                0.0, 0.0, 1.0, 0.0,\n" +
					"                offset.x, offset.y, offset.z, 1.0);\n" +
					"}");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_ModelViewMatrix (iris_ModelViewMat * _iris_internal_translate(iris_ChunkOffset))");
		} else {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_ModelViewMatrix iris_ModelViewMat");
		}

		// TODO: All of the transformed variants of the input matrices, preferably computed on the CPU side...
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_ModelViewProjectionMatrix (gl_ProjectionMatrix * gl_ModelViewMatrix)");

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Vertex vec4(Position, 1.0)");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec3 Position;");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 ftransform() { return gl_ModelViewProjectionMatrix * gl_Vertex; }");
		}

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define attribute in");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define varying out");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define varying in");
		}

		if (type == ShaderType.FRAGMENT) {
			if (transformations.contains("gl_FragColor")) {
				// TODO: Find a way to properly support gl_FragColor
				Iris.logger.warn("[Triforce Patcher] gl_FragColor is not supported yet, please use gl_FragData! Assuming that the shaderpack author intended to use gl_FragData[0]...");
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FragColor iris_FragData[0]");
			}

			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FragData iris_FragData");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "out vec4 iris_FragData[8];");
		}

		// TODO: Add similar functions for all legacy texture sampling functions
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }");

		if (type == ShaderType.FRAGMENT) {
			// GLSL 1.50 Specification, Section 8.7:
			//    In all functions below, the bias parameter is optional for fragment shaders.
			//    The bias parameter is not accepted in a vertex or geometry shader.
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 texture2D(sampler2D sampler, vec2 coord, float bias) { return texture(sampler, coord, bias); }");
		}

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 texture2DLod(sampler2D sampler, vec2 coord, float lod) { return textureLod(sampler, coord, lod); }");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 shadow2D(sampler2DShadow sampler, vec3 coord) { return vec4(texture(sampler, coord)); }");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 shadow2DLod(sampler2DShadow sampler, vec3 coord, float lod) { return vec4(textureLod(sampler, coord, lod)); }");

		//System.out.println(transformations.toString());

		// NB: This is needed on macOS or else the driver will refuse to compile most packs making use of these
		// constants.
		ProgramBuilder.MACRO_CONSTANTS.getDefineStrings().forEach(defineString ->
				transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, defineString + "\n"));

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
