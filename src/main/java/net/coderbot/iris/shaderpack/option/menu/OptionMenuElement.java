package net.coderbot.iris.shaderpack.option.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.MergedBooleanOption;
import net.coderbot.iris.shaderpack.option.MergedStringOption;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;

import java.util.Map;

public abstract class OptionMenuElement {
	public static final OptionMenuElement EMPTY = new OptionMenuElement() {
		@Override
		public AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation) {
			return new AbstractElementWidget() {
				@Override
				public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {}
			};
		}
	};

	private static final String ELEMENT_EMPTY = "<empty>";
	private static final String ELEMENT_PROFILE = "<profile>";

	public abstract AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation);

	public static OptionMenuElement create(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions) throws IllegalArgumentException {
		// Empty element
		if (ELEMENT_EMPTY.equals(elementString)) {
			return EMPTY;
		}
		// Profile element
		if (ELEMENT_PROFILE.equals(elementString)) {
			return new OptionMenuProfileElement(container.getCurrentProfile(), container.getProfiles());
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
