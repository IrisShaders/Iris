package net.coderbot.iris.shaderpack.option.menu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.widget.AbstractShaderPackOptionWidget;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.shaderpack.ShaderProperties;
import net.coderbot.iris.shaderpack.option.MergedBooleanOption;
import net.coderbot.iris.shaderpack.option.MergedStringOption;
import net.coderbot.iris.shaderpack.option.ShaderPackOptions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Map;

public abstract class OptionMenuElement {
	public static final OptionMenuElement EMPTY = new OptionMenuElement() {
		@Override
		public AbstractShaderPackOptionWidget createWidget(NavigationController navigation) {
			return new AbstractShaderPackOptionWidget() {
				@Override
				public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {}
			};
		}
	};
	public static final OptionMenuElement ERROR = new OptionMenuElement() {
		@Override
		public AbstractShaderPackOptionWidget createWidget(NavigationController navigation) {
			return new AbstractShaderPackOptionWidget() {
				private final Component label = new TextComponent("ERROR").withStyle(ChatFormatting.DARK_RED);

				@Override
				public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
					GuiUtil.bindIrisWidgetsTexture();
					GuiUtil.drawButton(poseStack, x, y, width, height, false, true);

					Font font = Minecraft.getInstance().font;
					font.draw(poseStack, label, x + (int)(width * 0.5) - (int)(font.width(label) * 0.5), y + 7, 0xFFFFFF);
				}
			};
		}
	};

	private static final String ELEMENT_EMPTY = "<empty>";
	private static final String ELEMENT_PROFILE = "<profile>";

	public abstract AbstractShaderPackOptionWidget createWidget(NavigationController navigation);

	public static OptionMenuElement create(String elementString, OptionMenuContainer container, ShaderProperties shaderProperties, ShaderPackOptions shaderPackOptions) throws IllegalArgumentException {
		// Empty element
		if (ELEMENT_EMPTY.equals(elementString)) {
			return EMPTY;
		}
		// Profile element
		if (ELEMENT_PROFILE.equals(elementString)) {
			return EMPTY; // TODO: Profiles
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
			return new OptionMenuStringOptionElement(elementString, container, shaderProperties, shaderPackOptions.getOptionValues(), stringOptions.get(elementString).getOption(), shaderProperties.getSliderOptions().contains(elementString));
		}

		// Handled and ignored with log warning
		throw new IllegalArgumentException("Unable to resolve shader pack option menu element \"" + elementString + "\" defined in shaders.properties");
	}
}
