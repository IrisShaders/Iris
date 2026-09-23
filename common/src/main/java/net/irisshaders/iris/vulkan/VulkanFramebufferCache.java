package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VulkanFramebufferCache {
	private final VkDevice device;
	private final Map<Key, Long> framebuffers = new HashMap<>();

	public VulkanFramebufferCache(VkDevice device) {
		this.device = device;
	}

	public long get(long renderPass, long[] views, int width, int height) {
		if (width <= 0 || height <= 0 || Arrays.stream(views).anyMatch(view -> view == 0)) {
			throw new IllegalArgumentException("invalid framebuffer attachments");
		}

		var key = new Key(renderPass, Arrays.stream(views).boxed().toList(), width, height);
		return this.framebuffers.computeIfAbsent(key, unused -> {
			try (var stack = MemoryStack.stackPush()) {
				var result = stack.mallocLong(1);
				int status = VK10.vkCreateFramebuffer(this.device,
					VkFramebufferCreateInfo.calloc(stack).sType$Default().renderPass(renderPass).pAttachments(stack.longs(views))
						.width(width).height(height).layers(1), null, result);

				if (status != VK10.VK_SUCCESS) {
					throw new IllegalStateException("cannot create framebuffer: " + status);
				}

				return result.get(0);
			}
		});
	}

	public void release(long view, VulkanCommandEncoder encoder) {
		var iterator = this.framebuffers.entrySet().iterator();

		while (iterator.hasNext()) {
			var entry = iterator.next();

			if (entry.getKey().views().contains(view)) {
				long framebuffer = entry.getValue();
				iterator.remove();
				encoder.queueForDestroy(() -> VK10.vkDestroyFramebuffer(this.device, framebuffer, null));
			}
		}
	}

	public void close(VulkanCommandEncoder encoder) {
		for (long framebuffer : this.framebuffers.values()) {
			encoder.queueForDestroy(() -> VK10.vkDestroyFramebuffer(this.device, framebuffer, null));
		}

		this.framebuffers.clear();
	}

	public record Key(long renderPass, List<Long> views, int width, int height) {
	}
}
