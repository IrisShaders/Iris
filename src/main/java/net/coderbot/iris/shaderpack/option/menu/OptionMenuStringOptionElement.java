package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.SliderElementWidget;
import net.coderbot.iris.gui.element.widget.StringElementWidget;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.OptionValues;
import net.coderbot.iris.shaderpack.option.StringOption;

public class OptionMenuStringOptionElement extends OptionMenuOptionElement {
	private final StringOption option;

	public OptionMenuStringOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, StringOption option) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
	}

	@Override
	public AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation) {
		return this.slider ?
				new SliderElementWidget(screen, option, this.values.getStringValue(this.optionId).orElse(option.getDefaultValue()))
				: new StringElementWidget(screen, option, this.values.getStringValue(this.optionId).orElse(option.getDefaultValue()));
	}
}
