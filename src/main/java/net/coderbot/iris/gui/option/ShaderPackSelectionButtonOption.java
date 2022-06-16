package net.coderbot.iris.gui.option;

import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;


public class ShaderPackSelectionButtonOption {
	private final Screen parent;
	private final Minecraft client;

	public ShaderPackSelectionButtonOption(Screen parent, Minecraft client) {
		super();
		this.parent = parent;
		this.client = client;
	}
}
