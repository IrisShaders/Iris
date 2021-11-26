package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.widget.AbstractShaderPackOptionWidget;
import net.coderbot.iris.gui.element.widget.BooleanShaderPackOptionWidget;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.BooleanOption;
import net.coderbot.iris.shaderpack.option.OptionValues;

public class OptionMenuBooleanOptionElement extends OptionMenuOptionElement {
	private final BooleanOption option;

	public OptionMenuBooleanOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, BooleanOption option) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
	}

	@Override
	public AbstractShaderPackOptionWidget createWidget(NavigationController navigation) {
		return new BooleanShaderPackOptionWidget(option, this.values.shouldFlip(optionId) != option.getDefaultValue()); // Same logic as manually flipping the default value should it be marked as flipped
	}
}
