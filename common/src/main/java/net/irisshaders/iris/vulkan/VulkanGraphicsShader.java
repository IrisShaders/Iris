package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class VulkanGraphicsShader implements GraphicsShader {
	private final PatchedShader shader;
	private final ProgramResources resources;
	private final BufferUniformHolder uniforms;
	private final VulkanProgramResources bindings;
	private final VulkanPackResources pack;
	private final VulkanResourceSets sets;
	private final VulkanFrameResources frame;
	private final VulkanShaderCache cache;
	private final List<CompletableFuture<VulkanGraphicsPipeline>> pipelines = new ArrayList<>();
	private CompletableFuture<Map<PatchShaderType, ByteBuffer>> spirv;
	private CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary> library;
	private boolean closed;

	public VulkanGraphicsShader(VulkanShaderCache cache, String name, PatchedShader shader, ProgramResources resources,
		BufferUniformHolder uniforms) {
		if (shader.sources().containsKey(PatchShaderType.GEOMETRY) && !VulkanShaderFeatures.isSupported(VulkanShaderFeatures.GEOMETRY)) {
			throw new IllegalArgumentException("geometry shaders are not supported: " + name);
		}

		if ((shader.sources().containsKey(PatchShaderType.TESS_CONTROL) || shader.sources()
			.containsKey(PatchShaderType.TESS_EVAL)) && !VulkanShaderFeatures.isSupported(VulkanShaderFeatures.TESSELLATION)) {
			throw new IllegalArgumentException("tessellation shaders are not supported: " + name);
		}

		this.cache = cache;
		this.resources = resources;
		this.uniforms = uniforms;
		this.bindings = new VulkanProgramResources(resources);
		this.pack = (VulkanPackResources) resources.getPackSamplers().getResources();
		this.shader = shader;
		var device = VulkanCommandBridge.getDevice().vkDevice();
		this.sets = shader.layout().descriptors().stream().anyMatch(d -> d.set() == 0) ? new VulkanResourceSets(device, shader.layout(),
			4096) : null;
		this.frame = VulkanFrameResources.getOrCreate(uniforms.frameUniforms(), device);

		try {
			if (VulkanPipelineLibraries.isSupported(VulkanCommandBridge.getDevice())) {
				this.library = cache.acquireLibrary(name, shader.sources(), this.pack.getSamplerHeap(), this.sets,
					this.frame == null ? null : this.frame.sets);
			} else {
				this.spirv = cache.submit(() -> {
					var spirv = new EnumMap<PatchShaderType, ByteBuffer>(PatchShaderType.class);

					try {
						for (var entry : shader.sources().entrySet()) {
							spirv.put(entry.getKey(), cache.compile(name + entry.getKey().extension, entry.getKey(), entry.getValue()));
						}

						return spirv;
					} catch (RuntimeException | Error failure) {
						spirv.values().forEach(MemoryUtil::memFree);
						throw failure;
					}
				});
			}
		} catch (RuntimeException | Error e) {
			this.close();
			throw e;
		}
	}

	@Override
	public PatchedShader getSource() {
		return this.shader;
	}

	@Override
	public GraphicsPipeline createPipeline(PipelineState state) {
		if (this.closed) {
			throw new IllegalStateException("graphics shader is closed");
		}

		return new Pipeline(this.compilePipeline(state, false), state);
	}

	private CompletableFuture<VulkanGraphicsPipeline> compilePipeline(PipelineState state, boolean dynamic) {
		int[] formats = state.colorFormats().stream().mapToInt(VulkanConst::toVk).toArray();
		var attributes = state.vertexAttributes().stream()
			.map(a -> new VulkanGraphicsPipeline.Attribute(a.location(), a.binding(), VulkanConst.toVk(a.format()), a.offset())).toList();
		var world = state.vanillaPipeline() == null ? null : new VulkanGraphicsPipeline.WorldState(state.vanillaPipeline(),
			state.depthFormat() == null ? VK10.VK_FORMAT_UNDEFINED : VulkanConst.toVk(state.depthFormat()), state.shadowPass(),
			state.zeroVertexBuffer() == null ? 0 : ((VulkanGpuBuffer) state.zeroVertexBuffer()).vkBuffer(), state.vertexFormats());
		var device = VulkanCommandBridge.getDevice().vkDevice();
		var heap = this.pack.getSamplerHeap();
		var pipeline = this.library == null ? this.cache.thenApply(this.spirv,
			spirv -> new VulkanGraphicsPipeline(device, heap, this.sets, spirv, formats, state.vertexStride(), attributes,
				state.blendModes(), this.shader.writtenAttachments(), world, this.frame == null ? null : this.frame.sets,
				dynamic)) : this.cache.thenApply(this.library,
			library -> new VulkanGraphicsPipeline(device, heap, this.sets, library, formats, state.vertexStride(), attributes,
				state.blendModes(), this.shader.writtenAttachments(), world, this.frame == null ? null : this.frame.sets, dynamic));
		this.pipelines.add(pipeline);

		return pipeline;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;

		this.pipelines.forEach(future -> future.handle((pipeline, failure) -> null).join());
		this.pipelines.clear();

		if (this.library != null) {
			this.library.handle((library, failure) -> {
				if (library != null) {
					this.cache.releaseLibrary(library);
				}
				return null;
			}).join();
		}

		if (this.spirv != null) {
			this.spirv.handle((spirv, failure) -> {
				if (spirv != null) {
					spirv.values().forEach(MemoryUtil::memFree);
				}
				return null;
			}).join();
		}

		if (this.sets != null) {
			this.sets.close();
		}
	}

	public class Pipeline implements GraphicsPipeline {
		private final CompletableFuture<VulkanGraphicsPipeline> compilation;
		private final FramebufferDescription framebuffer;
		private final PipelineState state;
		private CompletableFuture<VulkanGraphicsPipeline> vanillaCompilation;
		private VulkanGraphicsPipeline vanillaPipeline;
		private VulkanGraphicsPipeline pipeline;
		private boolean closed;

		private Pipeline(CompletableFuture<VulkanGraphicsPipeline> compilation, PipelineState state) {
			this.compilation = compilation;
			this.framebuffer = state.framebuffer();
			this.state = state;
		}

		private VulkanGraphicsPipeline getPipeline() {
			if (this.closed) {
				throw new IllegalStateException("graphics pipeline is closed");
			}

			if (this.pipeline == null) {
				this.pipeline = VulkanShaderCache.await(this.compilation);
			}

			return this.pipeline;
		}

		public void bind(VulkanCommandEncoder encoder, VkCommandBuffer command, boolean vanilla) {
			var pipeline = this.getPipeline();
			if (vanilla) {
				if (this.vanillaPipeline == null) {
					if (this.vanillaCompilation == null) {
						this.vanillaCompilation = VulkanGraphicsShader.this.compilePipeline(this.state, true);
					}
					this.vanillaPipeline = VulkanShaderCache.await(this.vanillaCompilation);
				}
				pipeline = this.vanillaPipeline;
			}
			var resolved = VulkanGraphicsShader.this.resources.resolve();
			var complete = resolved.buffers();

			if (!complete.containsKey(ShaderResourceLayout.UNIFORM_BLOCK) && VulkanGraphicsShader.this.uniforms.data().hasRemaining()) {
				complete.put(ShaderResourceLayout.UNIFORM_BLOCK, List.of(encoder.transientMemory()
					.uploadGpu(VulkanGraphicsShader.this.uniforms.data(),
						RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment(), GpuBuffer.USAGE_UNIFORM)));
			}

			var heap = VulkanGraphicsShader.this.bindings.createSnapshot(resolved);
			VulkanResourceSets.Snapshot snapshot = null;

			try {
				var bindings = VulkanGraphicsShader.this.bindings;
				var nativeBuffers = bindings.getBufferSlices(resolved);
				snapshot = VulkanGraphicsShader.this.sets == null ? null : VulkanGraphicsShader.this.sets.snapshot(nativeBuffers,
					bindings.getImageViews(resolved));

				if (VulkanGraphicsShader.this.frame != null) {
					pipeline.bindFrame(command, VulkanGraphicsShader.this.frame.getSnapshot());
				}

				pipeline.bind(command, heap, snapshot);
			} finally {
				encoder.queueForDestroy(heap::close);

				if (snapshot != null) {
					encoder.queueForDestroy(snapshot::close);
				}
			}
		}

		@Override
		public FramebufferDescription framebuffer() {
			return this.framebuffer;
		}

		public List<VulkanSynchronization.Access> accesses() {
			return VulkanGraphicsShader.this.bindings.getResourceAccesses(VulkanGraphicsShader.this.resources.resolve());
		}

		public void pushConstants(VkCommandBuffer command, ByteBuffer data) {
			this.getPipeline().pushConstants(command, data);
		}

		@Override
		public void close() {
			if (!this.closed) {
				this.closed = true;
				if (this.vanillaCompilation != null) {
					this.vanillaCompilation.handle((pipeline, failure) -> {
						if (pipeline != null) {
							pipeline.close();
						}
						return null;
					}).join();
				}
				this.compilation.handle((pipeline, failure) -> {
					if (pipeline != null) {
						pipeline.close();
					}
					return null;
				}).join();
			}
		}
	}
}
