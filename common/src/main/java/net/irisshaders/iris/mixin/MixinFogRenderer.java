package net.irisshaders.iris.mixin;

import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinFogRenderer {
	@Shadow
	private static float fogRed, fogGreen, fogBlue;

	@Inject(method = "setupFog", at = @At("HEAD"))
	private static void iris$setupLegacyWaterFog(Camera camera, FogRenderer.FogMode $$1, float $$2, boolean $$3, float $$4, CallbackInfo ci) {
		if (camera.getFluidInCamera() == FogType.WATER) {
			Entity entity = camera.getEntity();

			float density = 0.05F;

			if (entity instanceof LocalPlayer localPlayer) {
				density -= localPlayer.getWaterVision() * localPlayer.getWaterVision() * 0.03F;
				Holder<Biome> biome = localPlayer.level().getBiome(localPlayer.blockPosition());

				if (biome.is(BiomeTags.HAS_CLOSER_WATER_FOG)) {
					density += 0.005F;
				}
			}

			CapturedRenderingState.INSTANCE.setFogDensity(density);
		} else {
			CapturedRenderingState.INSTANCE.setFogDensity(-1.0F);
		}
	}

	@Inject(method = "setupColor", at = @At("TAIL"))
	private static void render(Camera camera, float tickDelta, ClientLevel level, int i, float f, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setFogColor(fogRed, fogGreen, fogBlue);
	}
}
