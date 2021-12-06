package net.coderbot.iris.mixin;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;

@Mixin(ScreenEffectRenderer.class)
public abstract class MixinScreenEffectRenderer {
	@Inject(method = "renderWater", at = @At(value = "HEAD"), cancellable = true)
	private static void iris$disableUnderWaterOverlayRendering(Minecraft minecraft, PoseStack poseStack, CallbackInfo ci) {
		Optional<ShaderPack> shaderPack = Iris.getCurrentPack();

		if (shaderPack.isPresent()) {
			if (shaderPack.get().getProgramSet(Iris.getCurrentDimension()).getPackDirectives().underwaterOverlay()) {
				ci.cancel();
			}
		}
    }
}
