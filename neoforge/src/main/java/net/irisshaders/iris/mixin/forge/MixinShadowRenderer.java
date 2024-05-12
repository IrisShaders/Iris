package net.irisshaders.iris.mixin.forge;

import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@Mixin(ShadowRenderer.class)
public class MixinShadowRenderer {
	@Unique
	private static MethodHandle IEhandle;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void init(CallbackInfo ci) {
		try {
			IEhandle = MethodHandles.lookup().findStatic(Class.forName("blusunrize.immersiveengineering.client.utils.VertexBufferHolder"), "afterTERRendering", MethodType.methodType(void.class));
		} catch (Throwable e) {
			IEhandle = null;
		}
	}

	@Inject(method = "renderShadows", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private void render(LevelRendererAccessor levelRenderer, Camera playerCamera, CallbackInfo ci) {
		if (IEhandle != null) {
			try {
				IEhandle.invokeExact();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}
}
