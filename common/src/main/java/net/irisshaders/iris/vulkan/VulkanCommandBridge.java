package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.mixin.FrontendDeviceAccessor;
import net.irisshaders.iris.mixinterface.VulkanPipelineCacheAccess;
import org.lwjgl.vulkan.VkCommandBuffer;

public class VulkanCommandBridge {
	public static VulkanDevice getDevice() {
		return (VulkanDevice) ((FrontendDeviceAccessor) RenderSystem.getDevice()).iris$getBackend();
	}

	public static VulkanCommandEncoder encoder() {
		return getDevice().createCommandEncoder();
	}

	public static VkCommandBuffer commandBuffer() {
		return encoder().commandBuffer();
	}

	public static long pipelineCache() {
		return ((VulkanPipelineCacheAccess) getDevice()).iris$getPipelineCache();
	}

	public static void deleteWhenDone(Runnable destructor) {
		encoder().queueForDestroy(destructor::run);
	}
}
