package net.coderbot.iris.gui.option;

import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.Option;
import net.minecraft.text.TranslatableText;

public class ShaderPackSelectionButtonOption extends Option {
	private final Screen parent;
	private final MinecraftClient client;

	public ShaderPackSelectionButtonOption(Screen parent, MinecraftClient client) {
		super("options.iris.shaderPackSelection");
		this.parent = parent;
		this.client = client;
	}

	@Override
	public AbstractButtonWidget createButton(GameOptions options, int x, int y, int width) {
		return new OptionButtonWidget(
				x, y, width, 20,
				this,
				new TranslatableText("options.iris.shaderPackSelection"),
				button -> client.openScreen(new ShaderPackScreen(parent))
		);
	}
}
