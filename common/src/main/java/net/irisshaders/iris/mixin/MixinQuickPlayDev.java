package net.irisshaders.iris.mixin;

import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuickPlay.class)
public class MixinQuickPlayDev {
	@Inject(method = "joinSingleplayerWorld", at = @At("HEAD"), cancellable = true)
	private static void iris$createWorldIfDev(Minecraft minecraft, String string, CallbackInfo ci) {
		if (IrisPlatformHelpers.getInstance().isDevelopmentEnvironment()) {
			ci.cancel();

			if (!minecraft.getLevelSource().levelExists(string)) {
				minecraft.createWorldOpenFlows().createFreshLevel(string, new LevelSettings(string, GameType.CREATIVE, false, Difficulty.HARD, true, new GameRules(FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS, FeatureFlags.REDSTONE_EXPERIMENTS)), WorldDataConfiguration.DEFAULT),
					WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions, Minecraft.getInstance().screen);
			} else {
				minecraft.createWorldOpenFlows().openWorld(string, () -> minecraft.setScreen(new TitleScreen()));
			}
		}
	}
}
