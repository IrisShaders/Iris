package net.irisshaders.iris.gl.sampler;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkPhysicalDeviceProperties;

public final class SamplerLimits {
	private static SamplerLimits instance;
	private final int maxTextureUnits;
	private final int maxDrawBuffers;
	private final int maxShaderStorageUnits;

	private SamplerLimits() {
		var device = VulkanCommandBridge.device(RenderSystem.getDevice());

		try (var stack = MemoryStack.stackPush()) {
			var properties = VkPhysicalDeviceProperties.calloc(stack);
			VK10.vkGetPhysicalDeviceProperties(device.vkDevice().getPhysicalDevice(), properties);

			var limits = properties.limits();
			maxTextureUnits = Math.min(limits.maxPerStageDescriptorSamplers(), limits.maxPerStageDescriptorSampledImages());
			maxDrawBuffers = limits.maxColorAttachments();
			maxShaderStorageUnits = limits.maxPerStageDescriptorStorageBuffers();
		}
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public int getMaxDrawBuffers() {
		return maxDrawBuffers;
	}

	public int getMaxShaderStorageUnits() {
		return maxShaderStorageUnits;
	}
}
