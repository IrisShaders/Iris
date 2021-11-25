package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.OptionValues;

public abstract class OptionMenuOptionElement extends OptionMenuElement {
	protected final boolean slider;
	protected final OptionMenuContainer container;
	protected final String optionId;
	protected final OptionValues values;

	public OptionMenuOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values) {
		this.slider = shaderProperties.getSliderOptions().contains(elementString);
		this.container = container;
		this.optionId = elementString;
		this.values = values;
	}

	public String createDescriptionId() {
		return "option." + optionId;
	}
}
