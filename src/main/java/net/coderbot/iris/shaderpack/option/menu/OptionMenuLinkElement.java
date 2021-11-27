package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.LinkElementWidget;
import net.coderbot.iris.gui.NavigationController;
import net.minecraft.network.chat.TranslatableComponent;

public class OptionMenuLinkElement extends OptionMenuElement {
	private final String targetScreenId;

	public OptionMenuLinkElement(String targetScreenId) {
		this.targetScreenId = targetScreenId;
	}

	@Override
	public AbstractElementWidget createWidget(NavigationController navigation) {
		return new LinkElementWidget(navigation, new TranslatableComponent("screen." + targetScreenId), targetScreenId);
	}
}
