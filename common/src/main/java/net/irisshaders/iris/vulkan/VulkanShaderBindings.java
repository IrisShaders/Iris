package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRPushDescriptor;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.util.List;

public class VulkanShaderBindings {
	private final ProgramSamplers resources;
	private final VulkanPackResources pack;
	private final List<SamplerHeapLayout.Slot> drawSlots;
	private final List<ShaderResourceLayout.Descriptor> drawResources;
	private final long drawLayout;

	public VulkanShaderBindings(ProgramSamplers resources, net.irisshaders.iris.pipeline.transform.PatchedShader shader) {
		this.resources = resources;
		this.pack = (VulkanPackResources) resources.getPackSamplers().getResources();
		var types = shader.layout().descriptors().stream().filter(d -> d.set() == SamplerHeapLayout.DRAW_DESCRIPTOR_SET
			&& d.kind() == ShaderResourceLayout.Kind.SAMPLER)
			.map(d -> SamplerHeapLayout.Type.valueOf(d.type())).collect(java.util.stream.Collectors.toSet());
		this.drawResources = shader.layout().descriptors().stream().filter(d -> d.set() == SamplerHeapLayout.DRAW_DESCRIPTOR_SET
			&& d.kind() != ShaderResourceLayout.Kind.SAMPLER).toList();
		if (shader.layout().uniformBufferSize() > 0) {
			try (var stack = MemoryStack.stackPush()) {
				var properties = VkPhysicalDeviceProperties.calloc(stack);
				VK10.vkGetPhysicalDeviceProperties(VulkanCommandBridge.getDevice().vkDevice().getPhysicalDevice(), properties);
				if (shader.layout().uniformBufferSize() > Integer.toUnsignedLong(properties.limits().maxUniformBufferRange())) {
					throw new IllegalArgumentException("uniform block exceeds maxUniformBufferRange");
				}
			}
		}
		var plan = resources.getBindingPlan();
		this.drawSlots = plan.slots().stream().filter(slot -> plan.heap().isDraw(slot) && types.contains(slot.type())).toList();
		this.drawLayout = this.pack.getSamplerHeap().drawLayout(shader.layout().descriptors());
	}

	public long drawLayout() { return this.drawLayout; }

	public void bind(VkCommandBuffer command, long layout, int bindPoint, DrawUniforms uniforms) {
		var entries = this.pack.resolve(this.resources);
		this.pack.update(entries);
		var heap = this.pack.getSamplerHeap();
		heap.bind(command);
		int count = this.drawSlots.size() + this.drawResources.size();
		if (count == 0) return;
		try (var stack = MemoryStack.stackPush()) {
			var writes = VkWriteDescriptorSet.calloc(count, stack);
			int element = 0;
			SamplerHeapLayout.Type previous = null;
			for (int i = 0; i < this.drawSlots.size(); i++) {
				var slot = this.drawSlots.get(i);
				if (slot.type() != previous) {
					previous = slot.type();
					element = 0;
				}
				var write = writes.get(i).sType$Default().dstBinding(slot.type().binding()).dstArrayElement(element++).descriptorCount(1);
				if (entries.get(slot) instanceof VulkanSamplerHeap.ImageSampler sampled) {
					var info = VkDescriptorImageInfo.calloc(1, stack);
					info.get(0).imageView(sampled.imageView()).sampler(sampled.sampler()).imageLayout(sampled.imageLayout());
					write.descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).pImageInfo(info);
				} else {
					write.descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER)
						.pTexelBufferView(stack.longs(((VulkanSamplerHeap.TexelBuffer) entries.get(slot)).bufferView()));
				}
			}
			for (int i = 0; i < this.drawResources.size(); i++) {
				var descriptor = this.drawResources.get(i);
				var write = writes.get(this.drawSlots.size() + i).sType$Default().dstBinding(descriptor.binding()).descriptorType(descriptorType(descriptor));
				if (descriptor.kind() == ShaderResourceLayout.Kind.IMAGE) {
					var image = this.resources.getImages().get(descriptor.name());
					var info = VkDescriptorImageInfo.calloc(1, stack);
					info.get(0).imageView(image.image() == null ? ((VulkanGpuTextureView) image.texture().get()).vkImageView() : image.image().get().getView())
						.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
					write.descriptorCount(1).pImageInfo(info);
					continue;
				}
				var slices = uniforms.uniformBuffers().get(descriptor.name());
				var info = VkDescriptorBufferInfo.calloc(slices.size(), stack);
				for (int j = 0; j < slices.size(); j++) {
					var slice = slices.get(j);
					info.get(j).buffer(((VulkanGpuBuffer) slice.buffer()).vkBuffer()).offset(slice.offset()).range(slice.length());
				}
				write.descriptorCount(slices.size()).pBufferInfo(info);
			}
			KHRPushDescriptor.vkCmdPushDescriptorSetKHR(command, bindPoint, layout, SamplerHeapLayout.DRAW_DESCRIPTOR_SET, writes);
		}
	}

	public static int descriptorType(ShaderResourceLayout.Descriptor descriptor) {
		return switch (descriptor.kind()) {
			case UNIFORM_BUFFER -> VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
			case STORAGE_BUFFER -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
			case IMAGE -> VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
			case SAMPLER -> SamplerHeapLayout.Type.valueOf(descriptor.type()).texelBuffer()
				? VK10.VK_DESCRIPTOR_TYPE_UNIFORM_TEXEL_BUFFER : VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
		};
	}

	public static int stageFlags(ShaderResourceLayout.Descriptor descriptor) {
		int stages = 0;
		for (var stage : descriptor.stages()) stages |= VulkanShaderCompiler.stageFlag(stage);
		return stages;
	}

	public record LayoutBinding(int binding, int type, int count, int stages) {
	}

}
