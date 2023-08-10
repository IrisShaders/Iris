package net.coderbot.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.coderbot.iris.rendertarget.Blaze3dRenderTargetExt;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(value = RenderTarget.class, priority = 990)
public class MixinRenderTarget implements Blaze3dRenderTargetExt {
	@Shadow
	private int depthBufferId;

	private int iris$depthBufferVersion;
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@ModifyArgs(method = "createBuffers",
		at = @At (value = "INVOKE",
			target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V",
			ordinal = 0))
	public void init(Args args) {
		// Never allow depth format to be unsized. This *will* cause problems otherwise!
		args.set(2, GL30.GL_DEPTH_COMPONENT16);
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}
}
