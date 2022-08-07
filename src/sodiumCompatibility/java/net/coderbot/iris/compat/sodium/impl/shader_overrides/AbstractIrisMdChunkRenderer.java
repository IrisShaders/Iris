package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import it.unimi.dsi.fastutil.longs.LongList;
import net.caffeinemc.gfx.api.array.VertexArrayDescription;
import net.caffeinemc.gfx.api.array.VertexArrayResourceBinding;
import net.caffeinemc.gfx.api.array.attribute.VertexAttributeBinding;
import net.caffeinemc.gfx.api.buffer.Buffer;
import net.caffeinemc.gfx.api.buffer.MappedBufferFlags;
import net.caffeinemc.gfx.api.device.RenderDevice;
import net.caffeinemc.gfx.api.device.commands.RenderCommandList;
import net.caffeinemc.gfx.api.pipeline.PipelineState;
import net.caffeinemc.gfx.api.pipeline.RenderPipeline;
import net.caffeinemc.gfx.api.pipeline.RenderPipelineDescription;
import net.caffeinemc.gfx.api.pipeline.state.BlendFunc;
import net.caffeinemc.gfx.api.pipeline.state.CullMode;
import net.caffeinemc.gfx.api.shader.Program;
import net.caffeinemc.gfx.opengl.texture.GlTexture;
import net.caffeinemc.gfx.util.buffer.streaming.DualStreamingBuffer;
import net.caffeinemc.gfx.util.buffer.streaming.SequenceBuilder;
import net.caffeinemc.gfx.util.buffer.streaming.SequenceIndexBuffer;
import net.caffeinemc.gfx.util.buffer.streaming.StreamingBuffer;
import net.caffeinemc.sodium.SodiumClientMod;
import net.caffeinemc.sodium.render.chunk.RenderSection;
import net.caffeinemc.sodium.render.chunk.draw.AbstractChunkRenderer;
import net.caffeinemc.sodium.render.chunk.draw.ChunkCameraContext;
import net.caffeinemc.sodium.render.chunk.draw.ChunkRenderMatrices;
import net.caffeinemc.sodium.render.chunk.draw.SortedTerrainLists;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPass;
import net.caffeinemc.sodium.render.chunk.passes.ChunkRenderPassManager;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderBindingPoints;
import net.caffeinemc.sodium.render.chunk.shader.ChunkShaderInterface;
import net.caffeinemc.sodium.render.chunk.state.ChunkPassModel;
import net.caffeinemc.sodium.render.chunk.state.ChunkRenderBounds;
import net.caffeinemc.sodium.render.shader.ShaderConstants;
import net.caffeinemc.sodium.render.terrain.format.TerrainMeshAttribute;
import net.caffeinemc.sodium.render.terrain.format.TerrainVertexType;
import net.caffeinemc.sodium.render.terrain.quad.properties.ChunkMeshFace;
import net.caffeinemc.sodium.util.MathUtil;
import net.caffeinemc.sodium.util.TextureUtil;
import net.coderbot.iris.compat.sodium.impl.IrisChunkShaderBindingPoints;
import net.coderbot.iris.compat.sodium.impl.vertex_format.IrisChunkMeshAttributes;
import net.coderbot.iris.shadows.ShadowRenderingState;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

// TODO: abstract buffer targets, abstract VAO creation, supply shader identifiers
public abstract class AbstractIrisMdChunkRenderer<B extends AbstractIrisMdChunkRenderer.MdChunkRenderBatch> extends AbstractChunkRenderer implements IrisChunkRenderer {
    public static final int TRANSFORM_STRUCT_STRIDE = 4 * Float.BYTES;
    public static final int CAMERA_MATRICES_SIZE = 192;
    public static final int FOG_PARAMETERS_SIZE = 32;

    protected final ChunkRenderPassManager renderPassManager;
    protected final RenderPipeline<IrisChunkShaderInterface, BufferTarget>[] pipelines;
    protected final RenderPipeline<IrisChunkShaderInterface, BufferTarget>[] shadowPipelines;

