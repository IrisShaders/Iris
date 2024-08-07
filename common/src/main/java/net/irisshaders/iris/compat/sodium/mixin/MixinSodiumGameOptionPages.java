package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.Option;
import net.caffeinemc.mods.sodium.client.gui.options.OptionFlag;
import net.caffeinemc.mods.sodium.client.gui.options.OptionGroup;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpact;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.caffeinemc.mods.sodium.client.gui.options.control.SliderControl;
import net.caffeinemc.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.fantastic.SupportedGraphicsMode;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.io.IOException;

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
			target = "net/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder.add (" +
				"Lnet/caffeinemc/mods/sodium/client/gui/options/Option;" +
				")Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;"),
		allow = 1)
	private static OptionGroup.Builder iris$addMaxShadowDistanceOption(OptionGroup.Builder builder,
																	   Option<?> candidate) {
		builder.add(candidate);
		builder.add(createMaxShadowDistanceSlider(vanillaOpts));

		return builder;
	}

	@Redirect(method = "quality", remap = false,
		slice = @Slice(
			from = @At(value = "CONSTANT", args = "stringValue=options.graphics"),
			to = @At(value = "CONSTANT", args = "stringValue=options.renderClouds")
		),
		at = @At(value = "INVOKE", remap = false,
			target = "net/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder.add (" +
				"Lnet/caffeinemc/mods/sodium/client/gui/options/Option;" +
				")Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;"),
		allow = 1)
	private static OptionGroup.Builder iris$addColorSpaceOption(OptionGroup.Builder builder,
																Option<?> candidate) {
		builder.add(candidate);
		builder.add(createColorSpaceButton(vanillaOpts));

		return builder;
	}

	@ModifyArg(method = "quality", remap = false,
		slice = @Slice(
			from = @At(value = "CONSTANT", args = "stringValue=options.graphics"),
			to = @At(value = "CONSTANT", args = "stringValue=options.renderClouds")
		),
		at = @At(value = "INVOKE", remap = false,
			target = "net/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder.add (" +
				"Lnet/caffeinemc/mods/sodium/client/gui/options/Option;" +
				")Lnet/caffeinemc/mods/sodium/client/gui/options/OptionGroup$Builder;"),
		allow = 1)
	private static Option<?> iris$replaceGraphicsQualityButton(Option<?> candidate) {
		if (!Iris.getIrisConfig().areShadersEnabled()) {
			return candidate;
		} else {
			return createLimitedVideoSettingsButton(vanillaOpts);
		}
	}

	private static OptionImpl<Options, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<Options, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
			.setName(Component.translatable("options.iris.shadowDistance"))
			.setTooltip(Component.translatable("options.iris.shadowDistance.sodium_tooltip"))
			.setControl(option -> new SliderControl(option, 0, 32, 1, translateVariableOrDisabled("options.chunks", "Disabled")))
			.setBinding((options, value) -> {
					IrisVideoSettings.shadowDistance = value;
					try {
						Iris.getIrisConfig().save();
					} catch (IOException e) {
						Iris.logger.error("Failed to save Iris config!", e);
					}
				},
				options -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
			.setImpact(OptionImpact.HIGH)
			.setEnabled(IrisVideoSettings::isShadowDistanceSliderEnabled)
			.build();

		return maxShadowDistanceSlider;
	}

	private static OptionImpl<Options, ColorSpace> createColorSpaceButton(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<Options, ColorSpace> colorSpace = OptionImpl.createBuilder(ColorSpace.class, vanillaOpts)
			.setName(Component.translatable("options.iris.colorSpace"))
			.setTooltip(Component.translatable("options.iris.colorSpace.sodium_tooltip"))
			.setControl(option -> new CyclingControl<>(option, ColorSpace.class,
				new Component[]{Component.literal("sRGB"), Component.literal("DCI_P3"), Component.literal("Display P3"), Component.literal("REC2020"), Component.literal("Adobe RGB")}))
			.setBinding((options, value) -> {
					IrisVideoSettings.colorSpace = value;
					try {
						Iris.getIrisConfig().save();
					} catch (IOException e) {
						Iris.logger.error("Failed to save Iris config!", e);
					}
				},
				options -> IrisVideoSettings.colorSpace)
			.setImpact(OptionImpact.LOW)
			.setEnabled(() -> true)
			.build();


		return colorSpace;
	}

	private static ControlValueFormatter translateVariableOrDisabled(String key, String disabled) {
		return (v) -> v == 0 ? Component.literal(disabled) : (Component.translatable(key, v));
	}

	private static OptionImpl<Options, SupportedGraphicsMode> createLimitedVideoSettingsButton(MinecraftOptionsStorage vanillaOpts) {
		return OptionImpl.createBuilder(SupportedGraphicsMode.class, vanillaOpts)
			.setName(Component.translatable("options.graphics"))
			// TODO: State that Fabulous Graphics is incompatible with Shader Packs in the tooltip
			.setTooltip(Component.translatable("sodium.options.graphics_quality.tooltip"))
			.setControl(option -> new CyclingControl<>(option, SupportedGraphicsMode.class,
				new Component[]{Component.translatable("options.graphics.fast"), Component.translatable("options.graphics.fancy")}))
			.setBinding(
				(opts, value) -> opts.graphicsMode().set(value.toVanilla()),
				opts -> SupportedGraphicsMode.fromVanilla(opts.graphicsMode().get()))
			.setImpact(OptionImpact.HIGH)
			.setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
			.build();
	}
}
