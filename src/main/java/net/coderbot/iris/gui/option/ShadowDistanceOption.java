package net.coderbot.iris.gui.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShadowDistanceOption extends ProgressOption {
	private final Function<Minecraft, List<FormattedCharSequence>> shadowTooltipsGetter;

	public ShadowDistanceOption(String key, double min, double max, float step, Function<Options, Double> getter,
								BiConsumer<Options, Double> setter,
								BiFunction<Options, ProgressOption, Component> displayStringGetter,
								Function<Minecraft, List<FormattedCharSequence>> tooltipsGetter) {
		super(key, min, max, step, getter, setter, displayStringGetter, tooltipsGetter);

		shadowTooltipsGetter = tooltipsGetter;
	}

	@Override
	public AbstractWidget createButton(Options options, int x, int y, int width) {
		List<FormattedCharSequence> list = shadowTooltipsGetter.apply(Minecraft.getInstance());
		AbstractWidget widget = new ShadowDistanceSliderButton(options, x, y, width, 20, this, list);

		widget.active = IrisVideoSettings.isShadowDistanceSliderEnabled();

		return widget;
	}
}
