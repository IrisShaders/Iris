package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.GlUniformMatrix3f;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
import net.coderbot.iris.gl.shader.GlShader;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.pipeline.ShaderPrinter;
import net.coderbot.iris.pipeline.newshader.FogMode;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.pipeline.transform.TransformPatcher;
import net.coderbot.iris.samplers.IrisSamplers;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.coderbot.iris.uniforms.CommonUniforms;
import net.coderbot.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.coderbot.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Map;

public class IrisLodRenderProgram
{
	private final int id;

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
	private final ProgramUniforms uniforms;
	private final CustomUniforms customUniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;
	private final BlendModeOverride blend;

	public static IrisLodRenderProgram createProgram(String name, boolean isShadowPass, boolean translucent, ProgramSource source, CustomUniforms uniforms, NewWorldRenderingPipeline pipeline) {
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
		return new IrisLodRenderProgram(name, isShadowPass, translucent, source.getDirectives().getBlendModeOverride().orElse(null), vertex, tessControl, tessEval, geometry, fragment, uniforms, pipeline);
	}

	public int tryGetUniformLocation2(CharSequence name) {
		int i = GL32.glGetUniformLocation(this.id, name);
		if (i == -1) Iris.logger.warn("Couldn't find " + name);
		return i;
	}

	// Noise Uniforms

	// This will bind  AbstractVertexAttribute
	private IrisLodRenderProgram(String name, boolean isShadowPass, boolean translucent, BlendModeOverride override, String vertex, String tessControl, String tessEval, String geometry, String fragment, CustomUniforms customUniforms, NewWorldRenderingPipeline pipeline)
	{
		id = GL43C.glCreateProgram();

		GL32.glBindAttribLocation(this.id, 0, "vPosition");
		GL32.glBindAttribLocation(this.id, 1, "color");
		GL32.glBindAttribLocation(this.id, 2, "irisExtra");

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
		pipeline.addGbufferOrShadowSamplers(samplerBuilder, builder, isShadowPass ? () -> pipeline.flippedBeforeShadow : () -> translucent ? pipeline.flippedAfterTranslucent : pipeline.flippedAfterPrepare, isShadowPass, new InputAvailability(false, true, false));
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
	public void bind()
	{
		GL43C.glUseProgram(id);
		if (blend != null) blend.apply();
	}

	public void unbind()
	{
		GL43C.glUseProgram(0);
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
		BlendModeOverride.restore();
	}

	public void free()
	{
		GL43C.glDeleteProgram(id);
	}

	public void  fillUniformData(Matrix4f projection, Matrix4f modelView, int worldYOffset, float partialTicks)
	{
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

	public void setModelPos(Vec3f modelPos)
	{
		setUniform(modelOffsetUniform, modelPos);
	}

	private void setUniform(int index, Vec3f pos) {
		GL43C.glUniform3f(index, pos.x, pos.y, pos.z);
	}

}
