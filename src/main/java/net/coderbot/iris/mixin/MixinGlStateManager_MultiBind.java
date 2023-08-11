package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.Arrays;

@Mixin(value = GlStateManager.class, remap = false)
public class MixinGlStateManager_MultiBind {
	@Shadow
	private static int activeTexture;
	private static boolean hasChangedTextures = false;
	private static int[] streamlinedTextures = new int[64];
	private static int realActiveTexture = 0;

	static {
		Arrays.fill(streamlinedTextures, 0);
	}

	@Inject(method = "_bindTexture", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"), cancellable = true, remap = false)
	private static void iris$fakeBindTexture(int pInt0, CallbackInfo ci) {
		hasChangedTextures = true;
		streamlinedTextures[activeTexture] = pInt0;
		ci.cancel();
	}


	@Overwrite(remap = false)
	public static int _genTexture() {
		RenderSystem.assertOnRenderThreadOrInit();
		return GL46C.glCreateTextures(GL46C.GL_TEXTURE_2D);
	}

	@Overwrite(remap = false)
	public static void _genTextures(int[] pIntArray0) {
		RenderSystem.assertOnRenderThreadOrInit();
		GL46C.glCreateTextures(GL46C.GL_TEXTURE_2D, pIntArray0);
	}

	@Inject(method = "_activeTexture", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glActiveTexture(I)V"), cancellable = true, remap = false)
	private static void iris$fakeBindTexture2(int pInt0, CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = {
		"_texImage2D", "_texParameter(III)V", "_texParameter(IIF)V", "_getTexLevelParameter", "_drawElements", "_glDrawPixels", "_glCopyTexSubImage2D", "_getTexImage", "_texSubImage2D", "_glFramebufferTexture2D", "_glBindFramebuffer"
	}, at = @At("HEAD"), remap = false)
	private static void iris$bindAllAtOnce(CallbackInfo ci) {
		bindAllAtOnce();
	}

	private static void bindAllAtOnce() {
		if (hasChangedTextures) {
			hasChangedTextures = false;
			GL46C.glBindTextures(0, streamlinedTextures);
		}

		if (activeTexture != realActiveTexture) {
			realActiveTexture = activeTexture;
			GL46C.glActiveTexture(activeTexture + 33984);
		}
	}

	@Inject(method = "_logicOp", at = @At("HEAD"), cancellable = true, remap = false)
	private static void iris$redirectLogicOp(int pInt0, CallbackInfo ci) {
		if (pInt0 == 91384) {
			// Magic number!
			bindAllAtOnce();

			ci.cancel();
		}
	}
}