    protected final StreamingBuffer uniformBufferCameraMatrices;
    protected final StreamingBuffer uniformBufferChunkTransforms;
    protected final StreamingBuffer uniformBufferFogParameters;
    protected final SequenceIndexBuffer indexBuffer;
	private final VertexArrayDescription<BufferTarget> vertexArray;

	protected Collection<B>[] renderLists;
	protected IrisChunkProgramOverrides overrides;
	private TerrainVertexType vertexType;

    public AbstractIrisMdChunkRenderer(
			IrisChunkProgramOverrides overrides,
            RenderDevice device,
            ChunkRenderPassManager renderPassManager,
            TerrainVertexType vertexType
    ) {
        super(device);

        this.renderPassManager = renderPassManager;
		this.overrides = overrides;

		this.vertexType = vertexType;
        //noinspection unchecked
		this.pipelines = new RenderPipeline[renderPassManager.getRenderPassCount()];
		this.shadowPipelines = new RenderPipeline[renderPassManager.getRenderPassCount()];

		// construct all pipelines for current render passes now
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
				new VertexAttributeBinding(IrisChunkShaderBindingPoints.MID_BLOCK,
					vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_BLOCK)),
			})
		));

		boolean hasShadowPass = overrides.getSodiumTerrainPipeline() != null && overrides.getSodiumTerrainPipeline().hasShadowPass();

		RenderPipelineDescription terrainDescription = RenderPipelineDescription.builder().setCullingMode(CullMode.DISABLE).build();
		RenderPipelineDescription translucentDescription = RenderPipelineDescription.builder().setCullingMode(CullMode.DISABLE).setBlendFunction(BlendFunc.separate(BlendFunc.SrcFactor.SRC_ALPHA, BlendFunc.DstFactor.ONE_MINUS_SRC_ALPHA, BlendFunc.SrcFactor.ONE, BlendFunc.DstFactor.ONE_MINUS_SRC_ALPHA)).build();

		for (ChunkRenderPass pass : renderPassManager.getAllRenderPasses()) {
			Program<IrisChunkShaderInterface> program = overrides.getProgramOverride(this instanceof MdiChunkRendererIris, getMaxBatchSize(), false, device, pass, vertexType);
			if (program == null) {
				throw new RuntimeException("failure");
			}
			RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline = this.device.createRenderPipeline(
				pass.getPipelineDescription(),
				program,
				vertexArray
			);

			this.pipelines[pass.getId()] = pipeline;

			if (hasShadowPass) {
				Program<IrisChunkShaderInterface> shadowProgram = overrides.getProgramOverride(this instanceof MdiChunkRendererIris, getMaxBatchSize(), true, device, pass, vertexType);
				if (shadowProgram == null) {
					throw new RuntimeException("failure");
				}
				RenderPipeline<IrisChunkShaderInterface, BufferTarget> shadowPipeline = this.device.createRenderPipeline(
					pass.isTranslucent() ? translucentDescription : terrainDescription,
					shadowProgram,
					vertexArray
				);

				this.shadowPipelines[pass.getId()] = shadowPipeline;
			}
		}

        // Set up buffers
        int maxInFlightFrames = SodiumClientMod.options().advanced.cpuRenderAheadLimit + 1;
        int uboAlignment = device.properties().values.uniformBufferOffsetAlignment;
        int totalPasses = renderPassManager.getRenderPassCount();

        this.uniformBufferCameraMatrices = new DualStreamingBuffer(
                device,
                uboAlignment,
                MathUtil.align(CAMERA_MATRICES_SIZE, uboAlignment) * totalPasses * 2,
                maxInFlightFrames,
                EnumSet.of(MappedBufferFlags.EXPLICIT_FLUSH)
        );
        this.uniformBufferChunkTransforms = new DualStreamingBuffer(
                device,
                uboAlignment,
                1048576, // start with 1 MiB and expand from there if needed
                maxInFlightFrames,
                EnumSet.of(MappedBufferFlags.EXPLICIT_FLUSH)
        );
        this.uniformBufferFogParameters = new DualStreamingBuffer(
                device,
                uboAlignment,
                MathUtil.align(FOG_PARAMETERS_SIZE, uboAlignment) * totalPasses * 2,
                maxInFlightFrames,
                EnumSet.of(MappedBufferFlags.EXPLICIT_FLUSH)
        );
        this.indexBuffer = new SequenceIndexBuffer(device, SequenceBuilder.QUADS_INT);
    }

    protected static ShaderConstants.Builder getBaseShaderConstants(ChunkRenderPass pass, TerrainVertexType vertexType) {
        var constants = ShaderConstants.builder();

        if (pass.isCutout()) {
            constants.add("ALPHA_CUTOFF", String.valueOf(pass.getAlphaCutoff()));
        }

        if (!Mth.equal(vertexType.getVertexRange(), 1.0f)) {
            constants.add("VERT_SCALE", String.valueOf(vertexType.getVertexRange()));
        }

        return constants;
    }

    protected ShaderConstants.Builder addAdditionalShaderConstants(ShaderConstants.Builder constants) {
        return constants; // NOOP, override if needed
    }

	@Override
	public void deletePipeline() {
		overrides.deleteShaders(device);
	}

	@Override
	public void createPipelines(IrisChunkProgramOverrides overrides) {
		boolean hasShadowPass = overrides.getSodiumTerrainPipeline() != null && overrides.getSodiumTerrainPipeline().hasShadowPass();
		RenderPipelineDescription terrainDescription = RenderPipelineDescription.builder().setCullingMode(CullMode.DISABLE).build();
		RenderPipelineDescription translucentDescription = RenderPipelineDescription.builder().setCullingMode(CullMode.DISABLE).setBlendFunction(BlendFunc.separate(BlendFunc.SrcFactor.SRC_ALPHA, BlendFunc.DstFactor.ONE_MINUS_SRC_ALPHA, BlendFunc.SrcFactor.ONE, BlendFunc.DstFactor.ONE_MINUS_SRC_ALPHA)).build();

		for (ChunkRenderPass pass : renderPassManager.getAllRenderPasses()) {
			RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline = this.device.createRenderPipeline(
				pass.getPipelineDescription(),
				overrides.getProgramOverride(this instanceof MdiChunkRendererIris, getMaxBatchSize(), false, device, pass, vertexType),
				vertexArray
			);

			this.pipelines[pass.getId()] = pipeline;

			if (hasShadowPass) {
				RenderPipeline<IrisChunkShaderInterface, BufferTarget> shadowPipeline = this.device.createRenderPipeline(
					pass.isTranslucent() ? translucentDescription : terrainDescription,
					overrides.getProgramOverride(this instanceof MdiChunkRendererIris, getMaxBatchSize(), true, device, pass, vertexType),
					vertexArray
				);

				this.shadowPipelines[pass.getId()] = shadowPipeline;
			}
		}
	}

	//// RENDER METHODS

    @Override
    public void render(ChunkRenderPass renderPass, ChunkRenderMatrices matrices, int frameIndex) {
        // make sure a render list was created for this pass, if any
        if (this.renderLists == null) {
            return;
        }

        int passId = renderPass.getId();
        if (passId < 0 || this.renderLists.length < passId) {
            return;
        }

        var renderList = this.renderLists[passId];
        if (renderList == null) {
            return;
        }

        // if the render list exists, the pipeline probably exists (unless a new render pass was added without a reload)
		RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline;
		if(ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			pipeline = this.shadowPipelines[passId];
		} else {
			pipeline = this.pipelines[passId];
		}

		RenderSystem.setShaderTexture(0, GlTexture.getHandle(TextureUtil.getBlockAtlasTexture()));
		pipeline.getProgram().getInterface().setup();
        this.device.useRenderPipeline(pipeline, (commandList, programInterface, pipelineState) -> {
            this.setupPerRenderList(renderPass, matrices, frameIndex, pipeline, commandList, programInterface, pipelineState);

            for (B batch : renderList) {
                this.setupPerBatch(renderPass, matrices, frameIndex, pipeline, commandList, programInterface, pipelineState, batch);

                this.issueDraw(renderPass, matrices, frameIndex, pipeline, commandList, programInterface, pipelineState, batch);
            }
        });
		pipeline.getProgram().getInterface().restore();
	}

    //// OVERRIDABLE RENDERING METHODS

    protected void setupPerRenderList(
            ChunkRenderPass renderPass,
            ChunkRenderMatrices matrices,
            int frameIndex,
            RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline,
            RenderCommandList<BufferTarget> commandList,
			IrisChunkShaderInterface programInterface,
            PipelineState pipelineState
    ) {
        this.setupTextures(renderPass, pipelineState);
        this.setupUniforms(matrices, programInterface, pipelineState, frameIndex);

        commandList.bindElementBuffer(this.indexBuffer.getBuffer());
    }

    protected void setupPerBatch(
            ChunkRenderPass renderPass,
            ChunkRenderMatrices matrices,
            int frameIndex,
			RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline,
            RenderCommandList<BufferTarget> commandList,
			IrisChunkShaderInterface programInterface,
            PipelineState pipelineState,
            B batch
    ) {
        commandList.bindVertexBuffer(
                BufferTarget.VERTICES,
                batch.getVertexBuffer(),
                0,
                batch.getVertexStride()
        );
    }

    protected abstract void issueDraw(
            ChunkRenderPass renderPass,
            ChunkRenderMatrices matrices,
            int frameIndex,
			RenderPipeline<IrisChunkShaderInterface, BufferTarget> pipeline,
            RenderCommandList<BufferTarget> commandList,
			IrisChunkShaderInterface programInterface,
            PipelineState pipelineState,
            B batch
    );

    protected void setupTextures(ChunkRenderPass pass, PipelineState pipelineState) {
        pipelineState.bindTexture(
                0,
                TextureUtil.getBlockAtlasTexture(),
                pass.isMipped() ? this.blockTextureMippedSampler : this.blockTextureSampler
        );
        pipelineState.bindTexture(2, TextureUtil.getLightTexture(), this.lightTextureSampler);
    }

    protected void setupUniforms(
            ChunkRenderMatrices renderMatrices,
            ChunkShaderInterface programInterface,
            PipelineState state,
            int frameIndex
    ) {
        StreamingBuffer.WritableSection matricesSection = this.uniformBufferCameraMatrices.getSection(frameIndex, CAMERA_MATRICES_SIZE, true);
        ByteBuffer matricesView = matricesSection.getView();
        long matricesPtr = MemoryUtil.memAddress(matricesView);

        renderMatrices.projection().getToAddress(matricesPtr);
        renderMatrices.modelView().getToAddress(matricesPtr + 64);

        Matrix4f mvpMatrix = new Matrix4f();
        mvpMatrix.set(renderMatrices.projection());
        mvpMatrix.mul(renderMatrices.modelView());
        mvpMatrix.getToAddress(matricesPtr + 128);
        matricesView.position(matricesView.position() + CAMERA_MATRICES_SIZE);

        matricesSection.flushPartial();

        state.bindBufferBlock(
                programInterface.uniformCameraMatrices,
                this.uniformBufferCameraMatrices.getBufferObject(),
                matricesSection.getDeviceOffset(),
                matricesSection.getView().capacity()
        );

        StreamingBuffer.WritableSection fogParamsSection = this.uniformBufferFogParameters.getSection(frameIndex, FOG_PARAMETERS_SIZE, true);
        ByteBuffer fogParamsView = fogParamsSection.getView();
        long fogParamsPtr = MemoryUtil.memAddress(fogParamsView);

        float[] paramFogColor = RenderSystem.getShaderFogColor();
        MemoryUtil.memPutFloat(fogParamsPtr + 0, paramFogColor[0]);
        MemoryUtil.memPutFloat(fogParamsPtr + 4, paramFogColor[1]);
        MemoryUtil.memPutFloat(fogParamsPtr + 8, paramFogColor[2]);
        MemoryUtil.memPutFloat(fogParamsPtr + 12, paramFogColor[3]);
        MemoryUtil.memPutFloat(fogParamsPtr + 16, RenderSystem.getShaderFogStart());
        MemoryUtil.memPutFloat(fogParamsPtr + 20, RenderSystem.getShaderFogEnd());
        MemoryUtil.memPutInt(  fogParamsPtr + 24, RenderSystem.getShaderFogShape().getIndex());
        fogParamsView.position(fogParamsView.position() + FOG_PARAMETERS_SIZE);

        fogParamsSection.flushPartial();

        state.bindBufferBlock(
                programInterface.uniformFogParameters,
                this.uniformBufferFogParameters.getBufferObject(),
                fogParamsSection.getDeviceOffset(),
                fogParamsSection.getView().capacity()
        );
    }

    //// UTILITY METHODS

    protected static int getMaxSectionFaces(SortedTerrainLists list) {
        int faces = 0;

		for (List<LongList> passModelPartSegments : list.modelPartSegments) {
			for (LongList regionModelPartSegments : passModelPartSegments) {
				faces += regionModelPartSegments.size();
			}
		}

        return faces;
    }

    protected static float getCameraTranslation(int chunkBlockPos, int cameraBlockPos, float cameraPos) {
        return (chunkBlockPos - cameraBlockPos) - cameraPos;
    }

    //// OVERRIDABLE BATCH

    protected static class MdChunkRenderBatch {
        protected final Buffer vertexBuffer;
        protected final int vertexStride;
        protected final int commandCount;
        protected final long transformBufferOffset;

        public MdChunkRenderBatch(
                Buffer vertexBuffer,
                int vertexStride,
                int commandCount,
                long transformBufferOffset
        ) {
            this.vertexBuffer = vertexBuffer;
            this.vertexStride = vertexStride;
            this.commandCount = commandCount;
            this.transformBufferOffset = transformBufferOffset;
        }

        public Buffer getVertexBuffer() {
            return this.vertexBuffer;
        }

        public int getVertexStride() {
            return this.vertexStride;
        }

        public int getCommandCount() {
            return this.commandCount;
        }

        public long getTransformsBufferOffset() {
            return this.transformBufferOffset;
        }
    }

    //// OVERRIDABLE BUFFER INFO

    @Override
    public int getDeviceBufferObjects() {
        return 4;
    }

    @Override
    public long getDeviceUsedMemory() {
        return this.uniformBufferCameraMatrices.getDeviceUsedMemory() +
               this.uniformBufferChunkTransforms.getDeviceUsedMemory() +
               this.uniformBufferFogParameters.getDeviceUsedMemory() +
               this.indexBuffer.getDeviceUsedMemory();
    }

    @Override
    public long getDeviceAllocatedMemory() {
        return this.uniformBufferCameraMatrices.getDeviceAllocatedMemory() +
               this.uniformBufferChunkTransforms.getDeviceAllocatedMemory() +
               this.uniformBufferFogParameters.getDeviceAllocatedMemory() +
               this.indexBuffer.getDeviceAllocatedMemory();
    }

    //// MISC METHODS

    @Override
    public void delete() {
        super.delete();
        this.uniformBufferCameraMatrices.delete();
        this.uniformBufferChunkTransforms.delete();
        this.uniformBufferFogParameters.delete();
        this.indexBuffer.delete();
    }
}
