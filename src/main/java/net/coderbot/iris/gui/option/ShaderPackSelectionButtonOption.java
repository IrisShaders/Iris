package net.coderbot.iris.gui.option;

import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TranslatableComponent;

public class ShaderPackSelectionButtonOption extends Option {
	private final Screen parent;
	private final Minecraft client;

	public ShaderPackSelectionButtonOption(Screen parent, Minecraft client) {
		super("options.iris.shaderPackSelection");
		this.parent = parent;
		this.client = client;
	}

	@Override
	public AbstractWidget createButton(Options options, int x, int y, int width) {
		return new Button(
				x, y, width, 20,
				new TranslatableComponent("options.iris.shaderPackSelection"),
				button -> client.setScreen(new ShaderPackScreen(parent))
		);
	}
}
