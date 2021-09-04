package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.text.TranslatableText;

import java.io.IOException;

public class IrisVideoSettings {
	public static int shadowDistance = 32;

	// TODO: Grey out button when the shader pack is overriding the shadow distance
	// TODO: Save this to a file
	// TODO: Add a Sodium video settings button too.
	public static final DoubleOption RENDER_DISTANCE = new DoubleOption("options.iris.shadowDistance", 0.0D, 32.0D, 1.0F, (gameOptions) -> {
		return (double) shadowDistance;
	}, (gameOptions, viewDistance) -> {
		double outputShadowDistance = viewDistance;
		shadowDistance = (int) outputShadowDistance;
		try {
			Iris.getIrisConfig().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}, (gameOptions, option) -> {
		double d = option.get(gameOptions);

		if (d <= 0.0) {
			return new TranslatableText("options.generic_value", new TranslatableText("options.iris.shadowDistance"), "0 (disabled)");
		} else {
			return new TranslatableText("options.generic_value",
					new TranslatableText("options.iris.shadowDistance"),
					new TranslatableText("options.chunks", (int) shadowDistance));
		}
	});
}
