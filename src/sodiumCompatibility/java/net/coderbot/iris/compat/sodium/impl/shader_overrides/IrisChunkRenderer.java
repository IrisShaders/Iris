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
import net.caffeinemc.sodium.render.chunk.draw.ChunkPrep;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.ShaderChunkRenderer;
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

public class IrisChunkRenderer extends ShaderChunkRenderer<IrisChunkShaderInterface> {
	private final MappedBuffer bufferCameraMatrices;
	private final MappedBuffer bufferCameraMatricesShadow;
	private final MappedBuffer bufferFogParameters;

	private final SequenceIndexBuffer sequenceIndexBuffer;

	private final Map<ChunkRenderPass, Pipeline<IrisChunkShaderInterface, BufferTarget>> shadowPipelines = new Object2ObjectOpenHashMap();
	private final Map<ChunkRenderPass, Program<IrisChunkShaderInterface>> shadowPrograms = new Object2ObjectOpenHashMap();

	private final Map<ChunkRenderPass, Pipeline<IrisChunkShaderInterface, BufferTarget>> gbufferPipelines = new Object2ObjectOpenHashMap();
	private final Map<ChunkRenderPass, Program<IrisChunkShaderInterface>> gbufferPrograms = new Object2ObjectOpenHashMap();

	private final IrisChunkProgramOverrides irisChunkProgramOverrides;

	public IrisChunkRenderer(RenderDevice device, TerrainVertexType vertexType) {
		super(device, vertexType);

		var storageFlags = EnumSet.of(BufferStorageFlags.WRITABLE, BufferStorageFlags.COHERENT, BufferStorageFlags.PERSISTENT);
		var mapFlags = EnumSet.of(BufferMapFlags.WRITE, BufferMapFlags.COHERENT, BufferMapFlags.PERSISTENT);

		this.bufferCameraMatrices = device.createMappedBuffer(192, storageFlags, mapFlags);
		this.bufferCameraMatricesShadow = device.createMappedBuffer(192, storageFlags, mapFlags);
		this.bufferFogParameters = device.createMappedBuffer(24, storageFlags, mapFlags);

		this.sequenceIndexBuffer = new SequenceIndexBuffer(device, SequenceBuilder.QUADS);

		this.irisChunkProgramOverrides = new IrisChunkProgramOverrides();
	}

