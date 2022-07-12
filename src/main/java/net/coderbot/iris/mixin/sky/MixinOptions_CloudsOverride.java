package net.coderbot.iris.mixin.sky;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.CloudSetting;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows the current pipeline to override the cloud video mode setting.
 */
@Mixin(Options.class)
public class MixinOptions_CloudsOverride {
	@Inject(method = "getCloudsType",
		at = @At(value = "FIELD",
			target = "net/minecraft/client/Options.renderClouds : Lnet/minecraft/client/CloudStatus;"))
	private void iris$overrideCloudsType(CallbackInfoReturnable<CloudStatus> cir) {
		Iris.getPipelineManager().getPipeline().ifPresent(p -> {
			CloudSetting setting = p.getCloudSetting();

			switch (setting) {
				case OFF:
					cir.setReturnValue(CloudStatus.OFF);
				case FAST:
					cir.setReturnValue(CloudStatus.FAST);
				case FANCY:
					cir.setReturnValue(CloudStatus.FANCY);
			}
		});
	}
}
