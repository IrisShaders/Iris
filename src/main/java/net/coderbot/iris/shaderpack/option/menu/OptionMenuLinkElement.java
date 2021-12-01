package net.coderbot.iris.shaderpack.option.menu;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.LinkElementWidget;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.network.chat.TextComponent;

public class OptionMenuLinkElement extends OptionMenuElement {
	private final String targetScreenId;

	public OptionMenuLinkElement(String targetScreenId) {
		this.targetScreenId = targetScreenId;
	}

	@Override
	public AbstractElementWidget createWidget(ShaderPackScreen screen, NavigationController navigation) {
		return new LinkElementWidget(navigation, GuiUtil.translateOrDefault(new TextComponent(targetScreenId), "screen." + targetScreenId), targetScreenId);
	}
}
