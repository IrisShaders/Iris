package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.state.StateUpdateNotifiers;
import net.coderbot.iris.samplers.TextureAtlasTracker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;

@Mixin(GlStateManager.class)
public class MixinGlStateManager_AtlasTracking {
	private static Runnable atlasTextureListener;

	@Shadow
	private static int activeTexture;

	@Inject(method = "_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V", at = @At("HEAD"))
	private static void iris$onTexImage2D(int target, int level, int internalformat, int width, int height, int border,
										  int format, int type, @Nullable IntBuffer pixels, CallbackInfo ci) {
		TextureAtlasTracker.INSTANCE.trackTexImage2D(GlStateManager.getActiveTextureName(), level, width, height);
	}

	@Inject(method = "_bindTexture(I)V", at = @At("HEAD"))
	private static void iris$onBindTexture(int id, CallbackInfo ci) {
		if (activeTexture == 0 && atlasTextureListener != null && !TextureAtlasTracker.IS_FETCHING_SIZE) {
			atlasTextureListener.run();
		}
	}

	@Inject(method = "_deleteTexture(I)V", at = @At("HEAD"))
	private static void iris$onDeleteTexture(int id, CallbackInfo ci) {
		TextureAtlasTracker.INSTANCE.trackDeleteTextures(id);
	}

	@Inject(method = "_deleteTextures([I)V", at = @At("HEAD"))
	private static void iris$onDeleteTextures(int[] ids, CallbackInfo ci) {
		for (int id : ids) {
			TextureAtlasTracker.INSTANCE.trackDeleteTextures(id);
		}
	}

	static {
		StateUpdateNotifiers.atlasTextureNotifier = listener -> atlasTextureListener = listener;
	}
}
