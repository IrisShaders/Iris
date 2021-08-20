package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Makes the ScreenSize uniform be based on the current viewport size, not the window size.
 *
 * This makes line rendering work properly in the shadow pass, or if a shader pack uses
 * a custom viewport size.
 */
@Mixin(GlUniform.class)
public class MixinGlUniform {
	@Shadow
	@Final
	private String name;

	@Shadow
	private boolean stateDirty;

	@Shadow
	public final void set(float x, float y) {
		throw new AssertionError();
	}

	@Unique
	private boolean isScreenSize;

	@Inject(method = "<init>()V", at = @At("RETURN"))
	private void iris$onInit(String name, int dataType, int count, GlShader program, CallbackInfo ci) {
		isScreenSize = "ScreenSize".equals(name) || "iris_ScreenSize".equals(name);
	}

	@Inject(method = "upload()V", at = @At("HEAD"))
	public void iris$upload(CallbackInfo ci) {
		if (isScreenSize) {
			// Make sure that this uniform is always re-uploaded.
			stateDirty = true;
		}
	}

	@Inject(method = "uploadFloats()V", at = @At("HEAD"))
	private void iris$uploadFloats(CallbackInfo ci) {
		if (isScreenSize) {
			set(GlStateManager.Viewport.getWidth(), GlStateManager.Viewport.getHeight());

			// Vanilla has already cleared the dirty flag by this point, but we've just reactivated it.
			// So let's clear it again.
			stateDirty = false;
		}
	}
}
