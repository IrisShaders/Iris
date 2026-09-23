package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import net.irisshaders.iris.mixin.VulkanDeviceAccessor;
import org.lwjgl.vulkan.EXTGraphicsPipelineLibrary;
import org.lwjgl.vulkan.KHRPipelineLibrary;
import org.lwjgl.vulkan.VkPhysicalDeviceGraphicsPipelineLibraryFeaturesEXT;

import java.util.Set;

public class VulkanPipelineLibraries {
	// GPL support; much faster pipeline loading :)
	public static final FeatureSet FEATURES = new FeatureSet("Iris graphics pipeline libraries",
		Set.of(EXTGraphicsPipelineLibrary.VK_EXT_GRAPHICS_PIPELINE_LIBRARY_EXTENSION_NAME,
			KHRPipelineLibrary.VK_KHR_PIPELINE_LIBRARY_EXTENSION_NAME), Set.of(
		new VulkanFeature(new VulkanPNextStruct(VkPhysicalDeviceGraphicsPipelineLibraryFeaturesEXT.class), "graphicsPipelineLibrary")));

	private VulkanPipelineLibraries() {
	}

	public static boolean isSupported(VulkanDevice device) {
		return device instanceof VulkanDeviceAccessor access && access.iris$getEnabledFeatures().contains(FEATURES);
	}
}
