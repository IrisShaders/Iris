package net.coderbot.iris.gui.option;

import net.minecraft.client.gui.widget.DoubleOptionSliderWidget;
import net.minecraft.client.options.DoubleOption;
import net.minecraft.client.options.GameOptions;

public class ShadowDistanceSliderWidget extends DoubleOptionSliderWidget {
	public ShadowDistanceSliderWidget(GameOptions gameOptions, int x, int y, int width, int height, DoubleOption option) {
		super(gameOptions, x, y, width, height, option);
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		boolean actuallyActive = this.active;
		this.active = true;

		// Temporarily set active to true so that isMouseOver doesn't immediately bail out.
		// We don't just copy the code here in case some other mod wants to change how it works.
		boolean mouseOver = super.isMouseOver(mouseX, mouseY);

		this.active = actuallyActive;
		return mouseOver;
	}
}
