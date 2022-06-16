package net.coderbot.iris.compat.sodium.mixin.options;

import net.caffeinemc.sodium.config.user.UserConfig;
import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

/**
 * Ensures that the Iris config file is written whenever Sodium options are changed, in case the user changed the
 * Max Shadow Distance setting.
 */
@Mixin(UserConfig.class)
public class MixinUserConfig {
    @Inject(method = "writeChanges()V", at = @At("RETURN"), remap = false)
    public void iris$writeIrisConfig(CallbackInfo ci) {
        try {
        	if (Iris.getIrisConfig() != null) {
				Iris.getIrisConfig().save();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
