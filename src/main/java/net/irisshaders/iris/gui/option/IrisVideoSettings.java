package net.irisshaders.iris.gui.option;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.colorspace.ColorSpace;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class IrisVideoSettings {
	private static final Tooltip DISABLED_TOOLTIP = Tooltip.create(Component.translatable("options.iris.shadowDistance.disabled"));
	private static final Tooltip ENABLED_TOOLTIP = Tooltip.create(Component.translatable("options.iris.shadowDistance.enabled"));
	public static int shadowDistance = 32;
	public static ColorSpace colorSpace = ColorSpace.SRGB;
	public static final OptionInstance<Integer> RENDER_DISTANCE = new ShadowDistanceOption<>("options.iris.shadowDistance",
		mc -> {
			WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

			Tooltip tooltip;

			if (pipeline != null) {
				if (pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent()) {
					tooltip = DISABLED_TOOLTIP;
				} else {
					tooltip = ENABLED_TOOLTIP;
				}
			} else {
				tooltip = ENABLED_TOOLTIP;
			}

			return tooltip;
		},
		(arg, d) -> {
			WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

			if (pipeline != null) {
				d = pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(d);
			}

			if (d <= 0.0) {
				return Component.translatable("options.generic_value", Component.translatable("options.iris.shadowDistance"), "0 (disabled)");
			} else {
				return Component.translatable("options.generic_value",
					Component.translatable("options.iris.shadowDistance"),
					Component.translatable("options.chunks", d));
			}
		},
		new OptionInstance.IntRange(0, 32),
		getOverriddenShadowDistance(shadowDistance),
		integer -> {
			shadowDistance = integer;
			try {
				Iris.getIrisConfig().save();
			} catch (IOException e) {
				Iris.logger.fatal("Failed to save config!", e);
			}
		});

	public static int getOverriddenShadowDistance(int base) {
		return Iris.getPipelineManager().getPipeline()
			.map(pipeline -> pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(base))
			.orElse(base);
	}

	public static boolean isShadowDistanceSliderEnabled() {
		return Iris.getPipelineManager().getPipeline()
			.map(pipeline -> !pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent())
			.orElse(true);
	}
}
