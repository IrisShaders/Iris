package net.coderbot.iris.compat.sodium.impl.options;

import net.caffeinemc.sodium.config.user.options.OptionFlag;
import net.caffeinemc.sodium.config.user.options.OptionImpact;
import net.caffeinemc.sodium.config.user.options.OptionImpl;
import net.caffeinemc.sodium.gui.config.ControlValueFormatter;
import net.caffeinemc.sodium.gui.config.CyclingControl;
import net.caffeinemc.sodium.gui.config.SliderControl;
import net.caffeinemc.sodium.interop.vanilla.options.MinecraftOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class IrisSodiumOptions {
    public static OptionImpl<Options, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
        OptionImpl<Options, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
                .setName(new TranslatableComponent("options.iris.shadowDistance"))
                .setTooltip(new TranslatableComponent("options.iris.shadowDistance.sodium_tooltip"))
                .setControl(option -> new SliderControl(option, 0, 32, 1, ControlValueFormatter.quantityOrDisabled("Chunks", "Disabled")))
				.setBinding((options, value) -> {
						IrisVideoSettings.shadowDistance = value;
						try {
							Iris.getIrisConfig().save();
						} catch (IOException e) {
							e.printStackTrace();
						}
					},
					options -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
                .setImpact(OptionImpact.HIGH)
                .setEnabled(true)
                .build();

        ((OptionImplExtended) maxShadowDistanceSlider).iris$dynamicallyEnable(IrisVideoSettings::isShadowDistanceSliderEnabled);

        return maxShadowDistanceSlider;
    }

    public static OptionImpl<Options, SupportedGraphicsMode> createLimitedVideoSettingsButton(MinecraftOptionsStorage vanillaOpts) {
        return OptionImpl.createBuilder(SupportedGraphicsMode.class, vanillaOpts)
                .setName(new TranslatableComponent("options.graphics"))
				// TODO: State that Fabulous Graphics is incompatible with Shader Packs in the tooltip
                .setTooltip(new TranslatableComponent("sodium.options.graphics_quality.tooltip"))
                .setControl(option -> new CyclingControl<>(option, SupportedGraphicsMode.class,
						new Component[] { new TextComponent("Fast"), new TextComponent("Fancy") }))
                .setBinding(
                        (opts, value) -> opts.graphicsMode = value.toVanilla(),
                        opts -> SupportedGraphicsMode.fromVanilla(opts.graphicsMode))
                .setImpact(OptionImpact.HIGH)
                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                .build();
    }
}
