package net.coderbot.iris.mixin.vertices;

import net.coderbot.iris.vertices.IrisVertexFormats;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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
	@Inject(method = "startDrawing(J)V", at = @At("HEAD"), cancellable = true)
	private void iris$onStartDrawing(long pointer, CallbackInfo ci) {
		if ((Object) this == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
			IrisVertexFormats.TERRAIN.startDrawing(pointer);

			ci.cancel();
		}
	}

	@Inject(method = "endDrawing()V", at = @At("HEAD"), cancellable = true)
	private void iris$onEndDrawing(CallbackInfo ci) {
		if ((Object) this == VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) {
			IrisVertexFormats.TERRAIN.endDrawing();

			ci.cancel();
		}
	}
}
