package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinBackgroundRenderer {
	@Shadow private static float fogRed, fogGreen, fogBlue;
	@Inject(method = "setupColor", at = @At("TAIL"))
	private static void render(Camera camera, float tickDelta, ClientLevel world, int i, float f, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setFogColor(fogRed, fogGreen, fogBlue);
	}
}
