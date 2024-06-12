package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.BiomeAmbienceInterface;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeAmbientSoundsHandler.class)
public class MixinBiomeAmbientSoundsHandler implements BiomeAmbienceInterface {
	@Shadow
	@Final
	private LocalPlayer player;

	@Unique
	private float constantMoodiness;

	@Inject(method = "method_26271", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBrightness(Lnet/minecraft/world/level/LightLayer;Lnet/minecraft/core/BlockPos;)I", ordinal = 0))
	private void calculateConstantMoodiness(AmbientMoodSettings ambientMoodSettings, CallbackInfo ci, @Local BlockPos blockPos) {
		int j = this.player.level().getBrightness(LightLayer.SKY, blockPos);
		if (j > 0) {
			this.constantMoodiness -= (float)j / (float)this.player.level().getMaxLightLevel() * 0.001F;
		} else {
			this.constantMoodiness -= (float)(this.player.level().getBrightness(LightLayer.BLOCK, blockPos) - 1) / (float)ambientMoodSettings.getTickDelay();
		}

		this.constantMoodiness = Mth.clamp(constantMoodiness, 0.0f, 1.0f);
	}

	@Override
	public float getConstantMood() {
		return constantMoodiness;
	}
}
