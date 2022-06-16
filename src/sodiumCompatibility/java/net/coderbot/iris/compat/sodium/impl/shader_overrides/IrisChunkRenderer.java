package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.caffeinemc.gfx.api.array.VertexArrayDescription;
import net.caffeinemc.gfx.api.array.VertexArrayResourceBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeBinding;

import net.caffeinemc.gfx.api.buffer.MappedBuffer;
import net.caffeinemc.gfx.api.buffer.MappedBufferFlags;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.pipeline.Pipeline;
import net.caffeinemc.gfx.api.pipeline.PipelineState;
import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.gfx.api.shader.ShaderDescription;
import net.caffeinemc.gfx.api.shader.ShaderType;
import net.caffeinemc.gfx.api.types.ElementFormat;
import net.caffeinemc.gfx.api.types.PrimitiveType;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.buffer.StreamingBuffer;
import net.caffeinemc.sodium.render.chunk.draw.AbstractChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.RenderListBuilder;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.sequence.SequenceBuilder;
import net.caffeinemc.sodium.render.sequence.SequenceIndexBuffer;
import net.caffeinemc.sodium.render.shader.ShaderConstants;
import net.caffeinemc.sodium.render.shader.ShaderLoader;
import net.caffeinemc.sodium.render.shader.ShaderParser;
import net.caffeinemc.sodium.render.terrain.format.TerrainMeshAttribute;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.caffeinemc.sodium.util.TextureUtil;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class IrisChunkRenderer extends AbstractChunkRenderer {
	// TODO: should these be moved?
	public static final int CAMERA_MATRICES_SIZE = 192;
	public static final int FOG_PARAMETERS_SIZE = 32;
	private final StreamingBuffer bufferCameraMatrices;
	private final StreamingBuffer bufferInstanceData;
	private final StreamingBuffer bufferFogParameters;

	private Pipeline<IrisChunkShaderInterface, BufferTarget> pipeline;
	private final StreamingBuffer commandBuffer;

	private final SequenceIndexBuffer indexBuffer;

	private final boolean isShadowPass;
	private final ChunkRenderPass pass;
	private final VertexArrayDescription<BufferTarget> vertexArray;

	public IrisChunkRenderer(IrisChunkProgramOverrides overrides, boolean isShadowPass, RenderDevice device, StreamingBuffer instanceBuffer, StreamingBuffer commandBuffer, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		super(device, vertexType);

		var maxInFlightFrames = SodiumClientMod.options().advanced.cpuRenderAheadLimit + 1;

		final int alignment = device.properties().uniformBufferOffsetAlignment;
		this.bufferCameraMatrices = new StreamingBuffer(
			device,
			alignment,
			CAMERA_MATRICES_SIZE,
			maxInFlightFrames,
			MappedBufferFlags.EXPLICIT_FLUSH
		);
		this.bufferFogParameters = new StreamingBuffer(
			device,
			alignment,
			FOG_PARAMETERS_SIZE,
			maxInFlightFrames,
			MappedBufferFlags.EXPLICIT_FLUSH
		);

		this.bufferInstanceData = instanceBuffer;

		this.commandBuffer = commandBuffer;
		this.indexBuffer = indexBuffer;

		this.pass = pass;
		this.isShadowPass = isShadowPass;

		var vertexFormat = vertexType.getCustomVertexFormat();
		this.vertexArray = new VertexArrayDescription<>(BufferTarget.values(), List.of(
			new VertexArrayResourceBinding<>(BufferTarget.VERTICES, new VertexAttributeBinding[] {
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION,
					vertexFormat.getAttribute(TerrainMeshAttribute.POSITION)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_COLOR,
					vertexFormat.getAttribute(TerrainMeshAttribute.COLOR)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
					vertexFormat.getAttribute(TerrainMeshAttribute.BLOCK_TEXTURE)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
					vertexFormat.getAttribute(TerrainMeshAttribute.LIGHT_TEXTURE)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL)),
			})
		));

		this.pipeline = this.device.createPipeline(pass.pipelineDescription(), overrides.getProgramOverride(isShadowPass, device, pass, vertexType), vertexArray);
	}

	@Override
	public void render(RenderListBuilder.RenderList lists, ChunkRenderPass renderPass, ChunkRenderMatrices matrices, int frameIndex) {
		this.indexBuffer.ensureCapacity(lists.getLargestVertexIndex());
		this.pipeline.getProgram().getInterface().setup();

		this.device.usePipeline(this.pipeline, (cmd, programInterface, pipelineState) -> {
			this.setupTextures(renderPass, pipelineState);
			this.setupUniforms(matrices, programInterface, pipelineState, frameIndex);

			cmd.bindCommandBuffer(this.commandBuffer.getBuffer());
			cmd.bindElementBuffer(this.indexBuffer.getBuffer());

			for (var batch : lists.getBatches()) {
				pipelineState.bindBufferBlock(
					programInterface.storageInstanceData,
					this.bufferInstanceData.getBuffer(),
					batch.getInstanceBufferOffset(),
					batch.getInstanceBufferLength()
				);

				cmd.bindVertexBuffer(
					BufferTarget.VERTICES,
					batch.getVertexBuffer(),
					0,
					batch.getVertexStride()
				);

				cmd.multiDrawElementsIndirect(
					PrimitiveType.TRIANGLES,
					ElementFormat.UNSIGNED_INT,
					batch.getCommandBufferOffset(),
					batch.getCommandCount()
				);
			}
		});

		this.pipeline.getProgram().getInterface().restore();
	}

	private void setupTextures(ChunkRenderPass pass, PipelineState pipelineState) {
		pipelineState.bindTexture(0, TextureUtil.getBlockAtlasTexture(), pass.mipped() ? this.blockTextureMippedSampler : this.blockTextureSampler);
		pipelineState.bindTexture(2, TextureUtil.getLightTexture(), this.lightTextureSampler);
	}

	private void setupUniforms(ChunkRenderMatrices renderMatrices, ChunkShaderInterface programInterface, PipelineState state, int frameIndex) {
		var matricesSection = this.bufferCameraMatrices.getSection(frameIndex);
		var matricesBuf = matricesSection.getView();

		renderMatrices.projection()
			.get(0, matricesBuf);
		renderMatrices.modelView()
			.get(64, matricesBuf);

		var mvpMatrix = new Matrix4f();
		mvpMatrix.set(renderMatrices.projection());
		mvpMatrix.mul(renderMatrices.modelView());
		mvpMatrix
			.get(128, matricesBuf);

		matricesSection.flushFull();

		state.bindBufferBlock(programInterface.uniformCameraMatrices, matricesSection.getBuffer(), matricesSection.getOffset(), matricesSection.getView().capacity());

		var fogParamsSection = this.bufferFogParameters.getSection(frameIndex);
		var fogParamsBuf = fogParamsSection.getView();

		var paramFogColor = RenderSystem.getShaderFogColor();
		fogParamsBuf.putFloat(0, paramFogColor[0]);
		fogParamsBuf.putFloat(4, paramFogColor[1]);
		fogParamsBuf.putFloat(8, paramFogColor[2]);
		fogParamsBuf.putFloat(12, paramFogColor[3]);
		fogParamsBuf.putFloat(16, RenderSystem.getShaderFogStart());
		fogParamsBuf.putFloat(20, RenderSystem.getShaderFogEnd());
		fogParamsBuf.putInt(24, RenderSystem.getShaderFogShape().getIndex());

		fogParamsSection.flushFull();

		state.bindBufferBlock(programInterface.uniformFogParameters, fogParamsSection.getBuffer(), fogParamsSection.getOffset(), fogParamsSection.getView().capacity());
	}

	private static ShaderConstants getShaderConstants(ChunkRenderPass pass, TerrainVertexType vertexType) {
		var constants = ShaderConstants.builder();

		if (pass.isCutout()) {
			constants.add("ALPHA_CUTOFF", String.valueOf(pass.alphaCutoff()));
		}

		if (!Mth.equal(vertexType.getVertexRange(), 1.0f)) {
			constants.add("VERT_SCALE", String.valueOf(vertexType.getVertexRange()));
		}

		return constants.build();
	}

	@Override
	public void delete() {
		super.delete();

		this.device.deletePipeline(this.pipeline);

		this.bufferFogParameters.delete();
		this.bufferCameraMatrices.delete();
	}

	public void deletePipeline(IrisChunkProgramOverrides overrides) {
		this.pipeline = this.device.createPipeline(pass.pipelineDescription(), overrides.getProgramOverride(isShadowPass, device, pass, vertexType), vertexArray);
	}


	public static enum BufferTarget {
		VERTICES;

		private BufferTarget() {
		}
	}
}
