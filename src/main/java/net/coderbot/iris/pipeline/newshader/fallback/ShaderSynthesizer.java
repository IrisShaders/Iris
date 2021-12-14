package net.coderbot.iris.pipeline.newshader.fallback;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public class ShaderSynthesizer {
	public static String vsh(boolean hasChunkOffset, ShaderAttributeInputs inputs, FogMode fogMode, boolean entityLighting) {
		StringBuilder shader = new StringBuilder();
		StringBuilder main = new StringBuilder();

		// TODO: Legacy lines shader.

		shader.append("#version 150 core\n");

		// Vertex Position
		shader.append("uniform mat4 ModelViewMat;\n");
		shader.append("uniform mat4 ProjMat;\n");
		shader.append("in vec3 Position;\n");

		String position = hasChunkOffset ? "Position + ChunkOffset" : "Position";

		if (hasChunkOffset) {
			shader.append("uniform vec3 ChunkOffset;\n");
		}

		main.append("    gl_Position = ProjMat * ModelViewMat * vec4(");
		main.append(position);
		main.append(", 1.0);\n");

		// Vertex Color
		shader.append("out vec4 vertexColor;\n");
		shader.append("uniform vec4 ColorModulator;\n");

		// Vertex Normal
		if (inputs.hasNormal() && inputs.hasColor()) {
			shader.append("in vec4 Color;\n");

			if (entityLighting) {
				shader.append("uniform vec3 Light0_Direction;\n");
				shader.append("uniform vec3 Light1_Direction;\n");

				// TODO: Copied from Mojang code.
				shader.append("vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {\n" +
						"    lightDir0 = normalize(lightDir0);\n" +
						"    lightDir1 = normalize(lightDir1);\n" +
						"    float light0 = max(0.0, dot(lightDir0, normal));\n" +
						"    float light1 = max(0.0, dot(lightDir1, normal));\n" +
						"    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);\n" +
						"    return vec4(color.rgb * lightAccum, color.a);\n" +
						"}\n");

				shader.append("in vec3 Normal;\n");

				main.append("    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color);\n");
			} else {
				// TODO: Lines
			}
		} else if (inputs.hasColor()) {
			shader.append("in vec4 Color;\n");

			main.append("    vertexColor = Color * ColorModulator;\n");
		} else {
			main.append("    vertexColor = ColorModulator;\n");
		}

		// Overlay Color
		if (inputs.hasOverlay()) {
			shader.append("uniform sampler2D Sampler1;\n");
			shader.append("in vec2 UV2;\n");
			shader.append("out vec4 overlayColor;\n");

			main.append("    overlayColor = texelFetch(Sampler1, UV1, 0);\n");
		}

		// Vertex Texture
		if (inputs.hasTex()) {
			shader.append("in vec2 UV0;\n");
			shader.append("out vec2 texCoord;\n");

			main.append("    texCoord = UV0;\n");
		}

		// Fog
		if (fogMode == FogMode.ENABLED) {
			shader.append("out float vertexDistance;\n");

			main.append("    vertexDistance = length((ModelViewMat * vec4(");
			main.append(position);
			main.append(", 1.0)).xyz);\n");
		}

		// Vertex Light
		if (inputs.hasLight()) {
			shader.append("in vec2 UV2;\n");
			shader.append("out vec2 lightCoord;\n");

			main.append("    lightCoord = clamp(UV2 / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0));\n");
		}



		// void main
		shader.append("void main() {\n");
		shader.append(main);
		shader.append("}\n");

		return shader.toString();
	}

	public static String fsh(ShaderAttributeInputs inputs, FogMode fogMode, AlphaTest alphaTest) {
		StringBuilder shader = new StringBuilder();
		StringBuilder main = new StringBuilder();

		shader.append("#version 150 core\n");

		shader.append("out vec4 fragColor");

		if (inputs.hasTex()) {
			shader.append("in sampler2D Sampler0;\n");
			shader.append("in vec2 texCoord;\n");

			main.append("    vec4 color = texture(Sampler0, texCoord) * vertexColor;\n");
		} else {
			main.append("    vec4 color = vertexColor;\n");
		}

		if (inputs.hasOverlay()) {
			shader.append("in vec4 overlayColor;\n");

			main.append("    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);\n");
		}

		if (inputs.hasLight()) {
			shader.append("in sampler2D Sampler2;\n");
			shader.append("in vec2 lightCoord;\n");

			main.append("    color *= texture(Sampler2, lightCoord);\n");
		}

		if (fogMode == FogMode.ENABLED) {
			shader.append("uniform vec4 FogColor;\n");
			shader.append("uniform float FogStart;\n");
			shader.append("uniform float FogEnd;\n");
			shader.append("in float vertexDistance;\n");

			// TODO: Iris extensions - implement these two uniforms in FallbackShader.
			shader.append("uniform float FogDensity;\n");
			shader.append("uniform int FogIsExp2;\n");

			main.append("    float fogFactor;\n");
			main.append("    if (FogIsExp2 == 1) {\n");
			main.append("        float x = -vertexDistance * FogDensity;\n");
			main.append("        fogFactor = exp(x * x);\n");
			main.append("    } else {\n");
			main.append("        fogFactor = (FogEnd - vertexDistance) / (FogEnd - FogStart);\n");
			main.append("    }\n");

			main.append("    fogFactor = clamp(fogFactor, 0.0, 1.0);\n");

			main.append("    color.rgb = mix(FogColor.rgb, color.rgb, fogFactor * FogColor.a);\n");
		}

		main.append("    fragColor = color;\n");

		// void main
		shader.append("void main() {\n");
		shader.append(main);
		alphaTest.toExpression("fragColor.a", "    ");
		shader.append("}\n");

		return shader.toString();
	}
}
