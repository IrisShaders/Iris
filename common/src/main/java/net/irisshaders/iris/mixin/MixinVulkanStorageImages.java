package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import net.irisshaders.iris.targets.RenderTarget;
import org.lwjgl.vulkan.VK10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanConst.class)
public class MixinVulkanStorageImages {
	@Inject(method = "textureUsageToVk", at = @At("RETURN"), cancellable = true)
	private static void iris$storageUsage(int usage, GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		if ((usage & RenderTarget.USAGE_STORAGE) != 0) {
			cir.setReturnValue(cir.getReturnValueI() | VK10.VK_IMAGE_USAGE_STORAGE_BIT);
		}
	}
}
