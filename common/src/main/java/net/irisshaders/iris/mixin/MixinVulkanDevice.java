package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import net.irisshaders.iris.mixinterface.VulkanPipelineCacheAccess;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineCacheCreateInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.irisshaders.iris.vulkan.VulkanChecks.check;

@Mixin(VulkanDevice.class)
public abstract class MixinVulkanDevice implements VulkanPipelineCacheAccess {
	@Unique
	private long iris$pipelineCache;

	@Shadow
	public abstract VkDevice vkDevice();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$createPipelineCache(CallbackInfo ci) {
		try (var stack = MemoryStack.stackPush()) {
			var handle = stack.mallocLong(1);
			// Default flags let compilation threads share the cache with driver-managed synchronization.
			check(VK10.vkCreatePipelineCache(this.vkDevice(), VkPipelineCacheCreateInfo.calloc(stack).sType$Default(), null, handle),
				"create pipeline cache");
			this.iris$pipelineCache = handle.get(0);
		}
	}

	@Override
	public long iris$getPipelineCache() {
		return this.iris$pipelineCache;
	}

	@Inject(method = "close", at = @At(value = "INVOKE",
		target = "Lorg/lwjgl/vulkan/VK12;vkDestroyDevice(Lorg/lwjgl/vulkan/VkDevice;Lorg/lwjgl/vulkan/VkAllocationCallbacks;)V"))
	private void iris$destroyPipelineCache(CallbackInfo ci) {
		VK10.vkDestroyPipelineCache(this.vkDevice(), this.iris$pipelineCache, null);
	}
}
