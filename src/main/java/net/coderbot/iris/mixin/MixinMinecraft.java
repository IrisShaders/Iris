package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Small hook giving Iris a chance to check for keyboard input for its keybindings.
 *
 * <p>This is equivalent to the END_CLIENT_TICK event in Fabric API, but since it's a super simple mixin and we
 * only need this event (out of the many events provided by Fabric API) I've just implemented it myself. This
 * alone shaves over 60kB off the released JAR size.</p>
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private ProfilerFiller profiler;

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void iris$onTick(CallbackInfo ci) {
		this.profiler.push("iris_keybinds");

		Iris.handleKeybinds((Minecraft) (Object) this);

		this.profiler.pop();
	}

	/**
	 * Injects before LevelRenderer receives the new level, or is notified of the level unload.
	 *
	 * We destroy any pipelines here to guard against potential memory leaks related to pipelines for
	 * other dimensions never being unloaded.
	 *
	 * This injection point is needed so that we can reload the Iris shader pipeline before Sodium starts trying
	 * to reload its world renderer. Otherwise, there will be inconsistent state since Sodium might initialize and
	 * use the non-extended vertex format (since we do it based on whether the pipeline is available,
	 * then Iris will switch on its pipeline, then code will assume that the extended vertex format
	 * is used everywhere.
	 *
	 * See: https://github.com/IrisShaders/Iris/issues/1330
	 */
	@Inject(method = "updateLevelInEngines", at = @At("HEAD"))
	private void iris$resetPipeline(@Nullable ClientLevel level, CallbackInfo ci) {
		// Destroy pipelines when changing dimensions.
		Iris.getPipelineManager().destroyPipeline();

		// NB: We need create the pipeline immediately, so that it is ready by the time that Sodium starts trying to
		// initialize its world renderer.
		if (level != null) {
			Iris.getPipelineManager().preparePipeline(Iris.getCurrentDimension());
		}
	}
}
