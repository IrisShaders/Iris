package net.irisshaders.iris.compat.dh;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiGenericObjectShaderProgram;
import com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderableBoxGroup;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.math.DhApiMat4f;
import com.seibel.distanthorizons.api.objects.math.DhApiVec3d;
import com.seibel.distanthorizons.api.objects.math.DhApiVec3f;
import com.seibel.distanthorizons.api.objects.math.DhApiVec3i;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBoxGroupShading;
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
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IrisGenericRenderProgram implements IDhApiGenericObjectShaderProgram {
	// Uniforms
	public final int modelViewUniform;
	public final int modelViewInverseUniform;
	public final int projectionUniform;
	public final int projectionInverseUniform;
	public final int normalMatrix3fUniform;
	// Fog/Clip Uniforms
	private final int id;
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private final BlendModeOverride blend;
	private final BufferBlendOverride[] bufferBlendOverrides;

	private final int instancedShaderOffsetChunkUniform;
	private final int instancedShaderOffsetSubChunkUniform;
	private final int instancedShaderCameraChunkPosUniform;
	private final int instancedShaderCameraSubChunkPosUniform;
	private final int instancedShaderProjectionModelViewMatrixUniform;
	private final int va;
	private final int uBlockLight;
	private final int uSkyLight;

	// This will bind  AbstractVertexAttribute
	private IrisGenericRenderProgram(String name, boolean isShadowPass, boolean translucent, BlendModeOverride override, BufferBlendOverride[] bufferBlendOverrides, String vertex, String tessControl, String tessEval, String geometry, String fragment, CustomUniforms customUniforms, IrisRenderingPipeline pipeline) {
		id = GL43C.glCreateProgram();

		GL32.glBindAttribLocation(this.id, 0, "vPosition");

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

		this.va = GlStateManager._glGenVertexArrays();
		GlStateManager._glBindVertexArray(va);
		GL32.glVertexAttribPointer(0, 3, GL32.GL_FLOAT, false, 0, 0);
		GL32.glEnableVertexAttribArray(0);

		projectionUniform = tryGetUniformLocation2("iris_ProjectionMatrix");
		projectionInverseUniform = tryGetUniformLocation2("iris_ProjectionMatrixInverse");
		modelViewUniform = tryGetUniformLocation2("iris_ModelViewMatrix");
		modelViewInverseUniform = tryGetUniformLocation2("iris_ModelViewMatrixInverse");
		normalMatrix3fUniform = tryGetUniformLocation2("iris_NormalMatrix");

		this.instancedShaderOffsetChunkUniform = this.tryGetUniformLocation2("uOffsetChunk");
		this.instancedShaderOffsetSubChunkUniform = this.tryGetUniformLocation2("uOffsetSubChunk");
		this.instancedShaderCameraChunkPosUniform = this.tryGetUniformLocation2("uCameraPosChunk");
		this.instancedShaderCameraSubChunkPosUniform = this.tryGetUniformLocation2("uCameraPosSubChunk");
		this.instancedShaderProjectionModelViewMatrixUniform = this.tryGetUniformLocation2("uProjectionMvm");
		this.uBlockLight = this.tryGetUniformLocation2("uBlockLight");
		this.uSkyLight = this.tryGetUniformLocation2("uSkyLight");
	}

	public static IrisGenericRenderProgram createProgram(String name, boolean isShadowPass, boolean translucent, ProgramSource source, CustomUniforms uniforms, IrisRenderingPipeline pipeline) {
		Map<PatchShaderType, String> transformed = TransformPatcher.patchDHGeneric(
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
		ShaderPrinter.printProgram(name + "_g")
			.addSources(transformed)
			.setName("dh_" + name + "_g")
			.print();

		List<BufferBlendOverride> bufferOverrides = new ArrayList<>();

		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				bufferOverrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});

		return new IrisGenericRenderProgram(name, isShadowPass, translucent, source.getDirectives().getBlendModeOverride().orElse(null), bufferOverrides.toArray(BufferBlendOverride[]::new), vertex, tessControl, tessEval, geometry, fragment, uniforms, pipeline);
	}

	// Noise Uniforms

	private static int getChunkPosFromDouble(double value) {
		return (int) Math.floor(value / 16);
	}

	private static float getSubChunkPosFromDouble(double value) {
		double chunkPos = Math.floor(value / 16);
		return (float) (value - chunkPos * 16);
	}

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
	public void bind(DhApiRenderParam renderParam) {
		GlStateManager._glBindVertexArray(va);
		GL32C.glUseProgram(id);
		if (blend != null) blend.apply();

		for (BufferBlendOverride override : bufferBlendOverrides) {
			override.apply();
		}

		setUniform(modelViewUniform, toJOML(renderParam.dhModelViewMatrix));
		setUniform(modelViewInverseUniform, toJOML(renderParam.dhModelViewMatrix).invert());
		setUniform(projectionUniform, toJOML(renderParam.dhProjectionMatrix));
		setUniform(projectionInverseUniform, toJOML(renderParam.dhModelViewMatrix).invert());
		setUniform(normalMatrix3fUniform, toJOML(renderParam.dhModelViewMatrix).invert().transpose3x3(new Matrix3f()));
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));
		this.setUniform(this.instancedShaderProjectionModelViewMatrixUniform, toJOML(renderParam.dhProjectionMatrix).mul(toJOML(renderParam.dhModelViewMatrix)));

		samplers.update();
		uniforms.update();

		customUniforms.push(this);

		images.update();
	}

	public void unbind() {
		GlStateManager._glBindVertexArray(0);
		GL43C.glUseProgram(0);
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		BlendModeOverride.restore();
	}

	@Override
	public void bindVertexBuffer(int i) {
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, i);
		GL32.glVertexAttribPointer(0, 3, GL32.GL_FLOAT, false, 12, 0);
	}

	@Override
	public boolean overrideThisFrame() {
		return Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline;
	}

	@Override
	public int getId() {
		return id;
	}

	public void free() {
		GL43C.glDeleteProgram(id);
	}

	public void fillIndirectUniformData(DhApiRenderParam dhApiRenderParam, DhApiRenderableBoxGroupShading dhApiRenderableBoxGroupShading, IDhApiRenderableBoxGroup boxGroup, DhApiVec3d camPos) {
		bind(dhApiRenderParam);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL30C.GL_LEQUAL);
		this.setUniform(this.instancedShaderOffsetChunkUniform,
			new DhApiVec3i(
				getChunkPosFromDouble(boxGroup.getOriginBlockPos().x),
				getChunkPosFromDouble(boxGroup.getOriginBlockPos().y),
				getChunkPosFromDouble(boxGroup.getOriginBlockPos().z)
			));
		this.setUniform(this.instancedShaderOffsetSubChunkUniform,
			new DhApiVec3f(
				getSubChunkPosFromDouble(boxGroup.getOriginBlockPos().x),
				getSubChunkPosFromDouble(boxGroup.getOriginBlockPos().y),
				getSubChunkPosFromDouble(boxGroup.getOriginBlockPos().z)
			));

		this.setUniform(this.instancedShaderCameraChunkPosUniform,
			new DhApiVec3i(
				getChunkPosFromDouble(camPos.x),
				getChunkPosFromDouble(camPos.y),
				getChunkPosFromDouble(camPos.z)
			));
		this.setUniform(this.instancedShaderCameraSubChunkPosUniform,
			new DhApiVec3f(
				getSubChunkPosFromDouble(camPos.x),
				getSubChunkPosFromDouble(camPos.y),
				getSubChunkPosFromDouble(camPos.z)
			));
		this.setUniform(this.uBlockLight,
			boxGroup.getBlockLight());
		this.setUniform(this.uSkyLight,
			boxGroup.getSkyLight());

	}

	@Override
	public void fillSharedDirectUniformData(DhApiRenderParam dhApiRenderParam, DhApiRenderableBoxGroupShading dhApiRenderableBoxGroupShading, IDhApiRenderableBoxGroup iDhApiRenderableBoxGroup, DhApiVec3d dhApiVec3d) {
		throw new IllegalStateException("Only indirect is supported with Iris.");
	}

	@Override
	public void fillDirectUniformData(DhApiRenderParam dhApiRenderParam, IDhApiRenderableBoxGroup iDhApiRenderableBoxGroup, DhApiRenderableBox dhApiRenderableBox, DhApiVec3d dhApiVec3d) {
		throw new IllegalStateException("Only indirect is supported with Iris.");
	}

	private Matrix4f toJOML(DhApiMat4f mat4f) {
		return new Matrix4f().setTransposed(mat4f.getValuesAsArray());
	}

	private void setUniform(int index, int value) {
		GL43C.glUniform1i(index, value);
	}

	private void setUniform(int index, float value) {
		GL43C.glUniform1f(index, value);
	}

	private void setUniform(int index, DhApiVec3f pos) {
		GL43C.glUniform3f(index, pos.x, pos.y, pos.z);
	}

	private void setUniform(int index, DhApiVec3i pos) {
		GL43C.glUniform3i(index, pos.x, pos.y, pos.z);
	}

}
