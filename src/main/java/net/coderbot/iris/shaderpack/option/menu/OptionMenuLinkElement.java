package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.AbstractShaderPackOptionWidget;
import net.coderbot.iris.gui.element.LinkShaderPackOptionWidget;
import net.coderbot.iris.gui.NavigationController;
import net.minecraft.network.chat.TranslatableComponent;

public class OptionMenuLinkElement extends OptionMenuElement {
	private final String targetScreenId;

	public OptionMenuLinkElement(String targetScreenId) {
		this.targetScreenId = targetScreenId;
	}

	@Override
	public AbstractShaderPackOptionWidget createWidget(NavigationController navigation) {
		return new LinkShaderPackOptionWidget(navigation, new TranslatableComponent("screen." + targetScreenId), targetScreenId);
	}
}
