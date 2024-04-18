package net.irisshaders.iris.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
@Environment(EnvType.CLIENT)
public class MixinMinecraft_PipelineManagement {
	/**
	 * Should run before the Minecraft.level field is updated after disconnecting from a server or leaving a singleplayer world
	 */
	@Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
	public void iris$trackLastDimensionOnLeave(Screen arg, CallbackInfo ci) {
		Iris.lastDimension = Iris.getCurrentDimension();
	}

	/**
	 * Should run before the Minecraft.level field is updated after receiving a login or respawn packet
	 * NB: Not on leave, another inject is used for that
	 */
	@Inject(method = "setLevel", at = @At("HEAD"))
	private void iris$trackLastDimensionOnLevelChange(@Nullable ClientLevel level, CallbackInfo ci) {
		Iris.lastDimension = Iris.getCurrentDimension();
	}

	/**
	 * Injects before LevelRenderer receives the new level, or is notified of the level unload.
	 * <p>
	 * We destroy any pipelines here to guard against potential memory leaks related to pipelines for
	 * other dimensions never being unloaded.
	 * <p>
	 * This injection point is needed so that we can reload the Iris shader pipeline before Sodium starts trying
	 * to reload its world renderer. Otherwise, there will be inconsistent state since Sodium might initialize and
	 * use the non-extended vertex format (since we do it based on whether the pipeline is available,
	 * then Iris will switch on its pipeline, then code will assume that the extended vertex format
	 * is used everywhere.
	 * <p>
	 * See: <a href="https://github.com/IrisShaders/Iris/issues/1330">Issue 1330</a>
	 */
	@Inject(method = "updateLevelInEngines", at = @At("HEAD"))
	private void iris$resetPipeline(@Nullable ClientLevel level, CallbackInfo ci) {
		if (Iris.getCurrentDimension() != Iris.lastDimension) {
			Iris.logger.info("Reloading pipeline on dimension change: " + Iris.lastDimension + " => " + Iris.getCurrentDimension());
			// Destroy pipelines when changing dimensions.
			Iris.getPipelineManager().destroyPipeline();

			// NB: We need create the pipeline immediately, so that it is ready by the time that Sodium starts trying to
			// initialize its world renderer.
			if (level != null) {
				Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
			}
		}
	}
}
