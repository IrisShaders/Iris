package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.IrisGpuFormats;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.VulkanPushConstants;
import net.irisshaders.iris.samplers.SamplerHeapLayout;
import net.irisshaders.iris.samplers.VulkanHeapLayout;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorImageInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties2;
import org.lwjgl.vulkan.VkPhysicalDeviceSubgroupProperties;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;
import static org.lwjgl.vulkan.KHRSynchronization2.*;

// This is ported from the technique I used in Aperture. Modded AMD SPD.
// Please do not touch this (unless you are planning to get rid of it...)
public class VulkanColorMipmaps implements AutoCloseable {
	private static final int MAX_MIPS = 12;
	private final Object2BooleanMap<GpuFormat> supported = new Object2BooleanAVLTreeMap<>();
	private final Map<GpuFormat, Pipelines> pipelines = new EnumMap<>(GpuFormat.class);
	private final Map<GpuTexture, MipResources> resources = new IdentityHashMap<>();
	private long layout;
	private long setLayout;
	private boolean subgroups;

	private static String read(String name) {
		try (var stream = VulkanColorMipmaps.class.getResourceAsStream("/assets/iris/shaders/mipmaps/" + name)) {
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static int consecutiveMips(int width, int height, int remaining) {
		int limit = Math.min(remaining, Math.max(width, height) > 4096 ? 6 : MAX_MIPS);
		int count = 0;
		boolean oneDimensional = width == 1 || height == 1;
		while (count < limit && (width == 1 || width % 2 == 0) && (height == 1 || height % 2 == 0)) {
			width = Math.max(1, width / 2);
			height = Math.max(1, height / 2);
			count++;
			if (!oneDimensional && (width == 1 || height == 1)) {
				break;
			}
		}
		return count;
	}

	public boolean supports(GpuFormat format) {
		return this.supported.computeBooleanIfAbsent(format, candidate -> {
			if (!candidate.hasColorAspect() || candidate.componentCount() == 3 || PipelineState.isInteger(
				candidate) || candidate == IrisGpuFormats.RGB565_UNORM || candidate == IrisGpuFormats.RGB5A1_UNORM
				|| candidate == IrisGpuFormats.RGBA4_UNORM || candidate == IrisGpuFormats.RGB9E5_FLOAT) {
				return false;
			}

			try (var stack = MemoryStack.stackPush()) {
				var device = VulkanCommandBridge.getDevice().vkDevice();
				var properties = VkFormatProperties.calloc(stack);
				VK10.vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), VulkanConst.toVk(candidate), properties);
				int required = VK10.VK_FORMAT_FEATURE_STORAGE_IMAGE_BIT | VK10.VK_FORMAT_FEATURE_SAMPLED_IMAGE_BIT;
				var limits = VkPhysicalDeviceProperties2.calloc(stack).sType$Default();
				VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(), limits);

				return (properties.optimalTilingFeatures() & required) == required && limits.properties().limits()
					.maxPerStageDescriptorStorageImages() >= MAX_MIPS && limits.properties().limits()
					.maxDescriptorSetStorageImages() >= MAX_MIPS && limits.properties().limits()
					.maxComputeWorkGroupInvocations() >= 256 && limits.properties().limits().maxComputeWorkGroupSize(0) >= 256;
			}
		});
	}

	private void initialize(VulkanSamplerHeap heap) {
		if (this.layout != 0) {
			return;
		}

		var device = VulkanCommandBridge.getDevice().vkDevice();
		try (var stack = MemoryStack.stackPush()) {
			var subgroup = VkPhysicalDeviceSubgroupProperties.calloc(stack).sType$Default();
			var properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(subgroup);
			VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(), properties);
			this.subgroups = (subgroup.supportedStages() & VK10.VK_SHADER_STAGE_COMPUTE_BIT) != 0 && (subgroup.supportedOperations() & VK11.VK_SUBGROUP_FEATURE_QUAD_BIT) != 0;

			var handle = stack.mallocLong(1);
			var bindings = VkDescriptorSetLayoutBinding.calloc(2, stack);
			bindings.get(0).binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.descriptorCount(1).stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			bindings.get(1).binding(VulkanHeapLayout.DRAW_BUFFER_BINDING).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE)
				.descriptorCount(MAX_MIPS).stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			// push descriptors *cannot* be used here!!! If you do, MoltenVK will not use argument buffers and you'll hit the stupid texture limit
			check(VK10.vkCreateDescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default()
				.pBindings(bindings), null, handle), "create mipmap descriptor layout");
			this.setLayout = handle.get(0);
			check(VK10.vkCreatePipelineLayout(device,
				VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(heap.pipelineSetLayouts(stack, this.setLayout))
					.pPushConstantRanges(VkPushConstantRange.calloc(1, stack).stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT).size(VulkanPushConstants.MAX_SIZE)), null,
				handle), "create mipmap pipeline layout");
			this.layout = handle.get(0);
		} catch (RuntimeException | Error failure) {
			this.close();
			throw failure;
		}
	}

	private Pipelines createPipelines(GpuFormat format, VulkanSamplerHeap heap) {
		this.initialize(heap);
		String qualifier = switch (format) {
			case RGB10A2_UNORM -> "rgb10_a2";
			case RG11B10_FLOAT -> "r11f_g11f_b10f";
			default -> format.name().toLowerCase(Locale.ROOT).replace("_unorm", "").replace("_float", "f");
		};
		var sources = sources(qualifier, this.subgroups, heap.layout().buffers(), heap.mipmapCounterIndex());
		long spd = 0;
		try (var compiler = new VulkanShaderCompiler()) {
			spd = this.compile(compiler, "spd",
				sources.get("spd"));
			return new Pipelines(spd, this.compile(compiler, "area", sources.get("area")));
		} catch (RuntimeException | Error failure) {
			if (spd != 0) {
				VK10.vkDestroyPipeline(VulkanCommandBridge.getDevice().vkDevice(), spd, null);
			}
			throw failure;
		}
	}

	private static Map<String, String> sources(String qualifier, boolean subgroups, int buffers, int counterIndex) {
		String header = "#version 450\n" + (subgroups ? "" : "#define FFX_SPD_NO_WAVE_OPERATIONS\n") + VulkanHeapLayout.sourceSampler2D("sourceImage") + """
			layout(%s, set = %d, binding = %d) coherent uniform image2D mipImages[12];
			layout(std430, set = %d, binding = %d) coherent buffer Counter { uint value; } iris_mipCounters[%d];
			#define counter iris_mipCounters[%d].value
			layout(push_constant, std430) uniform Parameters { %s layout(offset = %d) ivec2 sourceSize; uint mips; uint numWorkGroups; uint initialMips; };
			""".formatted(qualifier, SamplerHeapLayout.DRAW_DESCRIPTOR_SET, VulkanHeapLayout.DRAW_BUFFER_BINDING, VulkanHeapLayout.DESCRIPTOR_SET, VulkanHeapLayout.BUFFERS_BINDING, buffers, counterIndex,
				VulkanPushConstants.MEMBERS, VulkanPushConstants.NATIVE_OFFSET);
		StringBuilder accessors = new StringBuilder("void storeMip(uint mip, ivec2 tex, vec4 value) { switch (mip) {\n");
		for (int i = 0; i < MAX_MIPS; i++) {
			accessors.append("case ").append(i).append(": imageStore(mipImages[").append(i).append("], tex, value); break;\n");
		}
		accessors.append("}}\n");
		accessors.append("vec4 loadMip(uint mip, ivec2 tex) { switch (mip) {\ncase 0: return texelFetch(sourceImage, tex, 0);\n");
		for (int i = 0; i < MAX_MIPS - 1; i++) {
			accessors.append("case ").append(i + 1).append(": return imageLoad(mipImages[").append(i).append("], tex);\n");
		}
		accessors.append("} return vec4(0.0); }\n");
		String reductions = accessors + read("tail.glsl");

		return Map.of("spd", header + read("ffx_core.h") + reductions + read("spd.glsl").replace("// FFX_SPD", read("ffx_spd.h")),
			"area", header + reductions + read("area.glsl"));
	}

	private long compile(VulkanShaderCompiler compiler, String name, String source) {
		var device = VulkanCommandBridge.getDevice().vkDevice();
		var spirv = compiler.compile("mipmaps/" + name + ".csh", PatchShaderType.COMPUTE, source);
		long module = 0;
		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			check(VK10.vkCreateShaderModule(device, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle),
				"create mipmap shader module");
			module = handle.get(0);
			var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(this.layout);
			info.stage().sType$Default().stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
			check(VK10.vkCreateComputePipelines(device, VulkanCommandBridge.pipelineCache(), info, null, handle), "create mipmap compute pipeline");
			return handle.get(0);
		} finally {
			if (module != 0) {
				VK10.vkDestroyShaderModule(device, module, null);
			}
			MemoryUtil.memFree(spirv);
		}
	}

	public boolean generate(GpuTexture texture, VulkanSamplerHeap heap) {
		if (texture.getMipLevels() <= 1) {
			return true;
		}

		if (texture.getDepthOrLayers() != 1 || (texture.usage() & RenderTarget.USAGE_STORAGE) == 0 || !this.supports(
			texture.getFormat())) {
			return false;
		}

		var command = VulkanCommandBridge.commandBuffer();
		var programs = this.pipelines.computeIfAbsent(texture.getFormat(), format -> this.createPipelines(format, heap));
		var sampler = heap.sampler(new GlSampler.Description(false, false, false, false, true));
		var mipResources = this.resources.computeIfAbsent(texture, this::createResources);
		var levels = mipResources.levels;

		try (var stack = MemoryStack.stackPush()) {
			var constants = stack.mallocInt(5);

			for (int base = 0; base < levels.size() - 1; ) {
				int width = Math.max(1, texture.getWidth(base)), height = Math.max(1, texture.getHeight(base));
				int count = consecutiveMips(width, height, levels.size() - base - 1);
				boolean area = count == 0;
				if (area) {
					count = 1;
				}

				int initialMips = count;
				if (Math.max(1, width >> count) <= 64 && Math.max(1, height >> count) <= 64) {
					count = Math.min(MAX_MIPS, levels.size() - base - 1);
					if (count > initialMips) {
						// The custom tail elects its own workgroup; stop before SPD's election.
						initialMips = Math.min(initialMips, 6);
					}
				}

				int groupsX = area ? (Math.max(1, width / 2) + 15) / 16 : (width + 63) / 64;
				int groupsY = area ? (Math.max(1, height / 2) + 15) / 16 : (height + 63) / 64;
				int groups = Math.multiplyExact(groupsX, groupsY);
				Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT_KHR, VK_ACCESS_2_SHADER_READ_BIT_KHR,
					VK_ACCESS_2_SHADER_WRITE_BIT_KHR);
				VK10.vkCmdBindPipeline(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, area ? programs.area : programs.spd);
				heap.bind(command);
				this.bindDescriptors(command, mipResources, base, count, sampler, stack);
				constants.put(0, width).put(1, height).put(2, count).put(3, groups).put(4, initialMips);
				VK10.vkCmdPushConstants(command, this.layout, VK10.VK_SHADER_STAGE_COMPUTE_BIT, VulkanPushConstants.NATIVE_OFFSET, constants);
				VK10.vkCmdDispatch(command, groupsX, groupsY, 1);
				base += count;
			}
		}
		return true;
	}

	private MipResources createResources(GpuTexture texture) {
		var result = new ArrayList<GpuTextureView>();
		var device = VulkanCommandBridge.getDevice().vkDevice();
		long pool = 0;
		try (var stack = MemoryStack.stackPush()) {
			for (int level = 0; level < texture.getMipLevels(); level++) {
				result.add(RenderSystem.getDevice().createTextureView(texture, level, 1));
			}
			int batches = texture.getMipLevels() - 1;
			var sizes = VkDescriptorPoolSize.calloc(2, stack);
			sizes.get(0).type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(batches);
			sizes.get(1).type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(batches * MAX_MIPS);
			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorPool(device, VkDescriptorPoolCreateInfo.calloc(stack).sType$Default()
				.maxSets(batches).pPoolSizes(sizes), null, handle), "create mipmap descriptor pool");
			pool = handle.get(0);
			return new MipResources(result, pool, new long[batches]);
		} catch (RuntimeException | Error failure) {
			if (pool != 0) {
				VK10.vkDestroyDescriptorPool(device, pool, null);
			}
			result.forEach(GpuTextureView::close);
			throw failure;
		}
	}

	private void bindDescriptors(org.lwjgl.vulkan.VkCommandBuffer command, MipResources resources, int base, int count, long sampler, MemoryStack stack) {
		long set = resources.sets[base];
		if (set == 0) {
			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(command.getDevice(), VkDescriptorSetAllocateInfo.calloc(stack).sType$Default()
				.descriptorPool(resources.pool).pSetLayouts(stack.longs(this.setLayout)), handle), "allocate mipmap descriptors");
			set = handle.get(0);
			this.writeDescriptors(command.getDevice(), set, resources, base, count, sampler, stack);
			resources.sets[base] = set;
		}
		VK10.vkCmdBindDescriptorSets(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.layout,
			SamplerHeapLayout.DRAW_DESCRIPTOR_SET, stack.longs(set), null);
	}

	private void writeDescriptors(org.lwjgl.vulkan.VkDevice device, long set, MipResources resources, int base, int count, long sampler, MemoryStack stack) {
		var input = VkDescriptorImageInfo.calloc(1, stack);
		input.get(0).imageView(((VulkanGpuTextureView) resources.levels.get(base)).vkImageView()).sampler(sampler)
			.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
		var outputs = VkDescriptorImageInfo.calloc(MAX_MIPS, stack);
		for (int i = 0; i < MAX_MIPS; i++) {
			outputs.get(i).imageView(((VulkanGpuTextureView) resources.levels.get(base + Math.min(i + 1, count))).vkImageView())
				.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
		}
		var writes = VkWriteDescriptorSet.calloc(2, stack);
		writes.get(0).sType$Default().dstSet(set).dstBinding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
			.descriptorCount(1).pImageInfo(input);
		writes.get(1).sType$Default().dstSet(set).dstBinding(VulkanHeapLayout.DRAW_BUFFER_BINDING)
			.descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(MAX_MIPS).pImageInfo(outputs);
		VK10.vkUpdateDescriptorSets(device, writes, null);
	}

	public void release(GpuTexture texture) {
		var retired = this.resources.remove(texture);
		if (retired != null) {
			retired.close();
		}
	}

	@Override
	public void close() {
		this.resources.values().forEach(MipResources::close);
		this.resources.clear();
		if (this.layout == 0 && this.setLayout == 0) {
			return;
		}

		var device = VulkanCommandBridge.getDevice();
		long oldLayout = this.layout;
		long oldSetLayout = this.setLayout;
		var oldPipelines = List.copyOf(this.pipelines.values());
		device.createCommandEncoder().queueForDestroy(() -> {
			for (var programs : oldPipelines) {
				VK10.vkDestroyPipeline(device.vkDevice(), programs.spd, null);
				VK10.vkDestroyPipeline(device.vkDevice(), programs.area, null);
			}
			VK10.vkDestroyPipelineLayout(device.vkDevice(), oldLayout, null);
			VK10.vkDestroyDescriptorSetLayout(device.vkDevice(), oldSetLayout, null);
		});
		this.pipelines.clear();
		this.layout = 0;
		this.setLayout = 0;
	}

	public record Pipelines(long spd, long area) {
	}

	public record MipResources(List<GpuTextureView> levels, long pool, long[] sets) implements AutoCloseable {
		@Override
		public void close() {
			var device = VulkanCommandBridge.getDevice().vkDevice();
			VulkanCommandBridge.deleteWhenDone(() -> VK10.vkDestroyDescriptorPool(device, this.pool, null));
			this.levels.forEach(GpuTextureView::close);
		}
	}
}
