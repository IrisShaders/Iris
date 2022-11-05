package net.coderbot.iris.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

public class FeatureMissingErrorScreen extends Screen {
	private final Screen parent;
	private MultiLineLabel message;
	private final FormattedText messageTemp;

	public FeatureMissingErrorScreen(Screen parent, Component title, Component message) {
		super(title);
		this.parent = parent;
		this.messageTemp = message;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, messageTemp, this.width - 50);
		this.addRenderableWidget(new Button(this.width / 2 - 100, 140, 200, 20, CommonComponents.GUI_BACK, arg -> this.minecraft.setScreen(parent)));
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float delta) {
		this.renderBackground(poseStack);
		ErrorScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 90, 0xFFFFFF);
		message.renderCentered(poseStack, this.width / 2, 110, 9, 0xFFFFFF);
		super.render(poseStack, mouseX, mouseY, delta);
	}
}
