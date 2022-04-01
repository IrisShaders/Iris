package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.caffeinemc.gfx.api.array.VertexArrayDescription;
import net.caffeinemc.gfx.api.array.VertexArrayResourceBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeBinding;
import net.caffeinemc.gfx.api.buffer.BufferMapFlags;
import net.caffeinemc.gfx.api.buffer.BufferStorageFlags;
import net.caffeinemc.gfx.api.buffer.MappedBuffer;
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
import net.caffeinemc.sodium.render.chunk.draw.ChunkPrep;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.DefaultChunkRenderer;
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
	private final StreamingBuffer bufferCameraMatrices;
	private final StreamingBuffer bufferFogParameters;

	private Pipeline<IrisChunkShaderInterface, BufferTarget> pipeline;

	private final SequenceIndexBuffer indexBuffer;

	private final boolean isShadowPass;
	private final ChunkRenderPass pass;
	private final VertexArrayDescription<BufferTarget> vertexArray;

	public IrisChunkRenderer(IrisChunkProgramOverrides overrides, boolean isShadowPass, RenderDevice device, SequenceIndexBuffer indexBuffer, TerrainVertexType vertexType, ChunkRenderPass pass) {
		super(device, vertexType);

		var storageFlags = EnumSet.of(BufferStorageFlags.WRITABLE, BufferStorageFlags.PERSISTENT);
		var mapFlags = EnumSet.of(BufferMapFlags.WRITE, BufferMapFlags.EXPLICIT_FLUSH, BufferMapFlags.PERSISTENT, BufferMapFlags.UNSYNCHRONIZED);

		var maxInFlightFrames = SodiumClientMod.options().advanced.cpuRenderAheadLimit + 1;

		this.bufferCameraMatrices = new StreamingBuffer(device, storageFlags, mapFlags, 192, maxInFlightFrames);
		this.bufferFogParameters = new StreamingBuffer(device, storageFlags, mapFlags, 24, maxInFlightFrames);

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
	public void render(ChunkPrep.PreparedRenderList lists, ChunkRenderPass renderPass, ChunkRenderMatrices matrices, int frameIndex) {
		this.indexBuffer.ensureCapacity(lists.largestVertexIndex());

		if (this.pipeline.getProgram() == null) return;

		this.device.usePipeline(this.pipeline, (cmd, programInterface, pipelineState) -> {
			programInterface.setup();
			this.setupTextures(renderPass, pipelineState);
			this.setupUniforms(matrices, programInterface, pipelineState, frameIndex);

			for (var batch : lists.batches()) {
				pipelineState.bindUniformBlock(programInterface.uniformInstanceData, lists.instanceBuffer(),
					batch.instanceData().offset(), batch.instanceData().length());

				cmd.bindVertexBuffer(BufferTarget.VERTICES, batch.vertexBuffer(), 0, batch.vertexStride());
				cmd.bindElementBuffer(this.indexBuffer.getBuffer());

				cmd.multiDrawElementsIndirect(lists.commandBuffer(), batch.commandData().offset(), batch.commandCount(),
					ElementFormat.UNSIGNED_INT, PrimitiveType.TRIANGLES);
			}

			programInterface.restore();
		});
	}

	private void setupTextures(ChunkRenderPass pass, PipelineState pipelineState) {
		pipelineState.bindTexture(0, TextureUtil.getBlockAtlasTexture(), pass.mipped() ? this.blockTextureMippedSampler : this.blockTextureSampler);
		pipelineState.bindTexture(2, TextureUtil.getLightTexture(), this.lightTextureSampler);
	}

	private void setupUniforms(ChunkRenderMatrices renderMatrices, ChunkShaderInterface programInterface, PipelineState state, int frameIndex) {
		var matrices = this.bufferCameraMatrices.slice(frameIndex);
		var matricesBuf = matrices.view();

		renderMatrices.projection()
			.get(0, matricesBuf);
		renderMatrices.modelView()
			.get(64, matricesBuf);

		var mvpMatrix = new Matrix4f();
		mvpMatrix.set(renderMatrices.projection());
		mvpMatrix.mul(renderMatrices.modelView());
		mvpMatrix
			.get(128, matricesBuf);

		bufferCameraMatrices.flush(matrices);

		state.bindUniformBlock(programInterface.uniformCameraMatrices, matrices.buffer(), matrices.offset(), matrices.length());

		var fogParams = this.bufferFogParameters.slice(frameIndex);
		var fogParamsBuf = fogParams.view();

		var paramFogColor = RenderSystem.getShaderFogColor();
		fogParamsBuf.putFloat(0, paramFogColor[0]);
		fogParamsBuf.putFloat(4, paramFogColor[1]);
		fogParamsBuf.putFloat(8, paramFogColor[2]);
		fogParamsBuf.putFloat(12, paramFogColor[3]);
		fogParamsBuf.putFloat(16, RenderSystem.getShaderFogStart());
		fogParamsBuf.putFloat(20, RenderSystem.getShaderFogEnd());

		this.bufferFogParameters.flush(fogParams);

		state.bindUniformBlock(programInterface.uniformFogParameters, fogParams.buffer(), fogParams.offset(), fogParams.length());
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
