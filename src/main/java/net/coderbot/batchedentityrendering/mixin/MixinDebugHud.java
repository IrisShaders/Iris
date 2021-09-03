package net.coderbot.batchedentityrendering.mixin;

import net.coderbot.batchedentityrendering.impl.DrawCallTrackingBufferBuilderStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Inject(method = "getLeftText", at = @At("RETURN"))
    private void batchedentityrendering$appendStats(CallbackInfoReturnable<List<String>> cir) {
        List<String> messages = cir.getReturnValue();

		DrawCallTrackingBufferBuilderStorage drawTracker = (DrawCallTrackingBufferBuilderStorage) MinecraftClient.getInstance().getBufferBuilders();

        int drawCalls = drawTracker.getDrawCalls();
        int renderTypes = drawTracker.getRenderTypes();

        // blank line separator
        messages.add("");

        if (drawCalls > 0) {
        	int effectivenessTimes10 = renderTypes * 1000 / drawCalls;
        	float effectiveness = effectivenessTimes10 / 10.0F;

            messages.add("[Entity Batching] " + drawCalls + " draw calls / " + renderTypes + " render types = "
					+ effectiveness + "% batching effectiveness)");
        } else {
            messages.add("[Entity Batching] (no draw calls)");
        }
    }
}
