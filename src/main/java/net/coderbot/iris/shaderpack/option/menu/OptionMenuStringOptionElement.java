package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.SliderElementWidget;
import net.coderbot.iris.gui.element.widget.StringElementWidget;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.OptionValues;
import net.coderbot.iris.shaderpack.option.StringOption;

public class OptionMenuStringOptionElement extends OptionMenuOptionElement {
	private final StringOption option;
	private final boolean slider;

	public OptionMenuStringOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, StringOption option, boolean slider) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
		this.slider = slider;
	}

	@Override
	public AbstractElementWidget createWidget(NavigationController navigation) {
		return slider ?
				new SliderElementWidget(option, this.values.getStringValue(this.optionId).orElse(option.getDefaultValue()))
				: new StringElementWidget(option, this.values.getStringValue(this.optionId).orElse(option.getDefaultValue()));
	}
}
