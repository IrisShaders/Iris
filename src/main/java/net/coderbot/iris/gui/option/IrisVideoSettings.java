package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ProgressOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.io.IOException;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	// TODO: Tell the user to check in the shader options once that's supported.
	private static final Component DISABLED_TOOLTIP = new TranslatableComponent("options.iris.shadowDistance.disabled");
	private static final Component ENABLED_TOOLTIP = new TranslatableComponent("options.iris.shadowDistance.enabled");

	public static int getOverriddenShadowDistance(int base) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		if (pipeline != null) {
			return pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(base);
		} else {
			return base;
		}
	}

	public static boolean isShadowDistanceSliderEnabled() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		return pipeline == null || !pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent();
	}

	// TODO: Add a Sodium video settings button too.
	public static final ProgressOption RENDER_DISTANCE = new ShadowDistanceOption("options.iris.shadowDistance", 0.0D, 32.0D, 1.0F, (gameOptions) -> {
		return (double) getOverriddenShadowDistance(shadowDistance);
	}, (gameOptions, viewDistance) -> {
		double outputShadowDistance = viewDistance;
		shadowDistance = (int) outputShadowDistance;
		try {
			Iris.getIrisConfig().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}, (gameOptions, option) -> {
		int d = (int) option.get(gameOptions);

		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		Component tooltip;

		if (pipeline != null) {
			d = pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(d);

			if (pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent()) {
				tooltip = DISABLED_TOOLTIP;
			} else {
				tooltip = ENABLED_TOOLTIP;
			}
		} else {
			tooltip = ENABLED_TOOLTIP;
		}

		option.setTooltip(Minecraft.getInstance().font.split(tooltip, 200));

		if (d <= 0.0) {
			return new TranslatableComponent("options.generic_value", new TranslatableComponent("options.iris.shadowDistance"), "0 (disabled)");
		} else {
			return new TranslatableComponent("options.generic_value",
					new TranslatableComponent("options.iris.shadowDistance"),
					new TranslatableComponent("options.chunks", d));
		}
	});
}
