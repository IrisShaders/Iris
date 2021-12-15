package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.MergedBooleanOption;
import net.coderbot.iris.shaderpack.option.MergedStringOption;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;

import java.util.Map;

public abstract class OptionMenuElement {
	public static final OptionMenuElement EMPTY = new OptionMenuElement() {};

	private static final String ELEMENT_EMPTY = "<empty>";
	private static final String ELEMENT_PROFILE = "<profile>";

	public static OptionMenuElement create(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions) throws IllegalArgumentException {
		// Empty element
		if (ELEMENT_EMPTY.equals(elementString)) {
			return EMPTY;
		}
		// Profile element
		if (ELEMENT_PROFILE.equals(elementString)) {
			return new OptionMenuProfileElement(container.getProfiles(), shaderPackOptions.getOptionSet(), shaderPackOptions.getOptionValues());
		}
		// Link to sub screen element
		if (elementString.startsWith("[") && elementString.endsWith("]")) {
			return new OptionMenuLinkElement(elementString.substring(1, elementString.length() - 1));
		}

		Map<String, MergedBooleanOption> booleanOptions = shaderPackOptions.getOptionSet().getBooleanOptions();
		Map<String, MergedStringOption> stringOptions = shaderPackOptions.getOptionSet().getStringOptions();

		// Option elements (boolean and string), only succeed if the option is defined in the shader source
		if (booleanOptions.containsKey(elementString)) {
			return new OptionMenuBooleanOptionElement(elementString, container, shaderProperties, shaderPackOptions.getOptionValues(), booleanOptions.get(elementString).getOption());
		} else if (stringOptions.containsKey(elementString)) {
			return new OptionMenuStringOptionElement(elementString, container, shaderProperties, shaderPackOptions.getOptionValues(), stringOptions.get(elementString).getOption());
		}

		// Handled and ignored with log warning
		throw new IllegalArgumentException("Unable to resolve shader pack option menu element \"" + elementString + "\" defined in shaders.properties");
	}
}
