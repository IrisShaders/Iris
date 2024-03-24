package net.irisshaders.iris.pipeline.fallback;

import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;

public class ShaderSynthesizer {
	public static String vsh(boolean hasChunkOffset, ShaderAttributeInputs inputs, FogMode fogMode,
							 boolean entityLighting, boolean isLeash) {
		StringBuilder shader = new StringBuilder();
		StringBuilder main = new StringBuilder();

		shader.append("#version 150 core\n");

		// Vertex Position
		shader.append("uniform mat4 ModelViewMat;\n");
		shader.append("uniform mat4 ProjMat;\n");
		shader.append("in vec3 Position;\n");

		String position;

		if (hasChunkOffset) {
			shader.append("uniform vec3 ChunkOffset;\n");
			position = "Position + ChunkOffset";
		} else {
			position = "Position";
		}

		if (inputs.isNewLines()) {
			shader.append("const float VIEW_SHRINK = 1.0 - (1.0 / 256.0);\n" +
				"const mat4 VIEW_SCALE = mat4(\n" +
				"    VIEW_SHRINK, 0.0, 0.0, 0.0,\n" +
				"    0.0, VIEW_SHRINK, 0.0, 0.0,\n" +
				"    0.0, 0.0, VIEW_SHRINK, 0.0,\n" +
				"    0.0, 0.0, 0.0, 1.0\n" +
				");\n");

			shader.append("uniform float LineWidth;\n" +
				"uniform vec2 ScreenSize;\n");

			main.append("vec4 linePosStart = ProjMat * VIEW_SCALE * ModelViewMat * vec4(" + position + ", 1.0);\n" +
				"    vec4 linePosEnd = ProjMat * VIEW_SCALE * ModelViewMat * vec4(" + position + " + Normal, 1.0);\n" +
				"\n" +
				"    vec3 ndc1 = linePosStart.xyz / linePosStart.w;\n" +
				"    vec3 ndc2 = linePosEnd.xyz / linePosEnd.w;\n" +
				"\n" +
				"    vec2 lineScreenDirection = normalize((ndc2.xy - ndc1.xy) * ScreenSize);\n" +
				"    vec2 lineOffset = vec2(-lineScreenDirection.y, lineScreenDirection.x) * LineWidth / ScreenSize;\n" +
				"\n" +
				"    if (lineOffset.x < 0.0) {\n" +
				"        lineOffset *= -1.0;\n" +
				"    }\n" +
				"\n" +
				"    if (gl_VertexID % 2 == 0) {\n" +
				"        gl_Position = vec4((ndc1 + vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);\n" +
				"    } else {\n" +
				"        gl_Position = vec4((ndc1 - vec3(lineOffset, 0.0)) * linePosStart.w, linePosStart.w);\n" +
				"    }\n");
		} else {
			main.append("    gl_Position = ProjMat * ModelViewMat * vec4(");
			main.append(position);
			main.append(", 1.0);\n");
		}

		// Vertex Color
		if (isLeash) {
			shader.append("flat ");
		}
		shader.append("out vec4 iris_vertexColor;\n");
		shader.append("uniform vec4 ColorModulator;\n");

		// Vertex Normal
		if (inputs.hasNormal() && inputs.hasColor()) {
			shader.append("in vec4 Color;\n");

			// TODO: Entity lighting without color? Only a theoretical possibility since all
			//       entity shaders use vertex color.
			if (entityLighting) {
				shader.append("uniform vec3 Light0_Direction;\n");
				shader.append("uniform vec3 Light1_Direction;\n");

				// Copied from Mojang code.
				shader.append("vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {\n" +
					"    lightDir0 = normalize(lightDir0);\n" +
					"    lightDir1 = normalize(lightDir1);\n" +
					"    float light0 = max(0.0, dot(lightDir0, normal));\n" +
					"    float light1 = max(0.0, dot(lightDir1, normal));\n" +
					"    float lightAccum = min(1.0, (light0 + light1) * 0.6 + 0.4);\n" +
					"    return vec4(color.rgb * lightAccum, color.a);\n" +
					"}\n");

				shader.append("in vec3 Normal;\n");

				// minecraft_mix_light just passes through the original alpha value, so it's safe here.
				main.append("    iris_vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, Normal, Color * ColorModulator);\n");
			} else if (inputs.isNewLines()) {
				shader.append("in vec3 Normal;\n");
				main.append("    iris_vertexColor = Color * ColorModulator;\n");
			} else {
				main.append("    iris_vertexColor = Color * ColorModulator;\n");
			}
		} else if (inputs.hasColor()) {
			shader.append("in vec4 Color;\n");

			main.append("    iris_vertexColor = Color * ColorModulator;\n");
		} else {
			main.append("    iris_vertexColor = ColorModulator;\n");
		}

		// Overlay Color
		if (inputs.hasOverlay()) {
			shader.append("uniform sampler2D overlay;\n");
			shader.append("in ivec2 UV1;\n");
			shader.append("out vec4 overlayColor;\n");

			main.append("    overlayColor = texelFetch(overlay, UV1, 0);\n");
		}

		// Vertex Texture
		if (inputs.hasTex()) {
			shader.append("in vec2 UV0;\n");
			shader.append("out vec2 texCoord;\n");

			main.append("    texCoord = UV0;\n");
		}

		// Fog
		if (fogMode == FogMode.PER_VERTEX) {
			shader.append("out float vertexDistance;\n");

			main.append("    vertexDistance = length((ModelViewMat * vec4(");
			main.append(position);
			main.append(", 1.0)).xyz);\n");
		}

		// Vertex Light
		if (inputs.hasLight()) {
			shader.append("in ivec2 UV2;\n");
			shader.append("out vec2 lightCoord;\n");

			main.append("    lightCoord = clamp(UV2 / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0));\n");
		}


		// void main
		shader.append("void main() {\n");
		shader.append(main);
		shader.append("}\n");

		return shader.toString();
	}

