package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VulkanDevice.class)
public interface VulkanDeviceAccessor {
	@Accessor("enabledFeatures")
	FeatureSet iris$getEnabledFeatures();
}
