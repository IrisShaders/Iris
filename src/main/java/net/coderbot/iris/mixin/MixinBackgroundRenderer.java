package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class MixinBackgroundRenderer {
	@Shadow private static float red, green, blue;
	@Inject(method = "render", at = @At("TAIL"))
	private static void render(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setFogColor(red, green, blue);
	}
}
