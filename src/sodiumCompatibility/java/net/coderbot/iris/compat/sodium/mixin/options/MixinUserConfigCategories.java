package net.coderbot.iris.compat.sodium.mixin.options;

import net.caffeinemc.sodium.config.user.UserConfigCategories;
import net.caffeinemc.sodium.config.user.options.Option;
import net.caffeinemc.sodium.config.user.options.OptionGroup;
import net.caffeinemc.sodium.interop.vanilla.options.MinecraftOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.options.IrisSodiumOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * Adds the Iris-specific options / option changes to the Sodium game options pages.
 */
@Mixin(UserConfigCategories.class)
public class MixinUserConfigCategories {
    @Shadow(remap = false)
    @Final
    private static MinecraftOptionsStorage vanillaOpts;

    @Redirect(method = "general", remap = false,
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=options.renderDistance"),
                    to = @At(value = "CONSTANT", args = "stringValue=options.simulationDistance")
            ),
            at = @At(value = "INVOKE", remap = false,
                    target = "net/caffeinemc/sodium/config/user/options/OptionGroup$Builder.add (" +
							"Lnet/caffeinemc/sodium/config/user/options/Option;" +
							")Lnet/caffeinemc/sodium/config/user/options/OptionGroup$Builder;"),
            allow = 1)
    private static OptionGroup.Builder iris$addMaxShadowDistanceOption(OptionGroup.Builder builder,
																	   Option<?> candidate) {
        builder.add(candidate);
        builder.add(IrisSodiumOptions.createMaxShadowDistanceSlider(vanillaOpts));

        return builder;
    }

    @ModifyArg(method = "quality", remap = false,
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=options.graphics"),
                    to = @At(value = "CONSTANT", args = "stringValue=options.renderClouds")
            ),
            at = @At(value = "INVOKE", remap = false,
					target = "net/caffeinemc/sodium/config/user/options/OptionGroup$Builder.add (" +
							"Lnet/caffeinemc/sodium/config/user/options/Option;" +
							")Lnet/caffeinemc/sodium/config/user/options/OptionGroup$Builder;"),
            allow = 1)
    private static Option<?> iris$replaceGraphicsQualityButton(Option<?> candidate) {
        if (!Iris.getIrisConfig().areShadersEnabled()) {
            return candidate;
        } else {
            return IrisSodiumOptions.createLimitedVideoSettingsButton(vanillaOpts);
        }
    }
}
