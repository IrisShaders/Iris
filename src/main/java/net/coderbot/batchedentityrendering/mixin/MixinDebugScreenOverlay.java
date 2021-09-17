package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.BatchingDebugMessageHelper;
import net.coderbot.batchedentityrendering.impl.DrawCallTrackingRenderBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MixinDebugScreenOverlay {
    @Inject(method = "getGameInformation", at = @At("RETURN"))
    private void batchedentityrendering$appendStats(CallbackInfoReturnable<List<String>> cir) {
        List<String> messages = cir.getReturnValue();

		DrawCallTrackingRenderBuffers drawTracker = (DrawCallTrackingRenderBuffers) Minecraft.getInstance().renderBuffers();

        // blank line separator
        messages.add("");
		messages.add("[Entity Batching] " + BatchingDebugMessageHelper.getDebugMessage(drawTracker));
    }
}
