package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer {
	@Inject(method = "submitWater", at = @At(value = "HEAD"), cancellable = true)
	private static void iris$disableUnderWaterOverlayRendering(PlayerRenderState.WaterOverlay waterOverlay, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderUnderwaterOverlay()) {
			ci.cancel();
		}
	}
}
