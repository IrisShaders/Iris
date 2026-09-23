package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pipeline.GraphicsBackend;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanGpuTexture.class)
public class MixinVulkanTextureLifecycle {
	@Inject(method = "close", at = @At("HEAD"))
	private void iris$releaseTrackedTexture(CallbackInfo ci) {
		var texture = (VulkanGpuTexture) (Object) this;
		if (GraphicsBackend.isVulkan()) {
			((VulkanRenderBackend) Iris.getRenderBackend()).onDeleteTexture(texture);
		}

		TextureTracker.INSTANCE.onDeleteTexture(texture);
		PBRTextureManager.INSTANCE.onDeleteTexture(texture);
	}
}
