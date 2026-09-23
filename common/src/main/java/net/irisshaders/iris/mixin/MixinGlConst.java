package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlConst;
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
		switch (format) {
			case IRIS_RGB565_UNORM -> cir.setReturnValue(GL41C.GL_RGB565);
			case IRIS_RGB5A1_UNORM -> cir.setReturnValue(GL11C.GL_RGB5_A1);
			case IRIS_RGB9E5_FLOAT -> cir.setReturnValue(GL30C.GL_RGB9_E5);
		}
	}

	@Inject(method = "toGlExternalId", at = @At("HEAD"), cancellable = true)
	private static void iris$packedExternalFormat(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		switch (format) {
			case IRIS_RGB565_UNORM -> cir.setReturnValue(GL11C.GL_RGB);
			case IRIS_RGB5A1_UNORM -> cir.setReturnValue(GL12C.GL_BGRA);
			case IRIS_RGB9E5_FLOAT -> cir.setReturnValue(GL11C.GL_RGB);
		}
	}

	@Inject(method = "toGlType", at = @At("HEAD"), cancellable = true)
	private static void iris$packedPixelType(GpuFormat format, CallbackInfoReturnable<Integer> cir) {
		switch (format) {
			case IRIS_RGB565_UNORM -> cir.setReturnValue(GL12C.GL_UNSIGNED_SHORT_5_6_5);
			case IRIS_RGB5A1_UNORM -> cir.setReturnValue(GL12C.GL_UNSIGNED_SHORT_1_5_5_5_REV);
			case IRIS_RGB9E5_FLOAT -> cir.setReturnValue(GL30C.GL_UNSIGNED_INT_5_9_9_9_REV);
		}
	}

	@Inject(method = "isFormatNormalized", at = @At("HEAD"), cancellable = true)
	private static void iris$packedNormalized(GpuFormat format, CallbackInfoReturnable<Boolean> cir) {
		if (format == GpuFormat.IRIS_RGB565_UNORM || format == GpuFormat.IRIS_RGB5A1_UNORM) {
			cir.setReturnValue(true);
		}
	}
}
