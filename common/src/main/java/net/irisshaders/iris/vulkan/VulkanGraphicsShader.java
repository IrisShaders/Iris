package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.CompileTask;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
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
	private final DrawUniforms uniforms;
	private final VulkanShaderBindings bindings;
	private final VulkanPackResources pack;
	private final VulkanShaderCache cache;
	private final List<CompletableFuture<VulkanGraphicsPipeline>> pipelines = new ArrayList<>();
	private final List<CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary>> fragmentCompilations = new ArrayList<>();
	private CompletableFuture<Map<PatchShaderType, ByteBuffer>> spirv;
	private CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary> library;
	private boolean closed;

	public VulkanGraphicsShader(VulkanShaderCache cache, String name, PatchedShader shader, ProgramSamplers resources,
		DrawUniforms uniforms) {
		if (shader.sources().containsKey(PatchShaderType.GEOMETRY) && !VulkanShaderFeatures.isSupported(VulkanShaderFeatures.GEOMETRY)) {
			throw new IllegalArgumentException("geometry shaders are not supported by this GPU??? " + name);
		}

		if ((shader.sources().containsKey(PatchShaderType.TESS_CONTROL) || shader.sources()
			.containsKey(PatchShaderType.TESS_EVAL)) && !VulkanShaderFeatures.isSupported(VulkanShaderFeatures.TESSELLATION)) {
			throw new IllegalArgumentException("tessellation shaders are not supported by this GPU??? " + name);
		}

		this.cache = cache;
		this.uniforms = uniforms;
		this.bindings = new VulkanShaderBindings(resources, shader);
		this.pack = (VulkanPackResources) resources.getPackSamplers().getResources();
		this.shader = shader;

		try {
			if (VulkanPipelineLibraries.isSupported(VulkanCommandBridge.getDevice())) {
				this.library = cache.acquireLibrary(name, shader.sources(), this.pack.getSamplerHeap(), this.bindings.drawLayout());
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
		return this.createPipeline(state, false);
	}

	@Override
	public GraphicsPipeline createPipeline(PipelineState state, boolean background) {
		return new Pipeline(this.compilePipeline(state, false, background), state);
	}

	private CompletableFuture<VulkanGraphicsPipeline> compilePipeline(PipelineState state, boolean minecraftPass, boolean background) {
		int[] formats = new int[state.colorAttachmentCount()];
		for (int i = 0; i < formats.length; i++) {
			formats[i] = VulkanConst.toVk(state.colorFormats()[i]);
		}
		var attributes = state.vertexAttributes().stream()
			.map(a -> new VulkanGraphicsPipeline.Attribute(a.location(), a.binding(), VulkanConst.toVk(a.format()), a.offset())).toList();
		var world = state.vanillaPipeline() == null ? null : new VulkanGraphicsPipeline.WorldState(state.vanillaPipeline(),
			state.depthFormat() == null ? VK10.VK_FORMAT_UNDEFINED : VulkanConst.toVk(state.depthFormat()), state.shadowPass(),
			state.zeroVertexBuffer() == null ? 0 : ((VulkanGpuBuffer) state.zeroVertexBuffer()).vkBuffer(), state.vertexFormats()); // stupid validation layer warning...
		var device = VulkanCommandBridge.getDevice().vkDevice();
		var heap = this.pack.getSamplerHeap();
		var library = this.library;
		if (library != null && !minecraftPass) {
			// Prepare both render-pass variants before the link task can be run by the first bind.
			library = this.cache.thenApply(library, compiled -> {
				compiled.prepareFragment(formats, world, false);
				compiled.prepareFragment(formats, world, true);
				return compiled;
			});
			this.fragmentCompilations.add(library);
		}
		var pipeline = library == null ? this.cache.compilePipeline(this.spirv,
			spirv -> new VulkanGraphicsPipeline(device, heap, spirv, formats, state.vertexStride(), attributes,
				state.blendModes(), this.shader.writtenAttachments(), world, this.bindings.drawLayout(), minecraftPass), background) : this.cache.compilePipeline(library,
			compiled -> new VulkanGraphicsPipeline(device, heap, compiled, formats, state.vertexStride(), attributes,
				state.blendModes(), this.shader.writtenAttachments(), world, this.bindings.drawLayout(), minecraftPass), background);
		this.pipelines.add(pipeline);
		if (background) {
			pipeline.whenComplete((result, failure) -> {
				if (failure != null && !pipeline.isCancelled()) {
					Iris.logger.warn("Failed to precompile render pipeline {}", state.vanillaPipeline().getLocation(), failure);
				}
			});
		}

		return pipeline;
	}

	@Override
	public void cancelCompilation() {
		this.pipelines.forEach(future -> future.cancel(false));
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.cancelCompilation();

		this.pipelines.forEach(future -> future.handle((pipeline, failure) -> null).join());
		this.pipelines.clear();
		this.fragmentCompilations.forEach(future -> future.handle((library, failure) -> null).join());
		this.fragmentCompilations.clear();

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

	}

	public class Pipeline implements GraphicsPipeline {
		private final CompletableFuture<VulkanGraphicsPipeline> compilation;
		private final FramebufferDescription framebuffer;
		private final PipelineState state;
		private CompletableFuture<VulkanGraphicsPipeline> minecraftCompilation;
		private VulkanGraphicsPipeline minecraftPipeline;
		private VulkanGraphicsPipeline pipeline;
		private boolean closed;

		private Pipeline(CompletableFuture<VulkanGraphicsPipeline> compilation, PipelineState state) {
			this.compilation = compilation;
			this.framebuffer = state.framebuffer();
			this.state = state;
		}

		private VulkanGraphicsPipeline getPipeline() {
			if (this.pipeline == null) {
				this.pipeline = CompileTask.await(this.compilation);
			}

			return this.pipeline;
		}

		public void bind(VkCommandBuffer command, boolean minecraftPass, VulkanBindingState state) {
			var pipeline = this.getPipeline();
			// Minecraft-owned passes keep the compatibility variant required by its Vulkan backend.
			if (minecraftPass) {
				if (this.minecraftPipeline == null) {
					if (this.minecraftCompilation == null) {
						this.minecraftCompilation = VulkanGraphicsShader.this.compilePipeline(this.state, true, false);
					}
					this.minecraftPipeline = CompileTask.await(this.minecraftCompilation);
				}
				pipeline = this.minecraftPipeline;
			}
			VulkanGraphicsShader.this.bindings.bind(command, pipeline.layout(), VK10.VK_PIPELINE_BIND_POINT_GRAPHICS,
				VulkanGraphicsShader.this.uniforms);
			pipeline.bind(command, state);
			pipeline.pushUniforms(command, VulkanGraphicsShader.this.uniforms.pushConstants());
		}

		@Override
		public FramebufferDescription framebuffer() {
			return this.framebuffer;
		}


		public void pushConstants(VkCommandBuffer command, ByteBuffer data) {
			this.getPipeline().pushConstants(command, data);
		}

		@Override
		public void close() {
			if (!this.closed) {
				this.closed = true;
				if (this.minecraftCompilation != null) {
					this.minecraftCompilation.handle((pipeline, failure) -> {
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
