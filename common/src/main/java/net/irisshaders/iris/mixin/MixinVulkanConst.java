package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import net.irisshaders.iris.backend.IrisGpuFormats;
import org.lwjgl.vulkan.VK10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanConst.class)
public class MixinVulkanConst {
	@Inject(method = "toVk(Lcom/mojang/renderpearl/api/GpuFormat;)I", at = @At("HEAD"), cancellable = true)
	private static void iris$packedFormat(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		if (format == IrisGpuFormats.RGB565_UNORM) {
			cir.setReturnValue(VK10.VK_FORMAT_R5G6B5_UNORM_PACK16);
		} else if (format == IrisGpuFormats.RGB5A1_UNORM) {
			cir.setReturnValue(VK10.VK_FORMAT_A1R5G5B5_UNORM_PACK16);
		} else if (format == IrisGpuFormats.RGBA4_UNORM) {
			cir.setReturnValue(VK10.VK_FORMAT_R4G4B4A4_UNORM_PACK16);
		} else if (format == IrisGpuFormats.RGB9E5_FLOAT) {
			cir.setReturnValue(VK10.VK_FORMAT_E5B9G9R9_UFLOAT_PACK32);
		}
	}
}
