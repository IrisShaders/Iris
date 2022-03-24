package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;

import java.io.IOException;
import java.util.List;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	// TODO: Tell the user to check in the shader options once that's supported.
	private static final Component DISABLED_TOOLTIP = new TranslatableComponent("options.iris.shadowDistance.disabled");
	private static final Component ENABLED_TOOLTIP = new TranslatableComponent("options.iris.shadowDistance.enabled");

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
