package net.coderbot.iris.gui.option;

import net.minecraft.client.gui.widget.DoubleOptionSliderWidget;
import net.minecraft.client.option.DoubleOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.OrderedText;

import java.util.List;

public class ShadowDistanceSliderWidget extends DoubleOptionSliderWidget {
	public ShadowDistanceSliderWidget(GameOptions gameOptions, int x, int y, int width, int height, DoubleOption option, List<OrderedText> orderedTooltip) {
		super(gameOptions, x, y, width, height, option, orderedTooltip);
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
