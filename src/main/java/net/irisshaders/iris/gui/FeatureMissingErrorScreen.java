package net.irisshaders.iris.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class FeatureMissingErrorScreen extends Screen {
	private final Screen parent;
	private final FormattedText messageTemp;
	private MultiLineLabel message;

	public FeatureMissingErrorScreen(Screen parent, Component title, Component message) {
		super(title);
		this.parent = parent;
		this.messageTemp = message;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, messageTemp, this.width - 50);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, arg -> this.minecraft.setScreen(parent)).bounds(this.width / 2 - 100, 140, 200, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(guiGraphics);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 90, 0xFFFFFF);
		message.renderCentered(guiGraphics, this.width / 2, 110, 9, 0xFFFFFF);
		super.render(guiGraphics, mouseX, mouseY, delta);
	}
}
