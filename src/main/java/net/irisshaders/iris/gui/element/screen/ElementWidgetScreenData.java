package net.irisshaders.iris.gui.element.screen;

import net.minecraft.network.chat.Component;


public record ElementWidgetScreenData(Component heading, boolean backButton) {
	public static final ElementWidgetScreenData EMPTY = new ElementWidgetScreenData(Component.empty(), true);

}
