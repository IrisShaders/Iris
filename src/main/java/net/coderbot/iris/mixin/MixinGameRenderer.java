package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.SystemTimeUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	@Inject(method = "render(FJZ)V", at = @At("HEAD"))
	private void iris$beginFrame(float tickDelta, long startTime, boolean tick, CallbackInfo callback) {
		SystemTimeUniforms.TIMER.beginFrame(startTime);
	}
}
