package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;
import net.irisshaders.iris.mixinterface.VulkanPipelineCacheAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(VulkanRenderPipeline.class)
public class MixinVulkanRenderPipeline {
	@ModifyArg(method = "compile", at = @At(value = "INVOKE",
		target = "Lorg/lwjgl/vulkan/VK12;vkCreateGraphicsPipelines(Lorg/lwjgl/vulkan/VkDevice;JLorg/lwjgl/vulkan/VkGraphicsPipelineCreateInfo$Buffer;Lorg/lwjgl/vulkan/VkAllocationCallbacks;Ljava/nio/LongBuffer;)I"), index = 1)
	private static long iris$usePipelineCache(long cache, @Local(argsOnly = true) VulkanDevice device) {
		return ((VulkanPipelineCacheAccess) device).iris$getPipelineCache();
	}
}
