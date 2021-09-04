package net.coderbot.iris.gui.option;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShadowDistanceOption extends DoubleOption {
	private final Function<MinecraftClient, List<OrderedText>> shadowTooltipsGetter;

	public ShadowDistanceOption(String key, double min, double max, float step, Function<GameOptions, Double> getter,
								BiConsumer<GameOptions, Double> setter,
								BiFunction<GameOptions, DoubleOption, Text> displayStringGetter,
								Function<MinecraftClient, List<OrderedText>> tooltipsGetter) {
		super(key, min, max, step, getter, setter, displayStringGetter, tooltipsGetter);

		shadowTooltipsGetter = tooltipsGetter;
	}

	@Override
	public ClickableWidget createButton(GameOptions options, int x, int y, int width) {
		List<OrderedText> list = shadowTooltipsGetter.apply(MinecraftClient.getInstance());
		ClickableWidget widget = new ShadowDistanceSliderWidget(options, x, y, width, 20, this, list);

		widget.active = IrisVideoSettings.isShadowDistanceSliderEnabled();

		return widget;
	}
}
