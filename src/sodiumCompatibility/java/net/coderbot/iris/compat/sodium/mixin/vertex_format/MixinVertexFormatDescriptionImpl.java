package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.render.vertex.VertexFormatDescriptionImpl;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexFormatDescriptionImpl.class)
public class MixinVertexFormatDescriptionImpl {
	// A better fix would be to treat IrisVertexFormats.PADDING_SHORT as padding, but this works too.
	@Inject(method = "checkSimple", at = @At("HEAD"), cancellable = true)
	private static void iris$forceSimple(VertexFormat format, CallbackInfoReturnable<Boolean> cir) {
		if (format == IrisVertexFormats.TERRAIN || format == IrisVertexFormats.ENTITY || format == IrisVertexFormats.GLYPH) {
			cir.setReturnValue(true);
		}
	}
}
