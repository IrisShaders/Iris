package net.irisshaders.iris.vulkan;

import org.lwjgl.vulkan.VK10;

public class VulkanChecks {
	 public static void check(int status, String action) {
		if (status != VK10.VK_SUCCESS) {
			throw new IllegalStateException("failed to " + action + ": " + status);
		}
	}
}
