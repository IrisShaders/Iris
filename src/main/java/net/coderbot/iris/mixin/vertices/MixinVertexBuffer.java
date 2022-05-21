package net.coderbot.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
public class MixinVertexBuffer {
	@Shadow
	@Final
	@Mutable
	private VertexFormat format;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$onInit(VertexFormat format, CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			// We have to fix the vertex format here, or else the vertex count will be calculated wrongly and too many
			// vertices will be drawn.
			//
			// Needless to say, that is not good if you don't like access violation crashes!
			if (format == DefaultVertexFormat.BLOCK) {
				this.format = IrisVertexFormats.TERRAIN;
			} else if (format == DefaultVertexFormat.NEW_ENTITY || format == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
				this.format = IrisVertexFormats.ENTITY;
			}
		}
	}
}
