package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlConst;
import net.irisshaders.iris.backend.IrisGpuFormats;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL41C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlConst.class)
public class MixinGlConst {
	@Inject(method = "toGlInternalId", at = @At("HEAD"), cancellable = true)
	private static void iris$packedInternalFormat(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		if (format == IrisGpuFormats.RGB565_UNORM) {
			cir.setReturnValue(GL41C.GL_RGB565);
		} else if (format == IrisGpuFormats.RGB5A1_UNORM) {
			cir.setReturnValue(GL11C.GL_RGB5_A1);
		} else if (format == IrisGpuFormats.RGBA4_UNORM) {
			cir.setReturnValue(GL11C.GL_RGBA4);
		} else if (format == IrisGpuFormats.RGB9E5_FLOAT) {
			cir.setReturnValue(GL30C.GL_RGB9_E5);
		}
	}

	@Inject(method = "toGlExternalId", at = @At("HEAD"), cancellable = true)
	private static void iris$packedExternalFormat(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		if (format == IrisGpuFormats.RGB565_UNORM || format == IrisGpuFormats.RGB9E5_FLOAT) {
			cir.setReturnValue(GL11C.GL_RGB);
		} else if (format == IrisGpuFormats.RGB5A1_UNORM) {
			cir.setReturnValue(GL12C.GL_BGRA);
		} else if (format == IrisGpuFormats.RGBA4_UNORM) {
			cir.setReturnValue(GL11C.GL_RGBA);
		}
	}

	@Inject(method = "toGlType", at = @At("HEAD"), cancellable = true)
	private static void iris$packedPixelType(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		if (format == IrisGpuFormats.RGB565_UNORM) {
			cir.setReturnValue(GL12C.GL_UNSIGNED_SHORT_5_6_5);
		} else if (format == IrisGpuFormats.RGB5A1_UNORM) {
			cir.setReturnValue(GL12C.GL_UNSIGNED_SHORT_1_5_5_5_REV);
		} else if (format == IrisGpuFormats.RGBA4_UNORM) {
			cir.setReturnValue(GL12C.GL_UNSIGNED_SHORT_4_4_4_4);
		} else if (format == IrisGpuFormats.RGB9E5_FLOAT) {
			cir.setReturnValue(GL30C.GL_UNSIGNED_INT_5_9_9_9_REV);
		}
	}

	@Inject(method = "isFormatNormalized", at = @At("HEAD"), cancellable = true)
	private static void iris$packedNormalized(GpuFormat format, CallbackInfoReturnable<Boolean> cir) {
		if (format == IrisGpuFormats.RGB565_UNORM || format == IrisGpuFormats.RGB5A1_UNORM || format == IrisGpuFormats.RGBA4_UNORM) {
			cir.setReturnValue(true);
		}
	}
}