	@Override
	public void render(ChunkPrep.PreparedRenderList lists, ChunkRenderPass renderPass, ChunkRenderMatrices matrices) {
		this.sequenceIndexBuffer.ensureCapacity(lists.largestVertexIndex());

		this.device.usePipeline(this.getPipeline(renderPass), (cmd, programInterface, pipelineState) -> {
			programInterface.setup();
			this.setupTextures(renderPass, pipelineState);
			this.setupUniforms(matrices, programInterface, pipelineState);

			for (var batch : lists.batches()) {
				pipelineState.bindUniformBlock(programInterface.uniformInstanceData, lists.instanceBuffer(),
					batch.instanceData().offset(), batch.instanceData().length());

				cmd.bindVertexBuffer(BufferTarget.VERTICES, batch.vertexBuffer(), 0, this.vertexFormat.stride());
				cmd.bindElementBuffer(this.sequenceIndexBuffer.getBuffer());

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

	private void setupUniforms(ChunkRenderMatrices matrices, ChunkShaderInterface programInterface, PipelineState state) {
		boolean isShadowPass = ShadowRenderingState.areShadowsCurrentlyBeingRendered();
		var bufMatrices = (isShadowPass ? bufferCameraMatricesShadow : bufferCameraMatrices).view();

		matrices.projection()
			.get(0, bufMatrices);
		matrices.modelView()
			.get(64, bufMatrices);

		var mvpMatrix = new Matrix4f();
		mvpMatrix.set(matrices.projection());
		mvpMatrix.mul(matrices.modelView());
		mvpMatrix
			.get(128, bufMatrices);

		(isShadowPass ? bufferCameraMatricesShadow : bufferCameraMatrices).flush();

		var bufFogParameters = this.bufferFogParameters.view();
		var paramFogColor = RenderSystem.getShaderFogColor();
		bufFogParameters.putFloat(0, paramFogColor[0]);
		bufFogParameters.putFloat(4, paramFogColor[1]);
		bufFogParameters.putFloat(8, paramFogColor[2]);
		bufFogParameters.putFloat(12, paramFogColor[3]);
		bufFogParameters.putFloat(16, RenderSystem.getShaderFogStart());
		bufFogParameters.putFloat(20, RenderSystem.getShaderFogEnd());

		this.bufferFogParameters.flush();

		state.bindUniformBlock(programInterface.uniformFogParameters, this.bufferFogParameters);
		state.bindUniformBlock(programInterface.uniformCameraMatrices, (isShadowPass ? bufferCameraMatricesShadow : bufferCameraMatrices));
	}

	@Override
	protected Program<IrisChunkShaderInterface> createProgram(ChunkRenderPass pass) {
		return null; // No-op
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
	protected Pipeline<IrisChunkShaderInterface, BufferTarget> getPipeline(ChunkRenderPass pass) {
		if (Iris.getPipelineManager().isSodiumShaderReloadNeeded()) {
			for (var pipeline : this.gbufferPipelines.values()) {
				this.device.deletePipeline(pipeline);
			}

			this.gbufferPipelines.clear();

			for (var program : this.gbufferPrograms.values()) {
				if (((GlObjectExt) program).isHandleValid()) {
					this.device.deleteProgram(program);
				}
			}

			this.gbufferPrograms.clear();

			deletePrograms();
		}

		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			return this.shadowPipelines.computeIfAbsent(pass, this::createShadowPipeline);
		} else {
			return this.gbufferPipelines.computeIfAbsent(pass, this::createGbufferPipeline);
		}
	}

	private Pipeline<IrisChunkShaderInterface, ShaderChunkRenderer.BufferTarget> createGbufferPipeline(ChunkRenderPass pass) {
		Program<IrisChunkShaderInterface> program = this.getIrisProgram(false, pass);
		VertexArrayDescription<BufferTarget> vertexArray =  new VertexArrayDescription<>(BufferTarget.values(), List.of(
			new VertexArrayResourceBinding<>(BufferTarget.VERTICES, new VertexAttributeBinding[] {
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.POSITION)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_COLOR,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.COLOR)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.BLOCK_TEXTURE)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.LIGHT_TEXTURE)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
					this.vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))})));
		return this.device.createPipeline(pass.pipelineDescription(), program, vertexArray);
	}

	private Pipeline<IrisChunkShaderInterface, ShaderChunkRenderer.BufferTarget> createShadowPipeline(ChunkRenderPass pass) {
		Program<IrisChunkShaderInterface> program = this.getIrisProgram(true, pass);
		VertexArrayDescription<BufferTarget> vertexArray = new VertexArrayDescription<>(ShaderChunkRenderer.BufferTarget.values(), List.of(
			new VertexArrayResourceBinding<>(BufferTarget.VERTICES, new VertexAttributeBinding[] {
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.POSITION)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_COLOR,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.COLOR)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.BLOCK_TEXTURE)),
				new VertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
					this.vertexFormat.getAttribute(TerrainMeshAttribute.LIGHT_TEXTURE)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
					this.vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))})));
		return this.device.createPipeline(pass.pipelineDescription(), program, vertexArray);
	}

	private void deletePrograms() {
		for (var pipeline : this.gbufferPipelines.values()) {
			this.device.deletePipeline(pipeline);
		}

		this.gbufferPipelines.clear();

		for (var program : this.gbufferPrograms.values()) {
			if (((GlObjectExt) program).isHandleValid()) {
				this.device.deleteProgram(program);
			}
		}

		this.gbufferPrograms.clear();

		for (var pipeline : this.shadowPipelines.values()) {
			this.device.deletePipeline(pipeline);
		}

		this.shadowPipelines.clear();

		for (var program : this.shadowPrograms.values()) {
			if (((GlObjectExt) program).isHandleValid()) {
				this.device.deleteProgram(program);
			}
		}

		this.shadowPrograms.clear();

		irisChunkProgramOverrides.deleteShaders(this.device);
	}

	public Program<IrisChunkShaderInterface> getIrisProgram(boolean isShadowPass, ChunkRenderPass pass) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return irisChunkProgramOverrides.getProgramOverride(isShadowPass, device, pass, vertexType);
		} else {
			return null;
		}
	}
	@Override
	public void delete() {
		super.delete();

		deletePrograms();

		this.device.deleteBuffer(this.bufferFogParameters);
		this.device.deleteBuffer(this.bufferCameraMatrices);
		this.device.deleteBuffer(this.bufferCameraMatricesShadow);
	}
}
