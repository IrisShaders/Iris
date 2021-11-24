package net.coderbot.iris.compat.sodium.mixin.options;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.Option;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.options.IrisSodiumOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

// TODO: Port this Mixin

/**
 * Adds the Iris-specific options / option changes to the Sodium game options pages.
 */
@Mixin(SodiumGameOptionPages.class)
public class MixinSodiumGameOptionPages {
    @Shadow(remap = false)
    @Final
    private static MinecraftOptionsStorage vanillaOpts;

    @Redirect(method = "general", remap = false,
            slice = @Slice(
                    from = @At(value = "CONSTANT", args = "stringValue=options.renderDistance"),
                    to = @At(value = "CONSTANT", args = "stringValue=options.simulationDistance")
            ),
            at = @At(value = "INVOKE", remap = false,
                    target = "me/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder.add (" +
                            "Lme/jellysquid/mods/sodium/client/gui/options/Option;" +
                            ")Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;"),
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
                    target = "me/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder.add (" +
                                "Lme/jellysquid/mods/sodium/client/gui/options/Option;" +
                            ")Lme/jellysquid/mods/sodium/client/gui/options/OptionGroup$Builder;"),
            allow = 1)
    private static Option<?> iris$replaceGraphicsQualityButton(Option<?> candidate) {
        if (!Iris.getIrisConfig().areShadersEnabled()) {
            return candidate;
        } else {
            return IrisSodiumOptions.createLimitedVideoSettingsButton(vanillaOpts);
        }
    }
}
