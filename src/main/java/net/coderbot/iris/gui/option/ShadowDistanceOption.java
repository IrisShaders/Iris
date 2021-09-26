package net.coderbot.iris.gui.option;

import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ShadowDistanceOption extends ProgressOption {

	public ShadowDistanceOption(String string, double d, double e, float f, Function<Options, Double> function, BiConsumer<Options, Double> biConsumer, BiFunction<Options, ProgressOption, Component> biFunction) {
		super(string, d, e, f, function, biConsumer, biFunction);
	}

	@Override
	public AbstractWidget createButton(Options options, int x, int y, int width) {
		AbstractWidget widget = new ShadowDistanceSliderButton(options, x, y, width, 20, this);

		widget.active = IrisVideoSettings.isShadowDistanceSliderEnabled();

		return widget;
	}
}
