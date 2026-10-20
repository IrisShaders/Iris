package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vulkan.VulkanShaderFeatures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanBackend.class)
public class MixinVulkanBackend {
	@ModifyExpressionValue(method = "createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuDebugOptions;useLabels()Z"))
	private boolean iris$enableDebugLabels(boolean enabled) {
		return enabled || Iris.getIrisConfig().areDebugOptionsEnabled();
	}

	@ModifyExpressionValue(method = "createDevice(Lcom/mojang/renderpearl/api/device/GpuDebugOptions;)Lcom/mojang/renderpearl/api/device/GpuDevice;", at = @At(value = "NEW", target = "(Ljava/lang/String;Ljava/util/Collection;)Lcom/mojang/renderpearl/backend/vulkan/init/FeatureSet;"))
	private FeatureSet iris$enableShaderFeatures(FeatureSet enabled,
		@Local VulkanPhysicalDevice physicalDevice) throws BackendCreationException {
		return enabled.composite(VulkanShaderFeatures.getOptionalFeatures(physicalDevice.vkPhysicalDevice()));
	}
}
