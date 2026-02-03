package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import org.joml.Vector4i;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A simple optimization to avoid redundant glBindFramebuffer calls, works in principle the same as things like
 * glBindTexture in GlStateManager.
 */
@Mixin(GlStateManager.class)
public class MixinGlStateManager_FramebufferBinding {
	private static int iris$program;

	private static Vector4i iris$viewport = new Vector4i();

	@Inject(method = "_glUseProgram", at = @At("HEAD"), cancellable = true, remap = false)
	private static void iris$avoidRedundantBind2(int pInt0, CallbackInfo ci) {
		if (iris$program == 0 && pInt0 == 0) {
			ci.cancel();
		}

		IrisRenderSystem.onProgramUse();

		iris$program = pInt0;
	}

	@Inject(method = "_activeTexture", at = @At("HEAD"))
	private static void iris$checkActiveTexture(int i, CallbackInfo ci) {
		int tex = i - GL46C.GL_TEXTURE0;
		if (tex < 0 || tex > 128) throw new IllegalArgumentException("Texture " + tex + " out of range");
	}

	@Inject(method = "_viewport", at = @At("HEAD"), cancellable = true)
	private static void iris$avoidRedundantBind1(int x, int y, int width, int height, CallbackInfo ci) {
		if (iris$viewport.x == x && iris$viewport.y == y && iris$viewport.z == width && iris$viewport.w == height) {
			ci.cancel();
		} else {
			iris$viewport.set(x, y, width, height);
		}
	}
}
