package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
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
import org.lwjgl.vulkan.VkSamplerCreateInfo;
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

// This is ported from the technique I used in Aperture. Modded AMD SPD.
public class VulkanColorMipmaps implements AutoCloseable {
	private static final int MAX_MIPS = 12;
	private final Map<GpuFormat, Boolean> supported = new EnumMap<>(GpuFormat.class);
	private final Map<GpuFormat, Pipelines> pipelines = new EnumMap<>(GpuFormat.class);
	private final Map<GpuTexture, MipResources> resources = new IdentityHashMap<>();
	private VulkanStorageBuffer counter;
	private long setLayout;
	private long layout;
	private long sampler;
	private boolean subgroups;

	private static String read(String name) {
		try (var stream = VulkanColorMipmaps.class.getResourceAsStream("/assets/iris/shaders/mipmaps/" + name)) {
			if (stream == null) {
				throw new IllegalStateException("missing mipmap shader: " + name);
			}
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

	private static void check(int result) {
		if (result != VK10.VK_SUCCESS) {
			throw new IllegalStateException("Vulkan mipmap error: " + result);
		}
	}

	public boolean supports(GpuFormat format) {
		return this.supported.computeIfAbsent(format, candidate -> {
			if (!candidate.hasColorAspect() || candidate.componentCount() == 3 || PipelineState.isInteger(
				candidate) || candidate == GpuFormat.IRIS_RGB565_UNORM || candidate == GpuFormat.IRIS_RGB5A1_UNORM || candidate == GpuFormat.IRIS_RGB9E5_FLOAT) {
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

	private void initialize() {
		if (this.layout != 0) {
			return;
		}

		var device = VulkanCommandBridge.getDevice().vkDevice();
		try (var stack = MemoryStack.stackPush()) {
			var subgroup = VkPhysicalDeviceSubgroupProperties.calloc(stack).sType$Default();
			var properties = VkPhysicalDeviceProperties2.calloc(stack).sType$Default().pNext(subgroup);
			VK11.vkGetPhysicalDeviceProperties2(device.getPhysicalDevice(), properties);
			this.subgroups = (subgroup.supportedStages() & VK10.VK_SHADER_STAGE_COMPUTE_BIT) != 0 && (subgroup.supportedOperations() & VK11.VK_SUBGROUP_FEATURE_QUAD_BIT) != 0;

			var bindings = VkDescriptorSetLayoutBinding.calloc(3, stack);
			bindings.get(0).binding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1)
				.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			bindings.get(1).binding(1).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(MAX_MIPS)
				.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			bindings.get(2).binding(2).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(1)
				.stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT);
			var handle = stack.mallocLong(1);
			check(
				VK10.vkCreateDescriptorSetLayout(device, VkDescriptorSetLayoutCreateInfo.calloc(stack).sType$Default().pBindings(bindings),
					null, handle));
			this.setLayout = handle.get(0);
			check(VK10.vkCreatePipelineLayout(device,
				VkPipelineLayoutCreateInfo.calloc(stack).sType$Default().pSetLayouts(stack.longs(this.setLayout))
					.pPushConstantRanges(VkPushConstantRange.calloc(1, stack).stageFlags(VK10.VK_SHADER_STAGE_COMPUTE_BIT).size(20)), null,
				handle));
			this.layout = handle.get(0);
			check(VK10.vkCreateSampler(device,
				VkSamplerCreateInfo.calloc(stack).sType$Default().minFilter(VK10.VK_FILTER_NEAREST).magFilter(VK10.VK_FILTER_NEAREST)
					.mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST).addressModeU(VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE)
					.addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE).addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE),
				null, handle));
			this.sampler = handle.get(0);
			this.counter = new VulkanStorageBuffer(VulkanCommandBridge.getDevice(), "Iris mipmap counter", 4, null);
		} catch (RuntimeException | Error failure) {
			this.close();
			throw failure;
		}
	}

	private Pipelines createPipelines(GpuFormat format) {
		this.initialize();
		String qualifier = switch (format) {
			case RGB10A2_UNORM -> "rgb10_a2";
			case RG11B10_FLOAT -> "r11f_g11f_b10f";
			default -> format.name().toLowerCase(Locale.ROOT).replace("_unorm", "").replace("_float", "f");
		};
		String header = "#version 450\n" + (this.subgroups ? "" : "#define FFX_SPD_NO_WAVE_OPERATIONS\n") + """
			layout(set = 0, binding = 0) uniform sampler2D sourceImage;
			layout(%s, set = 0, binding = 1) coherent uniform image2D mipImages[12];
			layout(std430, set = 0, binding = 2) coherent buffer Counter { uint counter; };
			layout(push_constant) uniform Parameters { ivec2 sourceSize; uint mips; uint numWorkGroups; uint initialMips; };
			""".formatted(qualifier);
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

		long spd = 0;
		try (var compiler = new VulkanShaderCompiler()) {
			spd = this.compile(compiler, "spd",
				header + read("ffx_core.h") + reductions + read("spd.glsl").replace("// FFX_SPD", read("ffx_spd.h")));
			return new Pipelines(spd, this.compile(compiler, "area", header + reductions + read("area.glsl")));
		} catch (RuntimeException | Error failure) {
			if (spd != 0) {
				VK10.vkDestroyPipeline(VulkanCommandBridge.getDevice().vkDevice(), spd, null);
			}
			throw failure;
		}
	}

	private long compile(VulkanShaderCompiler compiler, String name, String source) {
		var device = VulkanCommandBridge.getDevice().vkDevice();
		var spirv = compiler.compile("mipmaps/" + name + ".csh", PatchShaderType.COMPUTE, source);
		long module = 0;
		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			check(VK10.vkCreateShaderModule(device, VkShaderModuleCreateInfo.calloc(stack).sType$Default().pCode(spirv), null, handle));
			module = handle.get(0);
			var info = VkComputePipelineCreateInfo.calloc(1, stack).sType$Default().layout(this.layout);
			info.stage().sType$Default().stage(VK10.VK_SHADER_STAGE_COMPUTE_BIT).module(module).pName(stack.UTF8("main"));
			check(VK10.vkCreateComputePipelines(device, 0, info, null, handle));
			return handle.get(0);
		} finally {
			if (module != 0) {
				VK10.vkDestroyShaderModule(device, module, null);
			}
			MemoryUtil.memFree(spirv);
		}
	}

