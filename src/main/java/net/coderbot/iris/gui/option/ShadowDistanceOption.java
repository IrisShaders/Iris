package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShadowDistanceOption extends DoubleOption {
	public ShadowDistanceOption(String key, double min, double max, float step, Function<GameOptions, Double> getter, BiConsumer<GameOptions, Double> setter, BiFunction<GameOptions, DoubleOption, Text> displayStringGetter) {
		super(key, min, max, step, getter, setter, displayStringGetter);
	}

	@Override
	public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
		AbstractButtonWidget widget = new ShadowDistanceSliderWidget(options, x, y, width, 20, this);

		widget.active = IrisVideoSettings.isShadowDistanceSliderEnabled();

		return widget;
	}
}
