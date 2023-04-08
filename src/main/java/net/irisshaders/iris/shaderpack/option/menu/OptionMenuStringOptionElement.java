package net.irisshaders.iris.shaderpack.option.menu;

import net.irisshaders.iris.shaderpack.ShaderProperties;
import net.irisshaders.iris.shaderpack.option.StringOption;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;

public class OptionMenuStringOptionElement extends OptionMenuOptionElement {
	public final StringOption option;

	public OptionMenuStringOptionElement(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, OptionValues values, StringOption option) {
		super(elementString, container, shaderProperties, values);
		this.option = option;
	}
}
