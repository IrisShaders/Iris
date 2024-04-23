package net.irisshaders.iris.compat.dh;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.program.ProgramImages;
import net.irisshaders.iris.gl.program.ProgramSamplers;
import net.irisshaders.iris.gl.program.ProgramUniforms;
import net.irisshaders.iris.gl.shader.GlShader;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IrisLodRenderProgram {
	// Uniforms
	public final int modelOffsetUniform;
	public final int worldYOffsetUniform;
	public final int mircoOffsetUniform;
	public final int modelViewUniform;
	public final int modelViewInverseUniform;
	public final int projectionUniform;
	public final int projectionInverseUniform;
	public final int normalMatrix3fUniform;
	// Fog/Clip Uniforms
	public final int clipDistanceUniform;
	private final int id;
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private final BlendModeOverride blend;
	private final BufferBlendOverride[] bufferBlendOverrides;

	// This will bind  AbstractVertexAttribute
	private IrisLodRenderProgram(String name, boolean isShadowPass, boolean translucent, BlendModeOverride override, BufferBlendOverride[] bufferBlendOverrides, String vertex, String tessControl, String tessEval, String geometry, String fragment, CustomUniforms customUniforms, IrisRenderingPipeline pipeline) {
		id = GL43C.glCreateProgram();

		GL32.glBindAttribLocation(this.id, 0, "vPosition");
		GL32.glBindAttribLocation(this.id, 1, "iris_color");
		GL32.glBindAttribLocation(this.id, 2, "irisExtra");

		this.bufferBlendOverrides = bufferBlendOverrides;

		GlShader vert = new GlShader(ShaderType.VERTEX, name + ".vsh", vertex);
		GL43C.glAttachShader(id, vert.getHandle());

		GlShader tessCont = null;
		if (tessControl != null) {
			tessCont = new GlShader(ShaderType.TESSELATION_CONTROL, name + ".tcs", tessControl);
			GL43C.glAttachShader(id, tessCont.getHandle());
		}

		GlShader tessE = null;
		if (tessEval != null) {
			tessE = new GlShader(ShaderType.TESSELATION_EVAL, name + ".tes", tessEval);
			GL43C.glAttachShader(id, tessE.getHandle());
		}

		GlShader geom = null;
		if (geometry != null) {
			geom = new GlShader(ShaderType.GEOMETRY, name + ".gsh", geometry);
			GL43C.glAttachShader(id, geom.getHandle());
		}

		GlShader frag = new GlShader(ShaderType.FRAGMENT, name + ".fsh", fragment);
		GL43C.glAttachShader(id, frag.getHandle());

		GL32.glLinkProgram(this.id);
		int status = GL32.glGetProgrami(this.id, 35714);
		if (status != 1) {
			String message = "Shader link error in Iris DH program! Details: " + GL32.glGetProgramInfoLog(this.id);
			this.free();
			throw new RuntimeException(message);
		} else {
			GL32.glUseProgram(this.id);
		}

		vert.destroy();
		frag.destroy();

		if (tessCont != null) tessCont.destroy();
		if (tessE != null) tessE.destroy();
		if (geom != null) geom.destroy();

		blend = override;
		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(name, id);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(id, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		CommonUniforms.addDynamicUniforms(uniformBuilder, FogMode.PER_VERTEX);
		customUniforms.assignTo(uniformBuilder);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniformBuilder);
		ProgramImages.Builder builder = ProgramImages.builder(id);
		pipeline.addGbufferOrShadowSamplers(samplerBuilder, builder, isShadowPass ? pipeline::getFlippedBeforeShadow : () -> translucent ? pipeline.getFlippedAfterTranslucent() : pipeline.getFlippedAfterPrepare(), isShadowPass, false, true, false);
		customUniforms.mapholderToPass(uniformBuilder, this);
		this.uniforms = uniformBuilder.buildUniforms();
		this.customUniforms = customUniforms;
		samplers = samplerBuilder.build();
		images = builder.build();

		modelOffsetUniform = tryGetUniformLocation2("modelOffset");
		worldYOffsetUniform = tryGetUniformLocation2("worldYOffset");
		mircoOffsetUniform = tryGetUniformLocation2("mircoOffset");
		projectionUniform = tryGetUniformLocation2("iris_ProjectionMatrix");
		projectionInverseUniform = tryGetUniformLocation2("iris_ProjectionMatrixInverse");
		modelViewUniform = tryGetUniformLocation2("iris_ModelViewMatrix");
		modelViewInverseUniform = tryGetUniformLocation2("iris_ModelViewMatrixInverse");
		normalMatrix3fUniform = tryGetUniformLocation2("iris_NormalMatrix");

		// Fog/Clip Uniforms
		clipDistanceUniform = tryGetUniformLocation2("clipDistance");
	}

	public static IrisLodRenderProgram createProgram(String name, boolean isShadowPass, boolean translucent, ProgramSource source, CustomUniforms uniforms, IrisRenderingPipeline pipeline) {
		Map<PatchShaderType, String> transformed = TransformPatcher.patchDH(
			name,
			source.getVertexSource().orElseThrow(RuntimeException::new),
			source.getTessControlSource().orElse(null),
			source.getTessEvalSource().orElse(null),
			source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(RuntimeException::new),
			pipeline.getTextureMap());
		String vertex = transformed.get(PatchShaderType.VERTEX);
		String tessControl = transformed.get(PatchShaderType.TESS_CONTROL);
		String tessEval = transformed.get(PatchShaderType.TESS_EVAL);
		String geometry = transformed.get(PatchShaderType.GEOMETRY);
		String fragment = transformed.get(PatchShaderType.FRAGMENT);
		ShaderPrinter.printProgram(name)
			.addSources(transformed)
			.setName("dh_" + name)
			.print();

		List<BufferBlendOverride> bufferOverrides = new ArrayList<>();

		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				bufferOverrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});

		return new IrisLodRenderProgram(name, isShadowPass, translucent, source.getDirectives().getBlendModeOverride().orElse(null), bufferOverrides.toArray(BufferBlendOverride[]::new), vertex, tessControl, tessEval, geometry, fragment, uniforms, pipeline);
	}

	// Noise Uniforms

	public int tryGetUniformLocation2(CharSequence name) {
		return GL32.glGetUniformLocation(this.id, name);
	}

	public void setUniform(int index, Matrix4f matrix) {
		if (index == -1 || matrix == null) return;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.callocFloat(16);
			matrix.get(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix4(index, false, buffer);
		}
	}

	public void setUniform(int index, Matrix3f matrix) {
		if (index == -1) return;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer buffer = stack.callocFloat(9);
			matrix.get(buffer);
			buffer.rewind();

			RenderSystem.glUniformMatrix3(index, false, buffer);
		}
	}

	// Override ShaderProgram.bind()
	public void bind() {
		GL43C.glUseProgram(id);
		if (blend != null) blend.apply();

		for (BufferBlendOverride override : bufferBlendOverrides) {
			override.apply();
		}
	}

	public void unbind() {
		GL43C.glUseProgram(0);
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		BlendModeOverride.restore();
	}

	public void free() {
		GL43C.glDeleteProgram(id);
	}

	public void fillUniformData(Matrix4f projection, Matrix4f modelView, int worldYOffset, float partialTicks) {
		GL43C.glUseProgram(id);

		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));
		setUniform(modelViewUniform, modelView);
		setUniform(modelViewInverseUniform, modelView.invert(new Matrix4f()));
		setUniform(projectionUniform, projection);
		setUniform(projectionInverseUniform, projection.invert(new Matrix4f()));
		setUniform(normalMatrix3fUniform, new Matrix4f(modelView).invert().transpose3x3(new Matrix3f()));

		setUniform(mircoOffsetUniform, 0.01f); // 0.01 block offset

		// setUniform(skyLightUniform, skyLight);

		if (worldYOffsetUniform != -1) setUniform(worldYOffsetUniform, (float) worldYOffset);

		// Fog/Clip Uniforms
		float dhNearClipDistance = DhApi.Delayed.renderProxy.getNearClipPlaneDistanceInBlocks(partialTicks);
		setUniform(clipDistanceUniform, dhNearClipDistance);

		samplers.update();
		uniforms.update();

		customUniforms.push(this);

		images.update();
	}

	private void setUniform(int index, float value) {
		GL43C.glUniform1f(index, value);
	}

	public void setModelPos(Vec3f modelPos) {
		setUniform(modelOffsetUniform, modelPos);
	}

	private void setUniform(int index, Vec3f pos) {
		GL43C.glUniform3f(index, pos.x, pos.y, pos.z);
	}

}
