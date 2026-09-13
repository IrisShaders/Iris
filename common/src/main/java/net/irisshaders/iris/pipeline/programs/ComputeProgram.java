package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import net.irisshaders.iris.vulkan.CompositeSynchronization;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanResourceSets;
import net.irisshaders.iris.vulkan.VulkanShaderCompiler;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ComputeProgram implements AutoCloseable {
	private FilledIndirectPointer indirect;
	private Vector3i absoluteWorkGroups;
	private Vector2f relativeWorkGroups;
	private float cachedWidth;
	private float cachedHeight;
	private Vector3i cachedWorkGroups;

	private final GpuDevice device;
	private final PackSamplers pack;
	private final String name;
	private final String source;
	private final BufferUniformHolder uniforms;
	private final ProgramResources samplers;
	private final long[] maxGroups = new long[3];
	private final Vector3i localSize = new Vector3i(1);
	private VulkanResourceSets sets;
	private long pipeline;
	private long layout;
	private long emptyLayout;
	private boolean closed;

	public ComputeProgram(GpuDevice device, PackSamplers pack, String name, String source) {
		this.device = device;
		this.pack = pack;
		this.name = name;
		this.source = source;

		try (var stack = MemoryStack.stackPush()) {
			var props = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(pack.backend().vkDevice().getPhysicalDevice(), props);

			for (int i = 0; i < 3; i++) {
				maxGroups[i] = Integer.toUnsignedLong(props.limits().maxComputeWorkGroupCount(i));
			}
		}

		var resources = VulkanShaderPatcher.discover(Map.of(PatchShaderType.COMPUTE, source)).layout();

		for (var resource : resources.descriptors()) {
			if (resource.kind() != VulkanResourceLayout.Kind.STORAGE_BUFFER && resource.kind() != VulkanResourceLayout.Kind.SAMPLER &&
				resource.kind() != VulkanResourceLayout.Kind.IMAGE && !resource.name().equals(VulkanResourceLayout.UNIFORM_BLOCK)) {
				throw new UnsupportedOperationException("Unsupported compute resource: " + name + ": " + resource.name());
			}
		}

		uniforms = new BufferUniformHolder(resources);
		samplers = new ProgramResources(pack, resources);
	}

	public void setWorkGroupInfo(Vector2f relativeWorkGroups, Vector3i absoluteWorkGroups, FilledIndirectPointer indirectPointer) {
		this.relativeWorkGroups = relativeWorkGroups;
		this.absoluteWorkGroups = absoluteWorkGroups;
		this.indirect = indirectPointer;
		cachedWorkGroups = null;
	}

	public Vector3i getWorkGroups(float width, float height) {
		var localSize = localSize();

		if (cachedWidth != width || cachedHeight != height || cachedWorkGroups == null) {
			this.cachedWidth = width;
			this.cachedHeight = height;

			if (this.absoluteWorkGroups != null) {
				this.cachedWorkGroups = this.absoluteWorkGroups;
			} else if (relativeWorkGroups != null) {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(Math.ceil((width * relativeWorkGroups.x)) / localSize.x),
					(int) Math.ceil(Math.ceil((height * relativeWorkGroups.y)) / localSize.y), 1);
			} else {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(width / localSize.x), (int) Math.ceil(height / localSize.y), 1);
			}
		}

		return cachedWorkGroups;
	}

	public void dispatch(float width, float height) {
		var level = Minecraft.getInstance().level;
		dispatch(indirect == null ? getWorkGroups(width, height) : new Vector3i(), level == null ? 0 : level.getGameTime(),
			SystemTimeUniforms.COUNTER.getAsInt());
	}

	public void destroy() {
		close();
	}

	public BufferUniformHolder uniforms() {
		return uniforms;
	}

	public ProgramResources samplers() {
		return samplers;
	}

	public Vector3i localSize() {
		prepare();

		return new Vector3i(localSize);
	}

	public void prepare() {
		if (closed) {
			throw new IllegalStateException("Compute program is closed: " + name);
		}

		if (pipeline != 0) {
			return;
		}

		var vk = pack.backend().vkDevice();
		var shader = VulkanShaderPatcher.patch(Map.of(PatchShaderType.COMPUTE, source), samplers.plan(), null, uniforms::isRegistered,
			uniforms.frameLayout(), uniforms::isDynamic);
		uniforms.resolveLayout(shader.layout());

		long module = 0;

		try (var stack = MemoryStack.stackPush(); var compiler = new VulkanShaderCompiler()) {
			if (shader.layout().descriptors().stream().anyMatch(d -> d.set() == 0)) {
				sets = new VulkanResourceSets(vk, shader.layout(), 32);
			}

			var handle = stack.mallocLong(1);

			if (sets == null) {
				check(VK10.vkCreateDescriptorSetLayout(vk, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default(), null, handle));
				emptyLayout = handle.get(0);
			}

			check(VK10.vkCreatePipelineLayout(vk,
				VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(
					uniforms.frameUniforms() == null
						? stack.longs(sets == null ? emptyLayout : sets.setLayout(), pack.heap().setLayout())
						: stack.longs(sets == null ? emptyLayout : sets.setLayout(), pack.heap().setLayout(),
						uniforms.frameUniforms().sets().setLayout())),
				null, handle));
			layout = handle.get(0);

			var spirv = compiler.compile(name + ".csh", PatchShaderType.COMPUTE, shader.sources().get(PatchShaderType.COMPUTE));

			try {
				var words = spirv.asIntBuffer();
				boolean foundSize = false;

				for (int i = 5; i < words.limit(); ) {
					int instruction = words.get(i);
					int count = instruction >>> 16;
					int op = instruction & 65535;

					if (count == 0) {
						throw new IllegalArgumentException("Invalid compute SPIR-V");
					}

					if (op == 16 && count == 6 && words.get(i + 2) == 17) {
						localSize.set(words.get(i + 3), words.get(i + 4), words.get(i + 5));
						foundSize = true;
					}

					i += count;
				}

				if (!foundSize) {
					throw new UnsupportedOperationException("Compute requires a literal local workgroup size: " + name);
				}

				check(VK10.vkCreateShaderModule(vk, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle));
				module = handle.get(0);
			} finally {
				MemoryUtil.memFree(spirv);
			}

			var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(layout);
			info.stage().sType$Default().stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
			check(VK10.vkCreateComputePipelines(vk, 0, info, null, handle));
			pipeline = handle.get(0);
		} catch (RuntimeException | Error failure) {
			destroyHandles();

			throw failure;
		} finally {
			if (module != 0) {
				VK10.vkDestroyShaderModule(vk, module, null);
			}
		}
	}

	public void dispatch(Vector3i groups, long tick, long frame) {
		prepare();
		uniforms.update(tick, frame);

		var frontend = device.createCommandEncoder();
		var bindings = new LinkedHashMap<String, List<VulkanResourceSets.BufferSlice>>();

		if (uniforms.data().remaining() > 0) {
			bindings.put(VulkanResourceLayout.UNIFORM_BLOCK, List.of(VulkanCommandBridge.bufferSlice(uniforms.upload(
				frontend, device.getDeviceInfo().limits().minUniformOffsetAlignment()))));
		}

		dispatch(VulkanCommandBridge.encoder(frontend), groups, bindings);
	}

	public void dispatch(VulkanCommandEncoder encoder, Vector3i groups, Map<String, List<VulkanResourceSets.BufferSlice>> bindings) {
		prepare();

		var allBindings = new LinkedHashMap<>(bindings);
		samplers.bufferBindings().forEach(
			(block, slices) -> allBindings.put(block, slices.stream().map(VulkanCommandBridge::bufferSlice).toList()));

		if (groups.x < 0 || groups.y < 0 || groups.z < 0 || groups.x > maxGroups[0] || groups.y > maxGroups[1] || groups.z > maxGroups[2]) {
			throw new IllegalArgumentException("Invalid dispatch for " + name + ": " + groups);
		}

		var heap = samplers.snapshot();
		VulkanResourceSets.Snapshot resources = null;
		boolean queued = false;

		try (var stack = MemoryStack.stackPush()) {
			samplers.addIndexBinding(encoder, allBindings);

			if (sets != null) {
				resources = sets.snapshot(allBindings, samplers.imageViews());
			}

			var frameSnapshot = uniforms.frameUniforms() == null ? null : uniforms.frameUniforms().snapshot();
			var command = encoder.allocateAndBeginTransientCommandBuffer();
			var accesses = CompositeSynchronization.active() ? samplers.accesses() : new ArrayList<CompositeSynchronization.Access>();

			if (indirect != null) {
				accesses.add(new CompositeSynchronization.Access(new CompositeSynchronization.Resource(false,
					indirect.buffer().vkBuffer()),
					VK10.VK_PIPELINE_STAGE_DRAW_INDIRECT_BIT, VK10.VK_ACCESS_INDIRECT_COMMAND_READ_BIT,
					false));
			}

			CompositeSynchronization.before(command, stack, accesses);
			VK10.vkCmdBindPipeline(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);
			heap.bind(command, layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);

			if (frameSnapshot != null) {
				frameSnapshot.bind(command, layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);
			}

			if (resources != null) {
				resources.bind(command, layout, VK10.VK_PIPELINE_BIND_POINT_COMPUTE);
			}

			if (indirect == null) {
				VK10.vkCmdDispatch(command, groups.x, groups.y, groups.z);
			} else {
				VK10.vkCmdDispatchIndirect(command, indirect.buffer().vkBuffer(), indirect.offset());
			}

			CompositeSynchronization.after(command, stack);
			check(VK10.vkEndCommandBuffer(command));
			encoder.execute(command);
			queued = true;
		} finally {
			if (queued) {
				encoder.queueForDestroy(heap::close);

				if (resources != null) {
					encoder.queueForDestroy(resources::close);
				}
			} else {
				heap.close();

				if (resources != null) {
					resources.close();
				}
			}
		}
	}

	private void destroyHandles() {
		var vk = pack.backend().vkDevice();

		if (pipeline != 0) {
			VK10.vkDestroyPipeline(vk, pipeline, null);
		}

		if (layout != 0) {
			VK10.vkDestroyPipelineLayout(vk, layout, null);
		}

		if (emptyLayout != 0) {
			VK10.vkDestroyDescriptorSetLayout(vk, emptyLayout, null);
		}

		if (sets != null) {
			sets.close();
		}

		pipeline = layout = emptyLayout = 0;
		sets = null;
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;
		pack.backend().createCommandEncoder().queueForDestroy(this::destroyHandles);
	}

	private static void check(int result) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Vulkan compute error: " + result);
		}
	}
}