	public boolean generate(GpuTexture texture) {
		if (texture.isClosed()) {
			throw new IllegalStateException("mipmap texture is closed");
		}

		if (texture.getMipLevels() <= 1) {
			return true;
		}

		if (!(texture instanceof VulkanGpuTexture image) || texture.getDepthOrLayers() != 1 || (texture.usage() & RenderTarget.USAGE_STORAGE) == 0 || !this.supports(
			texture.getFormat())) {
			return false;
		}

		var encoder = VulkanCommandBridge.encoder(RenderSystem.getDevice().createCommandEncoder());
		var command = VulkanCommandBridge.commandBuffer(encoder);
		var programs = this.pipelines.computeIfAbsent(texture.getFormat(), this::createPipelines);
		var mipResources = this.resources.computeIfAbsent(texture, this::createResources);
		var levels = mipResources.levels;
		var imageAccess = VulkanSynchronization.shaderImage(image.vkImage(), VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, true);
		var imageAccesses = List.of(imageAccess);
		var counterAccesses = List.of(imageAccess,
			new VulkanSynchronization.Access(new VulkanSynchronization.Resource(false, this.counter.vkBuffer()),
				VK10.VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT, VK10.VK_ACCESS_SHADER_READ_BIT | VK10.VK_ACCESS_SHADER_WRITE_BIT, true));

		try (var stack = MemoryStack.stackPush()) {
			var constants = stack.mallocInt(5);
			var descriptor = stack.mallocLong(1);

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
				descriptor.put(0, this.descriptorSet(mipResources, base, count));
				boolean usesCounter = initialMips > 6 || (count > initialMips && groups > 1);
				VulkanSynchronization.before(command, stack, usesCounter ? counterAccesses : imageAccesses);
				VK10.vkCmdBindPipeline(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, area ? programs.area : programs.spd);
				VK10.vkCmdBindDescriptorSets(command, VK10.VK_PIPELINE_BIND_POINT_COMPUTE, this.layout, 0, descriptor, null);
				constants.put(0, width).put(1, height).put(2, count).put(3, groups).put(4, initialMips);
				VK10.vkCmdPushConstants(command, this.layout, VK10.VK_SHADER_STAGE_COMPUTE_BIT, 0, constants);
				VK10.vkCmdDispatch(command, groupsX, groupsY, 1);
				base += count;
			}
			VulkanSynchronization.after(command, stack);
		}
		return true;
	}

	private MipResources createResources(GpuTexture texture) {
		var result = new ArrayList<GpuTextureView>();
		try (var stack = MemoryStack.stackPush()) {
			for (int level = 0; level < texture.getMipLevels(); level++) {
				result.add(RenderSystem.getDevice().createTextureView(texture, level, 1));
			}
			int capacity = result.size() - 1;
			var sets = new long[capacity];
			var sizes = VkDescriptorPoolSize.calloc(3, stack);
			sizes.get(0).type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(capacity);
			sizes.get(1).type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(capacity * MAX_MIPS);
			sizes.get(2).type(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(capacity);
			var handle = stack.mallocLong(1);
			check(VK10.vkCreateDescriptorPool(VulkanCommandBridge.getDevice().vkDevice(),
				VkDescriptorPoolCreateInfo.calloc(stack).sType$Default().maxSets(capacity).pPoolSizes(sizes), null, handle));
			return new MipResources(result, handle.get(0), sets);
		} catch (RuntimeException | Error failure) {
			result.forEach(GpuTextureView::close);
			throw failure;
		}
	}

	private long descriptorSet(MipResources resources, int base, int count) {
		if (resources.sets[base] != 0) {
			return resources.sets[base];
		}

		var device = VulkanCommandBridge.getDevice().vkDevice();
		try (var stack = MemoryStack.stackPush()) {
			var source = VkDescriptorImageInfo.calloc(1, stack).sampler(this.sampler).imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL)
				.imageView(((VulkanGpuTextureView) resources.levels.get(base)).vkImageView());
			var outputs = VkDescriptorImageInfo.calloc(MAX_MIPS, stack);
			for (int i = 0; i < MAX_MIPS; i++) {
				outputs.get(i).imageView(((VulkanGpuTextureView) resources.levels.get(base + Math.min(i + 1, count))).vkImageView())
					.imageLayout(VK10.VK_IMAGE_LAYOUT_GENERAL);
			}
			var buffer = VkDescriptorBufferInfo.calloc(1, stack).buffer(this.counter.vkBuffer()).range(4);
			var writes = VkWriteDescriptorSet.calloc(3, stack);
			writes.get(0).sType$Default().dstBinding(0).descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER).descriptorCount(1)
				.pImageInfo(source);
			writes.get(1).sType$Default().dstBinding(1).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE).descriptorCount(MAX_MIPS)
				.pImageInfo(outputs);
			writes.get(2).sType$Default().dstBinding(2).descriptorType(VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER).descriptorCount(1)
				.pBufferInfo(buffer);
			var handle = stack.mallocLong(1);
			check(VK10.vkAllocateDescriptorSets(device,
				VkDescriptorSetAllocateInfo.calloc(stack).sType$Default().descriptorPool(resources.pool)
					.pSetLayouts(stack.longs(this.setLayout)), handle));
			long set = handle.get(0);
			for (int i = 0; i < writes.capacity(); i++) {
				writes.get(i).dstSet(set);
			}
			VK10.vkUpdateDescriptorSets(device, writes, null);
			resources.sets[base] = set;
			return set;
		}
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
		if (this.setLayout == 0 && this.layout == 0 && this.sampler == 0) {
			return;
		}

		var device = VulkanCommandBridge.getDevice();
		long oldLayout = this.layout, oldSetLayout = this.setLayout, oldSampler = this.sampler;
		var oldPipelines = List.copyOf(this.pipelines.values());
		if (this.counter != null) {
			this.counter.close();
		}
		device.createCommandEncoder().queueForDestroy(() -> {
			for (var programs : oldPipelines) {
				VK10.vkDestroyPipeline(device.vkDevice(), programs.spd, null);
				VK10.vkDestroyPipeline(device.vkDevice(), programs.area, null);
			}
			VK10.vkDestroySampler(device.vkDevice(), oldSampler, null);
			VK10.vkDestroyPipelineLayout(device.vkDevice(), oldLayout, null);
			VK10.vkDestroyDescriptorSetLayout(device.vkDevice(), oldSetLayout, null);
		});
		this.pipelines.clear();
		this.layout = this.setLayout = this.sampler = 0;
		this.counter = null;
	}

	public record Pipelines(long spd, long area) {
	}

	public record MipResources(List<GpuTextureView> levels, long pool, long[] sets) implements AutoCloseable {
		@Override
		public void close() {
			this.levels.forEach(GpuTextureView::close);
			var device = VulkanCommandBridge.getDevice();
			device.createCommandEncoder().queueForDestroy(() -> VK10.vkDestroyDescriptorPool(device.vkDevice(), this.pool, null));
		}
	}
}
