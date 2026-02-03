package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.mixinterface.RenderTargetInterface;
import net.irisshaders.iris.targets.Blaze3dRenderTargetExt;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Final;
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
	@Final
	public boolean useDepth;
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
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, ((GlTexture) this.colorTexture).getFbo(((GlDevice) RenderSystem.getDevice()).directStateAccess(), this.depthTexture));
	}
}
