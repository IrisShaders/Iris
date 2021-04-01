package net.coderbot.iris.pipeline.newshader;

import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.shaderpack.transform.StringTransformations;
import net.coderbot.iris.shaderpack.transform.Transformations;

public class TriforcePatcher {
	public static String patch(String source, ShaderType type) {
		StringTransformations transformations = new StringTransformations(source);

		fixVersion(transformations);

		// TODO: What if the shader does gl_PerVertex.gl_FogFragCoord ?

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FogFragCoord iris_FogFragCoord");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "out float iris_FogFragCoord;");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_ProjectionMatrix ProjMat");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 ProjMat;");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord0 vec4(UV0, 0.0, 1.0)");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec2 UV0;");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_MultiTexCoord1 vec4(UV2, 0.0, 1.0)");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec2 UV2;");

		// TODO: Patching should take in mind cases where there are not color or normal vertex attributes
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Color (Color * ColorModulator)");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec4 Color;");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform vec4 ColorModulator;");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_Normal Normal");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec3 Normal;");

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "in vec3 Position;");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform mat4 ModelViewMat;");
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "uniform vec3 ChunkOffset;");

		if (type == ShaderType.VERTEX) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define attribute in");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define varying out");
		} else if (type == ShaderType.FRAGMENT) {
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define varying in");
		}

		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define lightmap Sampler2");

		if (type == ShaderType.FRAGMENT) {
			if (transformations.contains("gl_FragColor")) {
				throw new UnsupportedOperationException("[Triforce Patcher] gl_FragColor is not supported yet, please use gl_FragData!");
			}

			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "#define gl_FragData iris_FragData");
			transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "out vec4 iris_FragData[8];");
		}

		// TODO: Add similar functions for all legacy texture sampling functions
		transformations.injectLine(Transformations.InjectionPoint.AFTER_VERSION, "vec4 texture2D(sampler2D sampler, vec2 coord) { return texture(sampler, coord); }");

		System.out.println(transformations.toString());

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
