package net.coderbot.iris.compat.sodium.mixin.font;

import net.coderbot.iris.vertices.ImmediateState;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public class MixinFont {
	@Inject(method = "accept", at = @At("HEAD"))
	private void iris$beforeFlushBuffer(int pFont$StringRenderOutput0, Style pStyle1, int pInt2, CallbackInfoReturnable<Boolean> cir) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(method = "accept", at = @At("TAIL"))
	private void iris$afterFlushBuffer(int pFont$StringRenderOutput0, Style pStyle1, int pInt2, CallbackInfoReturnable<Boolean> cir) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}

	private boolean iris$notRenderingLevel() {
		return !ImmediateState.isRenderingLevel;
	}
}
