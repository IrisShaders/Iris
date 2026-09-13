package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.RenderTargetInterface;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Allows Iris to detect when the depth texture was re-created, so we can re-attach it
 * to the shader framebuffers. See DeferredWorldRenderingPipeline and RenderTargets.
 */
@Mixin(RenderTarget.class)
public class MixinRenderTarget implements Blaze3dRenderTargetExt, RenderTargetInterface {
	@Shadow
	@Nullable
	protected GpuTexture colorTexture;
	@Shadow
	@Nullable
	protected GpuTexture depthTexture;
	@Unique
	private int iris$depthBufferVersion;
	@Unique
	private int iris$colorBufferVersion;

	@Inject(method = "destroyBuffers()V", at = @At("HEAD"))
	private void iris$onDestroyBuffers(CallbackInfo ci) {
		iris$depthBufferVersion++;
		iris$colorBufferVersion++;
	}

	@Override
	public int iris$getDepthBufferVersion() {
		return iris$depthBufferVersion;
	}

	@Override
	public int iris$getColorBufferVersion() {
		return iris$colorBufferVersion;
	}

	@Override
	public void iris$bindFramebuffer() {
		var pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null) {
			pipeline.setIsMainBound((Object) this == Minecraft.getInstance().gameRenderer.mainRenderTarget());
		}
	}
}
