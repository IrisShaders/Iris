package net.irisshaders.iris.mixin.statelisteners;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.state.StateUpdateNotifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
public class MixinRenderSystem {
	@Unique
	private static Runnable fogStartListener;
	@Unique
	private static Runnable fogEndListener;

	static {
		StateUpdateNotifiers.fogStartNotifier = listener -> fogStartListener = listener;
		StateUpdateNotifiers.fogEndNotifier = listener -> fogEndListener = listener;
	}

	@Inject(method = "setShaderFog", at = @At(value = "HEAD"))
	private static void iris$onFogStart(GpuBufferSlice gpuBufferSlice, CallbackInfo ci) {
		if (fogStartListener != null) {
			fogStartListener.run();
		}
		if (fogEndListener != null) {
			fogEndListener.run();
		}
	}
}
