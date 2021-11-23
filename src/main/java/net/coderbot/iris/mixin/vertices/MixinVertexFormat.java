package net.coderbot.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Redirect all attempts to render with POSITION_COLOR_TEXTURE_LIGHT_NORMAL to render with the properly extended vertex
 * format.
 */
@Mixin(VertexFormat.class)
public class MixinVertexFormat {
	@Inject(method = "setupBufferState", at = @At("HEAD"), cancellable = true)
	private void iris$onSetupBufferState(long pointer, CallbackInfo ci) {
		if ((Object) this == DefaultVertexFormat.BLOCK) {
			IrisVertexFormats.TERRAIN.setupBufferState(pointer);

			ci.cancel();
		}
	}

	@Inject(method = "clearBufferState", at = @At("HEAD"), cancellable = true)
	private void iris$onClearBufferState(CallbackInfo ci) {
		if ((Object) this == DefaultVertexFormat.BLOCK) {
			IrisVertexFormats.TERRAIN.clearBufferState();

			ci.cancel();
		}
	}
}
