package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.AbstractShaderPackOptionWidget;
import net.coderbot.iris.gui.element.StringShaderPackOptionWidget;
import net.coderbot.iris.gui.NavigationController;
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
	public AbstractShaderPackOptionWidget createWidget(NavigationController navigation) {
		// FIXME: Better error handling
		return new StringShaderPackOptionWidget(option, this.values.getStringValue(this.optionId).orElse(option.getDefaultValue()));
	}
}
