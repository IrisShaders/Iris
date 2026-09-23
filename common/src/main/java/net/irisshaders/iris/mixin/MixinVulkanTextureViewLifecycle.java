package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuTextureView;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.GraphicsBackend;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuTextureView.class)
public class MixinVulkanTextureViewLifecycle {
	@Inject(method = "close", at = @At("HEAD"))
	private void iris$releaseFramebuffers(CallbackInfo ci) {
		if (GraphicsBackend.isVulkan()) {
			((VulkanRenderBackend) Iris.getRenderBackend()).onDeleteTextureView(((VulkanGpuTextureView) (Object) this).vkImageView());
		}
	}
}
