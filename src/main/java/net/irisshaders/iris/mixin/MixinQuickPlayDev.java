package net.irisshaders.iris.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.world.Difficulty;
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
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			ci.cancel();

			if (!minecraft.getLevelSource().levelExists(string)) {
				minecraft.createWorldOpenFlows().createFreshLevel(string, new LevelSettings(string, GameType.CREATIVE, false, Difficulty.HARD, true, new GameRules(), WorldDataConfiguration.DEFAULT),
					WorldOptions.defaultWithRandomSeed(), WorldPresets::createNormalWorldDimensions, Minecraft.getInstance().screen);
			} else {
				minecraft.createWorldOpenFlows().checkForBackupAndLoad(string, () -> minecraft.setScreen(new TitleScreen()));
			}
		}
	}
}
