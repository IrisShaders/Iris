package net.coderbot.iris.gui.option;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import java.util.function.Consumer;

public class ShadowDistanceOption<T> extends OptionInstance<T> {
	private final TooltipSupplierFactory<T> tooltipSupplier;

	public ShadowDistanceOption(String string, TooltipSupplierFactory<T> arg, CaptionBasedToString<T> arg2, OptionInstance.ValueSet<T> arg3, T object, Consumer<T> consumer) {
		super(string, arg, arg2, arg3, object, consumer);

		this.tooltipSupplier = arg;
	}

	@Override
	public AbstractWidget createButton(Options options, int x, int y, int width) {
		TooltipSupplier<T> lv = this.tooltipSupplier.apply(Minecraft.getInstance());
		AbstractWidget widget = this.values().createButton(lv, options, x, y, width).apply(this);

		widget.active = IrisVideoSettings.isShadowDistanceSliderEnabled();

		return widget;
	}
}
