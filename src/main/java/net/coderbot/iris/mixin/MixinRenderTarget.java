package net.coderbot.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	private boolean iris$dirty = false;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$dirty = true;
	}

	@Override
	public boolean iris$isDepthBufferDirty() {
		return iris$dirty;
	}

	@Override
	public void iris$clearDepthBufferDirtyFlag() {
		iris$dirty = false;
	}
}
