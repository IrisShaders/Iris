package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.backend.vulkan.Destroyable;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.GraphicsBackend;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VulkanGpuTextureView.class)
public class MixinVulkanTextureViewLifecycle {
	@WrapOperation(method = "close", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;queueForDestroy(Lcom/mojang/renderpearl/backend/vulkan/Destroyable;)V"))
	private void iris$releaseFramebuffers(VulkanCommandEncoder encoder, Destroyable view, Operation<Void> original) {
		if (GraphicsBackend.isVulkan()) {
			var framebuffers = ((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache();
			long handle = ((VulkanGpuTextureView) view).vkImageView();
			// Pending clears can still use this view until submission; retire their framebuffers with the view.
			original.call(encoder, (Destroyable) () -> {
				framebuffers.destroyFramebuffers(handle);
				view.destroy();
			});
		} else {
			original.call(encoder, view);
		}
	}
}
