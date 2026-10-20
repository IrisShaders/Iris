package net.irisshaders.iris.vulkan;

import net.irisshaders.iris.Iris;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import net.irisshaders.iris.backend.CompileTask;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.VulkanPushConstants;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.util.concurrent.CompletableFuture;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;
import static org.lwjgl.vulkan.KHRSynchronization2.*;

public class VulkanComputePipeline implements ComputePipeline {
	private final VulkanShaderCache cache;
	private final VkDevice device;
	private final CompletableFuture<Void> compilation;
	private final VulkanShaderBindings bindings;
	private final String name;
	private final PatchedShader shader;
	private final DrawUniforms uniforms;
	private final long[] maxGroups = new long[3];
	private final Vector3i localSize = new Vector3i(1);
	private long pipeline;
	private long layout;
	private boolean closed;

	public VulkanComputePipeline(VulkanShaderCache cache, String name, PatchedShader shader, ProgramSamplers resources,
		DrawUniforms uniforms) {
		this.cache = cache;
		this.device = VulkanCommandBridge.getDevice().vkDevice();
		var pack = (VulkanPackResources) resources.getPackSamplers().getResources();
		this.bindings = new VulkanShaderBindings(resources, shader);
		this.name = name;
		this.shader = shader;
		this.uniforms = uniforms;

		try (var stack = MemoryStack.stackPush()) {
			var props = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(this.device.getPhysicalDevice(), props);

			for (int i = 0; i < 3; i++) {
				this.maxGroups[i] = Integer.toUnsignedLong(props.limits().maxComputeWorkGroupCount(i));
			}
		}

		this.uniforms.resolveLayout(this.shader.layout());

		try {
			var heap = pack.getSamplerHeap();
			this.compilation = cache.submit(() -> {
				this.compile(heap);
				return null;
			});
		} catch (RuntimeException | Error failure) {
			this.destroyHandles();
			throw failure;
		}
	}

	@Override
	public Vector3i localSize() {
		this.prepare();

		return new Vector3i(this.localSize);
	}

	public void prepare() {
		CompileTask.await(this.compilation);
	}

	private void compile(VulkanSamplerHeap heap) {
		var vk = this.device;

		long module = 0;

		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);

			check(VK10.vkCreatePipelineLayout(vk, VkPipelineLayoutCreateInfo.calloc(stack).sType$Default()
					.pSetLayouts(heap.pipelineSetLayouts(stack, this.bindings.drawLayout()))
					.pPushConstantRanges(VkPushConstantRange.calloc(1, stack).stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT).size(VulkanPushConstants.MAX_SIZE)),
				null, handle), "create compute pipeline layout");
			this.layout = handle.get(0);

			var spirv = this.cache.compile(this.name + ".csh", PatchShaderType.COMPUTE, this.shader.sources().get(PatchShaderType.COMPUTE));

			try {
				var words = spirv.asIntBuffer();
				boolean foundSize = false;

				for (int i = 5; i < words.limit(); ) {
					int instruction = words.get(i);
					int count = instruction >>> 16;
					int op = instruction & 65535;

					if (op == 16 && count == 6 && words.get(i + 2) == 17) {
						this.localSize.set(words.get(i + 3), words.get(i + 4), words.get(i + 5));
						foundSize = true;
					}

					i += count;
				}

				if (!foundSize) {
					throw new UnsupportedOperationException("compute requires a literal local workgroup size: " + this.name);
				}

				check(VK10.vkCreateShaderModule(vk, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle),
					"create compute shader module");
				module = handle.get(0);
			} finally {
				MemoryUtil.memFree(spirv);
			}

			var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(this.layout);
			var name = Thread.currentThread().getName();
			Thread.currentThread().setName(this.name);
			info.stage().sType$Default().stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
			check(VK10.vkCreateComputePipelines(vk, VulkanCommandBridge.pipelineCache(), info, null, handle), "create compute pipeline");
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

	@Override
	public void dispatch(Vector3i groups, FilledIndirectPointer indirect) {
		this.prepare();

		if (groups.x < 0 || groups.y < 0 || groups.z < 0 || groups.x > this.maxGroups[0] || groups.y > this.maxGroups[1] || groups.z > this.maxGroups[2]) {
			throw new IllegalArgumentException("invalid dispatch for " + this.name + ": " + groups);
		}

		((VulkanRenderBackend) Iris.getRenderBackend()).textures.flush();
		try (var stack = MemoryStack.stackPush()) {
			var command = VulkanCommandBridge.commandBuffer();
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT_KHR
					| (indirect == null ? 0 : VK_PIPELINE_STAGE_2_DRAW_INDIRECT_BIT_KHR),
				VK_ACCESS_2_SHADER_READ_BIT_KHR | VK_ACCESS_2_UNIFORM_READ_BIT_KHR
					| (indirect == null ? 0 : VK_ACCESS_2_INDIRECT_COMMAND_READ_BIT_KHR), VK_ACCESS_2_SHADER_WRITE_BIT_KHR);

			VK10.vkCmdBindPipeline(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.pipeline);
			this.bindings.bind(command, this.layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.uniforms);
			var drawConstants = this.uniforms.pushConstants();
			if (drawConstants.hasRemaining()) {
				VK10.vkCmdPushConstants(command, this.layout, VK10.VK_SHADER_STAGE_COMPUTE_BIT, 0, drawConstants);
			}

			if (indirect == null) {
				VK10.vkCmdDispatch(command, groups.x, groups.y, groups.z);
			} else {
				VK10.vkCmdDispatchIndirect(command, ((VulkanGpuBuffer) indirect.buffer()).vkBuffer(), indirect.offset());
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


		this.pipeline = this.layout = 0;
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
