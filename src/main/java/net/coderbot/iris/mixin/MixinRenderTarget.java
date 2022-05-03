package net.coderbot.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import net.coderbot.iris.samplers.DepthBufferTracker;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Shadow
	private int depthBufferId;

	private boolean iris$depthDirty = false;
	private boolean iris$colorDirty = false;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthDirty = true;
		iris$colorDirty = true;
	}

	@Inject(method = "createBuffers(IIZ)V", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/platform/GlStateManager._bindTexture (I)V"))
	private void iris$onCreateDepthBuffer(int width, int height, boolean checkError, CallbackInfo ci) {
		DepthBufferTracker.INSTANCE.trackDepthBuffer(this.depthBufferId);
	}

	@ModifyArg(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"), index = 2)
	private int iris$use16(int internalformat) {
		if (internalformat == GL43C.GL_DEPTH_COMPONENT) {
			// If a format is not automatically selected, we need to select 32-bit float-based and hope for the best.
			// **Do not** use 16 bit! It looks fine normally, but far away objects start having major precision issues.
			internalformat = GL43C.GL_DEPTH_COMPONENT32F;
		}
		return internalformat;
	}

	@Override
	public boolean iris$isDepthBufferDirty() {
		return iris$depthDirty;
	}

	@Override
	public void iris$clearDepthBufferDirtyFlag() {
		iris$depthDirty = false;
	}

	@Override
	public boolean iris$isColorBufferDirty() {
		return iris$colorDirty;
	}

	@Override
	public void iris$clearColorBufferDirtyFlag() {
		iris$colorDirty = false;
	}
}
