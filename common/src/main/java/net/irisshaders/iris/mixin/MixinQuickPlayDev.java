package net.irisshaders.iris.mixin;

import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.quickplay.QuickPlay;
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
				//minecraft.createWorldOpenFlows().createFreshLevel(string, new LevelSettings(string, GameType.CREATIVE, false, Difficulty.HARD, true, new GameRules(FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS, FeatureFlags.REDSTONE_EXPERIMENTS)), WorldDataConfiguration.DEFAULT),
				//	WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions, Minecraft.getInstance().gui.screen());
			} else {
				minecraft.createWorldOpenFlows().openWorld(string, () -> minecraft.gui.setScreen(new TitleScreen()));
			}
		}
	}
}
