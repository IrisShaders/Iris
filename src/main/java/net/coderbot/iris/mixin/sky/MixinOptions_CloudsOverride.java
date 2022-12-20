package net.coderbot.iris.mixin.sky;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.CloudSetting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows the current pipeline to override the cloud video mode setting.
 *
 * Uses a priority of 1010 to apply after Sodium's MixinGameOptions, which overwrites getCloudsType, so that we can
 * override its behavior.
 */
@Mixin(value = Options.class, priority = 1010)
public class MixinOptions_CloudsOverride {
	@Shadow
	public int renderDistance;

	@Inject(method = "getCloudsType", at = @At("HEAD"), cancellable = true)
	private void iris$overrideCloudsType(CallbackInfoReturnable<CloudStatus> cir) {
		// Vanilla does not render clouds on low render distances, we have to mirror that check
		// when injecting at the head.
		if (renderDistance < 4) {
			return;
		}

		Iris.getPipelineManager().getPipeline().ifPresent(p -> {
			CloudSetting setting = p.getCloudSetting();

			switch (setting) {
				case OFF:
					cir.setReturnValue(CloudStatus.OFF);
					return;
				case FAST:
					cir.setReturnValue(CloudStatus.FAST);
					return;
				case FANCY:
					cir.setReturnValue(CloudStatus.FANCY);
			}
		});
	}
}
