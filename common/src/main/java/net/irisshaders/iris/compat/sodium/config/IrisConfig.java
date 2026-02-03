package net.irisshaders.iris.compat.sodium.config;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigState;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.option.Range;
import net.caffeinemc.mods.sodium.api.config.structure.ColorThemeBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.PageBuilder;
import net.caffeinemc.mods.sodium.client.config.builder.ColorThemeBuilderImpl;
import net.caffeinemc.mods.sodium.client.gui.ColorTheme;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatterImpls;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gui.option.IrisVideoSettings;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TextureFilteringMethod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

public class IrisConfig implements ConfigEntryPoint {
	public static final Identifier MONO = Identifier.fromNamespaceAndPath("iris", "textures/gui/config-icon-mono.png");
	public static final Identifier COLOR = Identifier.fromNamespaceAndPath("iris", "textures/gui/config-icon.png");
	@Override
	public void registerConfigLate(ConfigBuilder builder) {
		builder.registerOwnModOptions().setName("Iris").setIcon(MONO).setColorTheme(builder.createColorTheme().setBaseThemeRGB(0xFFf556e2))
			.setVersion(Iris.getVersionSimple())
			.addPage(builder.createExternalPage().setName(Component.translatable("options.iris.shaderPackSelection.title")).setScreenConsumer(i -> Minecraft.getInstance().setScreen(new ShaderPackScreen(i))))
			.addPage(builder.createOptionPage().setName(Component.literal("Settings")).addOptionGroup(builder.createOptionGroup().addOption(builder.createExternalButtonOption(Identifier.fromNamespaceAndPath("iris", "settings")).setTooltip(Component.literal("Packs")).setName(Component.translatable("options.iris.shaderPackList"))
				.setScreenConsumer(i -> Minecraft.getInstance().setScreen(new ShaderPackScreen(i)))))
				.addOptionGroup(builder.createOptionGroup().addOption(builder.createEnumOption(Identifier.fromNamespaceAndPath("iris", "colorSpace"), ColorSpace.class)
					.setBinding(i -> {
						IrisVideoSettings.colorSpace = i;
					}, () -> IrisVideoSettings.colorSpace)
					.setName(Component.translatable("options.iris.colorSpace"))
						.setDefaultValue(ColorSpace.SRGB)
					.setTooltip(Component.translatable("options.iris.colorSpace.sodium_tooltip"))
						.setStorageHandler(() -> {
							try {
								Iris.getIrisConfig().save();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						})
					.setElementNameProvider(ColorSpace::getName))
					.addOption(builder.createIntegerOption(Identifier.fromNamespaceAndPath("iris", "shadowDistance"))
						.setDefaultValue(32)
						.setBinding(value -> IrisVideoSettings.shadowDistance = value, () -> IrisVideoSettings.getOverriddenShadowDistance(IrisVideoSettings.shadowDistance))
						.setName(Component.translatable("options.iris.shadowDistance"))
						.setTooltip(i -> {
							if (!IrisVideoSettings.isShadowDistanceSliderEnabled()) {
								return Component.translatable("options.iris.shadowDistance.disabled");
							} else {
								return Component.translatable("options.iris.shadowDistance.sodium_tooltip");
							}
						})
						.setValueFormatter(ControlValueFormatterImpls.quantityOrDisabled(i -> Component.translatable("options.chunks", i), Component.literal("None")))
						.setEnabledProvider(i -> IrisVideoSettings.isShadowDistanceSliderEnabled(), ConfigState.UPDATE_ON_REBUILD)
						.setStorageHandler(() -> {
							try {
								Iris.getIrisConfig().save();
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						})
						.setRange(new Range(0, 32, 1))
						.setImpact(OptionImpact.HIGH)
					)
				)
			).registerOptionOverlay(Identifier.parse("sodium:quality.filtering_mode"), builder.createEnumOption(Identifier.parse("sodium:quality.filtering_mode"), TextureFilteringMethod.class)
				.setTooltip(i -> {
					if (i == TextureFilteringMethod.RGSS) {
						return Component.translatable("options.textureFiltering." + i.name().toLowerCase(Locale.ROOT) + ".tooltip").append(Component.literal(" (RGSS is not usable with shaders on.)"));
					} else {
						return Component.translatable("options.textureFiltering." + i.name().toLowerCase(Locale.ROOT) + ".tooltip");
					}
				})
				.setAllowedValuesProvider(state -> {
					if (Iris.getCurrentPack().isPresent()) {
						return Set.of(TextureFilteringMethod.NONE, TextureFilteringMethod.ANISOTROPIC);
					} else {
						return Set.of(TextureFilteringMethod.values());
					}
				}, ConfigState.UPDATE_ON_REBUILD)
			).registerOptionOverlay(Identifier.parse("sodium:quality.graphics"), builder.createBooleanOption(Identifier.parse("sodium:quality.graphics"))
				.setTooltip(i -> {
					if (Iris.getCurrentPack().isPresent()) {
						return Component.literal("This option is not relevant when a shader pack is active.");
					} else {
						return Component.translatable("options.improvedTransparency.tooltip");
					}
				})
				.setEnabledProvider(i -> {
					return Iris.getCurrentPack().isEmpty();
				}, ConfigState.UPDATE_ON_REBUILD)
			);//.registerOptionOverlay(Identifier.parse("sodium:quality.anisotropy_bit"), builder.createIntegerOption(Identifier.parse("sodium:quality.anisotropy_bit")));
		;
	}
}
