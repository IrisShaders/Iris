package net.coderbot.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.seibel.distanthorizons.core.config.Config;
import com.seibel.distanthorizons.core.dependencyInjection.SingletonInjector;
import com.seibel.distanthorizons.core.render.glObject.GLProxy;
import com.seibel.distanthorizons.core.render.glObject.shader.Shader;
import com.seibel.distanthorizons.core.render.glObject.shader.ShaderProgram;
import com.seibel.distanthorizons.core.render.glObject.vertexAttribute.AbstractVertexAttribute;
import com.seibel.distanthorizons.core.render.glObject.vertexAttribute.VertexAttributePostGL43;
import com.seibel.distanthorizons.core.render.glObject.vertexAttribute.VertexAttributePreGL43;
import com.seibel.distanthorizons.core.render.glObject.vertexAttribute.VertexPointer;
import com.seibel.distanthorizons.core.util.LodUtil;
import com.seibel.distanthorizons.core.render.fog.LodFogConfig;
import com.seibel.distanthorizons.core.util.RenderUtil;
import com.seibel.distanthorizons.coreapi.util.math.Mat4f;
import com.seibel.distanthorizons.coreapi.util.math.Vec3f;
import com.seibel.distanthorizons.core.wrapperInterfaces.IVersionConstants;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformFloat3v;
import me.jellysquid.mods.sodium.client.gl.shader.uniform.GlUniformMatrix4f;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.GlUniformMatrix3f;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.program.ProgramImages;
import net.coderbot.iris.gl.program.ProgramSamplers;
import net.coderbot.iris.gl.program.ProgramUniforms;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Map;

public class IrisLodRenderProgram extends ShaderProgram
{
	private static final IVersionConstants VERSION_CONSTANTS = SingletonInjector.INSTANCE.get(IVersionConstants.class);

	public final AbstractVertexAttribute vao;

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

	public static IrisLodRenderProgram createProgram(String name, ProgramSource source, CustomUniforms uniforms, NewWorldRenderingPipeline pipeline) {
		Map<PatchShaderType, String> transformed = TransformPatcher.patchDH(
			name,
			source.getVertexSource().orElseThrow(RuntimeException::new),
			source.getFragmentSource().orElseThrow(RuntimeException::new),
			pipeline.getTextureMap());
		String vertex2 = transformed.get(PatchShaderType.VERTEX);
		String fragment2 = transformed.get(PatchShaderType.FRAGMENT);
		Iris.logger.error("GOT A DH PROGRAM (not an error)");
		ShaderPrinter.printProgram(name)
			.addSources(transformed)
			.setName("dh_" + name)
			.print();
		return new IrisLodRenderProgram(name, vertex2, fragment2, uniforms, pipeline);
	}

	public int tryGetUniformLocation2(CharSequence name) {
		int i = GL32.glGetUniformLocation(this.id, name);
		if (i == -1) Iris.logger.warn("Couldn't find " + name);
		return i;
	}

	// Noise Uniforms

	// This will bind  AbstractVertexAttribute
	private IrisLodRenderProgram(String name, String vertex, String fragment, CustomUniforms customUniforms, NewWorldRenderingPipeline pipeline)
	{
		super(() -> vertex,
			() -> fragment,
			"fragColor", new String[]{"vPosition", "color"});

		ProgramUniforms.Builder uniformBuilder = ProgramUniforms.builder(name, id);
		ProgramSamplers.Builder samplerBuilder = ProgramSamplers.builder(id, IrisSamplers.WORLD_RESERVED_TEXTURE_UNITS);
		CommonUniforms.addDynamicUniforms(uniformBuilder, FogMode.PER_VERTEX);
		customUniforms.assignTo(uniformBuilder);
		BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniformBuilder);
		ProgramImages.Builder builder = ProgramImages.builder(id);
		pipeline.addGbufferOrShadowSamplers(samplerBuilder, builder, () -> pipeline.flippedAfterPrepare, false /* CHANGE WHEN SHADOWS SUPPORTED!!!*/, new InputAvailability(false, true, false));
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

		// TODO: Add better use of the LODFormat thing
		int vertexByteCount = LodUtil.LOD_VERTEX_FORMAT.getByteSize();
		if (GLProxy.getInstance().VertexAttributeBufferBindingSupported)
			vao = new VertexAttributePostGL43(); // also binds AbstractVertexAttribute
		else
			vao = new VertexAttributePreGL43(); // also binds AbstractVertexAttribute
		vao.bind();
		// Now a pos+light.
		vao.setVertexAttribute(0, 0, VertexPointer.addUnsignedShortsPointer(4, false, true)); // 2+2+2+2
		vao.setVertexAttribute(0, 1, VertexPointer.addUnsignedBytesPointer(4, true, false)); // +4
		try
		{
			vao.completeAndCheck(vertexByteCount);
		}
		catch (RuntimeException e)
		{
			System.out.println(LodUtil.LOD_VERTEX_FORMAT);
			throw e;
		}
	}

	public void setUniform(int index, Matrix4f matrix) {
		if (index == -1) return;

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
		super.bind();
	}
	// Override ShaderProgram.unbind()
	public void unbind()
	{
		super.unbind();
		ProgramUniforms.clearActiveUniforms();
		ProgramSamplers.clearActiveSamplers();
	}

	// Override ShaderProgram.free()
	public void free()
	{
		vao.free();
		super.free();
	}

	public void bindVertexBuffer(int vbo)
	{
		vao.bindBufferToAllBindingPoints(vbo);
	}

	public void unbindVertexBuffer()
	{
		vao.unbindBuffersFromAllBindingPoint();
	}

	public void fillUniformData(Matrix4f projection, Matrix4f modelView, int worldYOffset, float partialTicks)
	{
		super.bind();

		IrisRenderSystem.bindTextureToUnit(TextureType.TEXTURE_2D.getGlType(), IrisSamplers.LIGHTMAP_TEXTURE_UNIT, RenderSystem.getShaderTexture(2));

		setUniform(modelViewUniform, modelView);
		setUniform(modelViewInverseUniform, modelView.invert(new Matrix4f()));
		setUniform(projectionUniform, projection);
		setUniform(projectionInverseUniform, projection.invert(new Matrix4f()));
		setUniform(normalMatrix3fUniform, modelView.transpose3x3(new Matrix3f()));

		setUniform(mircoOffsetUniform, 0.01f); // 0.01 block offset

		// setUniform(skyLightUniform, skyLight);

		if (worldYOffsetUniform != -1) setUniform(worldYOffsetUniform, (float) worldYOffset);

		// Fog/Clip Uniforms
		float dhNearClipDistance = RenderUtil.getNearClipPlaneDistanceInBlocks(partialTicks);
		setUniform(clipDistanceUniform, dhNearClipDistance);

		samplers.update();
		uniforms.update();

		customUniforms.push(this);

		images.update();
	}

	public void setModelPos(Vec3f modelPos)
	{
		setUniform(modelOffsetUniform, modelPos);
	}

}
