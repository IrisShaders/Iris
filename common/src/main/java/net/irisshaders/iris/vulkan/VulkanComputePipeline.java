package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VulkanComputePipeline implements ComputePipeline {
	private final VulkanShaderCache cache;
	private final VkDevice device;
	private final CompletableFuture<Void> compilation;
	private final VulkanProgramResources bindings;
	private final String name;
	private final PatchedShader shader;
	private final BufferUniformHolder uniforms;
	private final ProgramResources resources;
	private final long[] maxGroups = new long[3];
	private final Vector3i localSize = new Vector3i(1);
	private VulkanResourceSets sets;
	private long pipeline;
	private long layout;
	private long emptyLayout;
	private boolean closed;

	public VulkanComputePipeline(VulkanShaderCache cache, String name, PatchedShader shader, ProgramResources resources,
		BufferUniformHolder uniforms) {
		this.cache = cache;
		this.device = VulkanCommandBridge.getDevice().vkDevice();
		var pack = (VulkanPackResources) resources.getPackSamplers().getResources();
		this.bindings = new VulkanProgramResources(resources);
		this.name = name;
		this.shader = shader;
		this.uniforms = uniforms;
		this.resources = resources;

		try (var stack = MemoryStack.stackPush()) {
			var props = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(this.device.getPhysicalDevice(), props);

			for (int i = 0; i < 3; i++) {
				this.maxGroups[i] = Integer.toUnsignedLong(props.limits().maxComputeWorkGroupCount(i));
			}
		}

		this.uniforms.resolveLayout(this.shader.layout());

		try {
			if (this.shader.layout().descriptors().stream().anyMatch(d -> d.set() == 0)) {
				this.sets = new VulkanResourceSets(this.device, this.shader.layout(), 32);
			}

			var heap = pack.getSamplerHeap();
			var frame = VulkanFrameResources.getOrCreate(this.uniforms.frameUniforms(), this.device);
			long frameLayout = frame == null ? 0 : frame.sets.setLayout();
			this.compilation = cache.submit(() -> {
				this.compile(heap, frameLayout);
				return null;
			});
		} catch (RuntimeException | Error failure) {
			this.destroyHandles();
			throw failure;
		}
	}

	private static void check(int result) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Vulkan compute error: " + result);
		}
	}

	@Override
	public Vector3i localSize() {
		this.prepare();

		return new Vector3i(this.localSize);
	}

	public void prepare() {
		if (this.closed) {
			throw new IllegalStateException("compute program is closed: " + this.name);
		}

		VulkanShaderCache.await(this.compilation);
	}

	private void compile(VulkanSamplerHeap heap, long frameLayout) {
		var vk = this.device;

		long module = 0;

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);

			if (this.sets == null) {
				check(VK10.vkCreateDescriptorSetLayout(vk, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default(), null, handle));
				this.emptyLayout = handle.get(0);
			}

			check(VK10.vkCreatePipelineLayout(vk, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
					.pSetLayouts(heap.pipelineSetLayouts(stack, this.sets == null ? this.emptyLayout : this.sets.setLayout(), frameLayout)),
				null, handle));
			this.layout = handle.get(0);

			var spirv = this.cache.compile(this.name + ".csh", PatchShaderType.COMPUTE, this.shader.sources().get(PatchShaderType.COMPUTE));

			try {
				var words = spirv.asIntBuffer();
				boolean foundSize = false;

				for (int i = 5; i < words.limit(); ) {
					int instruction = words.get(i);
					int count = instruction >>> 16;
					int op = instruction & 65535;

					if (count == 0) {
						throw new IllegalArgumentException("invalid compute SPIR-V");
					}

					if (op == 16 && count == 6 && words.get(i + 2) == 17) {
						this.localSize.set(words.get(i + 3), words.get(i + 4), words.get(i + 5));
						foundSize = true;
					}

					i += count;
				}

				if (!foundSize) {
					throw new UnsupportedOperationException("compute requires a literal local workgroup size: " + this.name);
				}

				check(VK10.vkCreateShaderModule(vk, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle));
				module = handle.get(0);
			} finally {
				MemoryUtil.memFree(spirv);
			}

			var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(this.layout);
			var name = Thread.currentThread().getName();
			Thread.currentThread().setName(this.name);
			info.stage().sType$Default().stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
			check(VK10.vkCreateComputePipelines(vk, 0, info, null, handle));
			this.pipeline = handle.get(0);
		} catch (RuntimeException | Error failure) {
			this.destroyHandles();

			throw failure;
		} finally {
			Thread.currentThread().setName(name);

			if (module != 0) {
				VK10.vkDestroyShaderModule(vk, module, null);
			}
		}
	}

	public void dispatch(Vector3i groups, FilledIndirectPointer indirect) {
		this.prepare();
		var frontend = RenderSystem.getDevice().createCommandEncoder();
		var encoder = VulkanCommandBridge.encoder(frontend);
		var resolved = this.resources.resolve();
		var allBindings = this.bindings.getBufferSlices(resolved);

		if (this.uniforms.data().hasRemaining()) {
			allBindings.put(ShaderResourceLayout.UNIFORM_BLOCK, List.of(VulkanCommandBridge.bufferSlice(
				this.uniforms.upload(frontend, RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment()))));
		}

		if (groups.x < 0 || groups.y < 0 || groups.z < 0 || groups.x > this.maxGroups[0] || groups.y > this.maxGroups[1] || groups.z > this.maxGroups[2]) {
			throw new IllegalArgumentException("invalid dispatch for " + this.name + ": " + groups);
		}

		var heap = this.bindings.createSnapshot(resolved);
		VulkanResourceSets.Snapshot snapshot = null;
		boolean recording = false;

		try (var stack = MemoryStack.stackPush()) {

			if (this.sets != null) {
				snapshot = this.sets.snapshot(allBindings, this.bindings.getImageViews(resolved));
			}

			var frameSnapshot = this.uniforms.frameUniforms() == null ? null : VulkanFrameResources.getOrCreate(
				this.uniforms.frameUniforms(), VulkanCommandBridge.getDevice().vkDevice()).getSnapshot();
			var command = VulkanCommandBridge.commandBuffer(encoder);
			var accesses = this.bindings.getResourceAccesses(resolved);

			if (indirect != null) {
				accesses.add(new VulkanSynchronization.Access(
					new VulkanSynchronization.Resource(false, ((VulkanGpuBuffer) indirect.buffer()).vkBuffer()),
					VK10.VK_PIPELINE_STAGE_DRAW_INDIRECT_BIT, VK10.VK_ACCESS_INDIRECT_COMMAND_READ_BIT, false));
			}

			VulkanSynchronization.before(command, stack, accesses);
			recording = true;
			VK10.vkCmdBindPipeline(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.pipeline);
			heap.bind(command, this.layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);

			if (frameSnapshot != null) {
				frameSnapshot.bind(command, this.layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);
			}

			if (snapshot != null) {
				snapshot.bind(command, this.layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);
			}

			if (indirect == null) {
				VK10.vkCmdDispatch(command, groups.x, groups.y, groups.z);
			} else {
				VK10.vkCmdDispatchIndirect(command, ((VulkanGpuBuffer) indirect.buffer()).vkBuffer(), indirect.offset());
			}

			VulkanSynchronization.after(command, stack);
		} finally {
			if (recording) {
				encoder.queueForDestroy(heap::close);

				if (snapshot != null) {
					encoder.queueForDestroy(snapshot::close);
				}
			} else {
				heap.close();

				if (snapshot != null) {
					snapshot.close();
				}
			}
		}
	}

	private void destroyHandles() {
		var vk = this.device;

		if (this.pipeline != 0) {
			VK10.vkDestroyPipeline(vk, this.pipeline, null);
		}

		if (this.layout != 0) {
			VK10.vkDestroyPipelineLayout(vk, this.layout, null);
		}

		if (this.emptyLayout != 0) {
			VK10.vkDestroyDescriptorSetLayout(vk, this.emptyLayout, null);
		}

		if (this.sets != null) {
			this.sets.close();
		}

		this.pipeline = this.layout = this.emptyLayout = 0;
		this.sets = null;
	}

	@Override
	public void close() {
		if (!this.closed) {
			this.closed = true;
			this.compilation.handle((result, failure) -> null).join();
			this.destroyHandles();
		}
	}
}
