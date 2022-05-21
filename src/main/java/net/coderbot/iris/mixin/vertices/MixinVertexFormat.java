package net.coderbot.iris.mixin.vertices;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.vertices.ImmediateState;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that the correct state for the extended vertex format is set up when needed.
 */
@Mixin(VertexFormat.class)
public class MixinVertexFormat {
	@Inject(method = "setupBufferState", at = @At("HEAD"), cancellable = true)
	private void iris$onSetupBufferState(long pointer, CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() && ImmediateState.renderWithExtendedVertexFormat) {
			if ((Object) this == DefaultVertexFormat.BLOCK) {
				IrisVertexFormats.TERRAIN.setupBufferState(pointer);

				ci.cancel();
			} else if ((Object) this == DefaultVertexFormat.NEW_ENTITY || (Object) this == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
				IrisVertexFormats.ENTITY.setupBufferState(pointer);

				ci.cancel();
			}
		}
	}

	@Inject(method = "clearBufferState", at = @At("HEAD"), cancellable = true)
	private void iris$onClearBufferState(CallbackInfo ci) {
		if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() && ImmediateState.renderWithExtendedVertexFormat) {
			if ((Object) this == DefaultVertexFormat.BLOCK) {
				IrisVertexFormats.TERRAIN.clearBufferState();

				ci.cancel();
			} else if ((Object) this == DefaultVertexFormat.NEW_ENTITY || (Object) this == DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP) {
				IrisVertexFormats.ENTITY.clearBufferState();

				ci.cancel();
			}
		}
	}
}
