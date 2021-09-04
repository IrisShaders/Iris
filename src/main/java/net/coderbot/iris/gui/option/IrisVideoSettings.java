package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.IOException;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	// TODO: Tell the user to check in the shader options once that's supported.
	private static final Text DISABLED_TOOLTIP = new TranslatableText("options.iris.shadowDistance.disabled");
	private static final Text ENABLED_TOOLTIP = new TranslatableText("options.iris.shadowDistance.enabled");

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
	public static final DoubleOption RENDER_DISTANCE = new ShadowDistanceOption("options.iris.shadowDistance", 0.0D, 32.0D, 1.0F, (gameOptions) -> {
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

		if (pipeline != null) {
			d = pipeline.getForcedShadowRenderDistanceChunksForDisplay().orElse(d);
		}

		if (d <= 0.0) {
			return new TranslatableText("options.generic_value", new TranslatableText("options.iris.shadowDistance"), "0 (disabled)");
		} else {
			return new TranslatableText("options.generic_value",
					new TranslatableText("options.iris.shadowDistance"),
					new TranslatableText("options.chunks", d));
		}
	}, client -> {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipeline();

		Text tooltip;

		if (pipeline != null) {
			if (pipeline.getForcedShadowRenderDistanceChunksForDisplay().isPresent()) {
				tooltip = DISABLED_TOOLTIP;
			} else {
				tooltip = ENABLED_TOOLTIP;
			}
		} else {
			tooltip = ENABLED_TOOLTIP;
		}

		return MinecraftClient.getInstance().textRenderer.wrapLines(tooltip, 200);
	});
}
