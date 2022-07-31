package net.coderbot.iris.compat.sodium.impl.options;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.MinecraftOptionsStorage;
import me.jellysquid.mods.sodium.client.gui.options.storage.SodiumOptionsStorage;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.option.IrisVideoSettings;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import java.io.IOException;

public class IrisSodiumOptions {
	public static OptionImpl<Options, Integer> createMaxShadowDistanceSlider(MinecraftOptionsStorage vanillaOpts) {
		OptionImpl<Options, Integer> maxShadowDistanceSlider = OptionImpl.createBuilder(int.class, vanillaOpts)
				.setName("Max Shadow Distance")
				.setTooltip("The shadow render distance controls how far away terrain can potentially be rendered in the shadow pass. Lower distances mean that less terrain will be " +
						"rendered, improving frame rates. This option cannot be changed on packs which explicitly specify a shadow render distance. The actual shadow render distance is capped by the " +
						"View Distance setting.")
				.setControl(option -> new SliderControl(option, 0, 32, 1, ControlValueFormatter.quantity("Chunks")))
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
				.setName("Graphics Quality")
				.setTooltip("The default graphics quality controls some legacy options and is necessary for mod compatibility. If the options below are left to " +
						"\"Default\", they will use this setting. Fabulous graphics are blocked while shaders are enabled.")
				.setControl(option -> new CyclingControl<>(option, SupportedGraphicsMode.class, new String[] { "Fast", "Fancy" }))
				.setBinding(
						(opts, value) -> opts.graphicsMode = value.toVanilla(),
						opts -> SupportedGraphicsMode.fromVanilla(opts.graphicsMode))
				.setImpact(OptionImpact.HIGH)
				.setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
				.build();
	}

	public static OptionImpl<SodiumGameOptions, SodiumGameOptions.GraphicsQuality> createLimitedCloudQualityButton(SodiumOptionsStorage sodiumOpts) {
		CloudStatus status;
		if (sodiumOpts.getData().quality.cloudQuality == SodiumGameOptions.GraphicsQuality.DEFAULT) {
			status = CloudStatus.FANCY;
		} else {
			status = CloudStatus.valueOf(sodiumOpts.getData().quality.cloudQuality.name());
		}
		status = IrisVideoSettings.getOverridenCloudQuality(status);
		CloudStatus finalStatus = status;
		SodiumGameOptions.GraphicsQuality quality = status == CloudStatus.OFF ? SodiumGameOptions.GraphicsQuality.FANCY : SodiumGameOptions.GraphicsQuality.valueOf(finalStatus.name());
		return OptionImpl.createBuilder(SodiumGameOptions.GraphicsQuality.class, sodiumOpts)
			.setName("Clouds Quality")
			.setTooltip("Controls the quality of rendered clouds in the sky. This setting is disabled while shaders are overriding it.")
			.setControl(option -> new CyclingControl<>(option, SodiumGameOptions.GraphicsQuality.class))
			.setEnabled(IrisVideoSettings.isCloudSettingEnabled())
			.setBinding((opts, value) -> opts.quality.cloudQuality = value, options -> quality)
			.setImpact(OptionImpact.LOW)
			.build();
	}

	public static OptionImpl<SodiumGameOptions, Boolean> createLimitedCloudEnableButton(SodiumOptionsStorage sodiumOpts) {
		return OptionImpl.createBuilder(boolean.class, sodiumOpts)
			.setName("Clouds")
			.setTooltip("Controls whether or not clouds will be visible. This setting is disabled while shaders are overriding it.")
			.setControl(TickBoxControl::new)
			.setEnabled(!IrisVideoSettings.areCloudsModified())
			.setBinding((opts, value) -> opts.quality.enableClouds = value, (opts) -> IrisVideoSettings.areCloudsEnabled(opts.quality.enableClouds))
			.setImpact(OptionImpact.LOW)
			.build();
	}
}