	public static String fsh(ShaderAttributeInputs inputs, FogMode fogMode, AlphaTest alphaTest, boolean intensityTex, boolean isLeash) {
		StringBuilder shader = new StringBuilder();
		StringBuilder main = new StringBuilder();

		shader.append("#version 150 core\n");

		shader.append("out vec4 fragColor;\n");
		shader.append("uniform float AlphaTestValue;\n");
		if (isLeash) {
			shader.append("flat ");
		}
		shader.append("in vec4 iris_vertexColor;\n");

		main.append("float iris_vertexColorAlpha = iris_vertexColor.a;");

		if (inputs.hasTex()) {
			shader.append("uniform sampler2D gtexture;\n");
			shader.append("in vec2 texCoord;\n");

			main.append("    vec4 color = texture(gtexture, texCoord)");

			if (intensityTex) {
				main.append(".rrrr");
			}

			if (alphaTest == AlphaTests.VERTEX_ALPHA) {
				main.append(" * vec4(iris_vertexColor.rgb, 1);\n");
			} else {
				main.append(" * iris_vertexColor;\n");
			}
		} else {
			if (alphaTest == AlphaTests.VERTEX_ALPHA) {
				main.append("vec4 color = vec4(iris_vertexColor.rgb, 1);\n");
			} else {
				main.append("vec4 color = iris_vertexColor;\n");
			}
		}

		if (inputs.hasOverlay()) {
			shader.append("in vec4 overlayColor;\n");

			main.append("    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);\n");
		}

		if (inputs.hasLight()) {
			shader.append("uniform sampler2D lightmap;\n");
			shader.append("in vec2 lightCoord;\n");

			main.append("    color *= texture(lightmap, lightCoord);\n");
		}

		if (fogMode == FogMode.PER_VERTEX || fogMode == FogMode.PER_FRAGMENT) {
			shader.append("uniform vec4 FogColor;\n");
			shader.append("uniform float FogStart;\n");
			shader.append("uniform float FogEnd;\n");

			if (fogMode == FogMode.PER_VERTEX) {
				// Use vertex distances, close enough
				shader.append("in float vertexDistance;\n");
				main.append("float fragmentDistance = vertexDistance;\n");
			} else /*if (fogMode == FogMode.PER_FRAGMENT)*/ {
				// Use fragment distances since beam vertices are very far apart
				shader.append("uniform mat4 ProjMat;\n");
				main.append("float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);\n");
			}

			// These are custom Iris uniforms implemented in FallbackShader.
			shader.append("uniform float FogDensity = 1.0;\n");
			shader.append("uniform int FogIsExp2 = 1;\n");

			main.append("    float fogFactor;\n");
			main.append("    if (FogIsExp2 == 1) {\n");
			main.append("        float x = fragmentDistance * FogDensity;\n");
			main.append("        fogFactor = exp(-x * x);\n");
			main.append("    } else {\n");
			main.append("        fogFactor = (FogEnd - fragmentDistance) / (FogEnd - FogStart);\n");
			main.append("    }\n");

			main.append("    fogFactor = clamp(fogFactor, 0.0, 1.0);\n");

			main.append("    color.rgb = mix(FogColor.rgb, color.rgb, fogFactor * FogColor.a);\n");
		}

		main.append("    fragColor = color;\n");

		// void main
		shader.append("void main() {\n");
		shader.append(main);
		shader.append(alphaTest.toExpression("fragColor.a", "AlphaTestValue", "    "));
		shader.append("}\n");

		return shader.toString();
	}
}
