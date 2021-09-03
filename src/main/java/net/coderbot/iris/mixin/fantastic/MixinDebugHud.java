package net.coderbot.iris.mixin.fantastic;

import net.coderbot.iris.fantastic.FullyBufferedVertexConsumerProvider;
import net.coderbot.iris.fantastic.MemoryTrackingBufferBuilderStorage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

//This mixin is unused in production.

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Inject(method = "getRightText", at = @At("RETURN"))
    private void appendShaderPackText(CallbackInfoReturnable<List<String>> cir) {
        List<String> messages = cir.getReturnValue();

		MemoryTrackingBufferBuilderStorage memoryTracker = (MemoryTrackingBufferBuilderStorage) MinecraftClient.getInstance().getBufferBuilders();
		messages.add(5, "[Entity Batching] Misc Buffers Allocated Size: " + memoryTracker.getMiscBufferAllocatedSize());
		messages.add(5, "[Entity Batching] Entity Buffers Size: " + memoryTracker.getEntityBufferAllocatedSize());
        messages.add(5, "[Entity Batching] WorldRenderer recursion depth (shouldn't go beyond 1): " + memoryTracker.getMaxBegins());

        int drawCalls = FullyBufferedVertexConsumerProvider.instance.getDrawCalls();
        int renderLayers = FullyBufferedVertexConsumerProvider.instance.getRenderLayers();

        if (drawCalls > 0) {
            messages.add(5, "[Entity Batching] Draw Calls: " + drawCalls);
            messages.add(5, "[Entity Batching] Render Layers: " + renderLayers + " (" + (renderLayers * 1000 / drawCalls) / 10.0F + "% batching effectiveness)");
        } else {
            messages.add(5, "[Entity Batching] (no draw calls)");
        }
    }
}
