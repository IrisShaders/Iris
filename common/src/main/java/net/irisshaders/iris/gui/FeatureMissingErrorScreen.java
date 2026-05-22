package net.irisshaders.iris.gui;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FeatureMissingErrorScreen extends Screen {
	private final Screen parent;
	private final Component messageTemp;
	private MultiLineLabel message;

	public FeatureMissingErrorScreen(Screen parent, Component title, Component message) {
		super(title);
		this.parent = parent;
		this.messageTemp = message;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.width - 50, messageTemp);
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, arg -> this.minecraft.setScreen(parent)).bounds(this.width / 2 - 100, 140, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
		this.extractBackground(guiGraphics, mouseX, mouseY, delta);
		ActiveTextCollector activeTextCollector = guiGraphics.textRenderer();
		guiGraphics.centeredText(this.font, this.title, this.width / 2, 90, 0xFFFFFFFF);
		message.visitLines(TextAlignment.CENTER,  this.width / 2, 110, 9, activeTextCollector);
		super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
	}
}
