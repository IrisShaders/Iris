package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.samplers.ProgramResources;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VK10;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VulkanProgramResources {
	private final ProgramResources resources;
	private final VulkanPackResources pack;

	public VulkanProgramResources(ProgramResources resources) {
		this.resources = resources;
		this.pack = (VulkanPackResources) resources.getPackSamplers().getResources();
	}

	private static long stages(Set<PatchShaderType> stages) {
		long mask = 0;

		for (var stage : stages) {
			mask |= switch (stage) {
				case VERTEX -> VK10.VK_PIPELINE_STAGE_VERTEX_SHADER_BIT;
				case TESS_CONTROL -> VK10.VK_PIPELINE_STAGE_TESSELLATION_CONTROL_SHADER_BIT;
				case TESS_EVAL -> VK10.VK_PIPELINE_STAGE_TESSELLATION_EVALUATION_SHADER_BIT;
				case GEOMETRY -> VK10.VK_PIPELINE_STAGE_GEOMETRY_SHADER_BIT;
				case FRAGMENT -> VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
				case COMPUTE -> VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT;
			};
		}

		return mask;
	}

	public VulkanSamplerHeap.Snapshot createSnapshot(ProgramResources.Bindings bindings) {
		return this.pack.createSnapshot(bindings.textures(), bindings.texels());
	}

	public Map<String, List<VulkanResourceSets.BufferSlice>> getBufferSlices(ProgramResources.Bindings bindings) {
		var buffers = new LinkedHashMap<String, List<VulkanResourceSets.BufferSlice>>();
		bindings.buffers().forEach((name, slices) -> buffers.put(name, slices.stream().map(VulkanCommandBridge::bufferSlice).toList()));
		return buffers;
	}

	public Map<String, List<Long>> getImageViews(ProgramResources.Bindings bindings) {
		var views = new LinkedHashMap<String, List<Long>>();
		bindings.images().forEach((name, images) -> views.put(name, images.stream()
			.map(image -> image.image() == null ? ((VulkanGpuTextureView) image.texture()).vkImageView() : image.image().getView())
			.toList()));

		return views;
	}

	public List<VulkanSynchronization.Access> getResourceAccesses(ProgramResources.Bindings bindings) {
		var accesses = new ArrayList<VulkanSynchronization.Access>();

		for (var entry : this.resources.getSamplerReferences().entrySet()) {
			var descriptor = this.resources.getSamplerDeclarations().get(entry.getKey());
			var binding = bindings.textures().get(entry.getValue());

			if (binding == null) {
				accesses.add(new VulkanSynchronization.Access(new VulkanSynchronization.Resource(false, 0), stages(descriptor.stages()),
					KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR, false));
			} else {
				long image = binding.image() == null ? VulkanSynchronization.image(binding.texture().texture()).handle() : binding.image()
					.getImage();
				accesses.add(VulkanSynchronization.shaderImage(image, stages(descriptor.stages()), false));
			}
		}

		for (var descriptor : this.resources.getImageDeclarations().values()) {
			for (var binding : bindings.images().get(descriptor.name())) {
				long image = binding.image() == null ? VulkanSynchronization.image(binding.texture().texture()).handle() : binding.image()
					.getImage();
				accesses.add(VulkanSynchronization.shaderImage(image, stages(descriptor.stages()), true));
			}
		}

		var buffers = bindings.buffers();

		for (var descriptor : this.resources.getStorageDeclarations()) {
			for (var slice : buffers.get(descriptor.name())) {
				accesses.add(new VulkanSynchronization.Access(
					new VulkanSynchronization.Resource(false, ((VulkanGpuBuffer) slice.buffer()).vkBuffer()), stages(descriptor.stages()),
					KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_SHADER_WRITE_BIT_KHR, true));
			}
		}

		return accesses;
	}
}
