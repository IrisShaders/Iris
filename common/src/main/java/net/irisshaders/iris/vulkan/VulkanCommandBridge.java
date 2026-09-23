package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.frontend.FrontendCommandEncoder;
import net.irisshaders.iris.gl.buffer.ShaderStorageBuffer;
import net.irisshaders.iris.mixin.FrontendDeviceAccessor;
import org.lwjgl.vulkan.VkCommandBuffer;

public class VulkanCommandBridge {
	private VulkanCommandBridge() {
	}

	public static VulkanDevice getDevice() {
		var frontend = RenderSystem.getDevice();

		if (!(frontend instanceof FrontendDeviceAccessor access) || !(access.iris$getBackend() instanceof VulkanDevice device)) {
			throw new IllegalArgumentException("expected Minecraft's Vulkan device");
		}

		return device;
	}

	public static VulkanCommandEncoder encoder(CommandEncoder encoder) {
		if (!(encoder instanceof FrontendCommandEncoder frontend) || !(frontend.backend() instanceof VulkanCommandEncoder backend)) {
			throw new IllegalArgumentException("expected Minecraft's Vulkan encoder");
		}

		if (frontend.isInRenderPass()) {
			throw new IllegalStateException("Iris native work must be recorded outside a Minecraft render pass");
		}

		return backend;
	}

	public static VkCommandBuffer commandBuffer(VulkanCommandEncoder encoder) {
		if (encoder.currentRenderPass != null) {
			throw new IllegalStateException("Iris native work must be recorded outside a Minecraft render pass");
		}

		return encoder.commandBuffer();
	}

	public static VulkanResourceSets.BufferSlice bufferSlice(GpuBufferSlice slice) {
		if (!(slice.buffer() instanceof VulkanGpuBuffer buffer) || buffer.isClosed() || (buffer.usage() & (GpuBuffer.USAGE_UNIFORM | ShaderStorageBuffer.USAGE_STORAGE)) == 0 || slice.offset() < 0 || slice.length() <= 0 || slice.offset() > buffer.size() - slice.length()) {
			throw new IllegalArgumentException("invalid Vulkan buffer slice");
		}

		return new VulkanResourceSets.BufferSlice(buffer.vkBuffer(), slice.offset(), slice.length());
	}

	public static void retire(CommandEncoder encoder, Runnable destructor) {
		encoder(encoder).queueForDestroy(destructor::run);
	}
}
