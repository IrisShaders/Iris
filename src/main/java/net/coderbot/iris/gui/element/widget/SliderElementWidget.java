package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class SliderElementWidget extends StringElementWidget {
	private static final int PREVIEW_SLIDER_WIDTH = 4;
	private static final int ACTIVE_SLIDER_WIDTH = 6;

	private boolean mouseDown = false;

	public SliderElementWidget(OptionMenuStringOptionElement element) {
		super(element);
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(35);

		if (!hovered && !isFocused()) {
			this.renderOptionWithValue(poseStack, false, (float)valueIndex / (valueCount - 1), PREVIEW_SLIDER_WIDTH);
		} else {
			this.renderSlider(poseStack);
		}

		if (Screen.hasShiftDown()) {
			renderTooltip(poseStack, SET_TO_DEFAULT, mouseX, mouseY, hovered);
		} else if (!this.screen.isDisplayingComment()) {
			renderTooltip(poseStack, this.unmodifiedLabel, mouseX, mouseY, hovered);
		}

		if (this.mouseDown) {
			// Release if the mouse went off the slider
			if (!hovered) {
				this.onReleased();
			}

			whileDragging(mouseX);
		}
	}

	private void renderSlider(PoseStack poseStack) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw background button
		GuiUtil.drawButton(poseStack, bounds.position().x(), bounds.position().y(), bounds.width(), bounds.height(), isFocused(), false);
		// Draw slider area
		GuiUtil.drawButton(poseStack, bounds.position().x() + 2, bounds.position().y() + 2, bounds.width() - 4, bounds.height() - 4, false, true);

		// Range of x values the slider can occupy
		int sliderSpace = (bounds.width() - 8) - ACTIVE_SLIDER_WIDTH;
		// Position of slider
		int sliderPos = (bounds.position().x() + 4) + (int)(((float)valueIndex / (valueCount - 1)) * sliderSpace);
		// Draw slider
		GuiUtil.drawButton(poseStack, sliderPos, bounds.position().y() + 4, ACTIVE_SLIDER_WIDTH, bounds.height() - 8, this.mouseDown, false);

		// Draw value label
		Font font = Minecraft.getInstance().font;
		font.drawShadow(poseStack, this.valueLabel, bounds.getCenterInAxis(ScreenAxis.HORIZONTAL) - (int)(font.width(this.valueLabel) * 0.5), bounds.position().y() + 7, 0xFFFFFF);
	}

	private void whileDragging(int mouseX) {
		float mousePositionAcrossWidget = Mth.clamp((float)(mouseX - (bounds.position().x() + 4)) / (bounds.width() - 8), 0, 1);

		int newValueIndex = Math.min(valueCount - 1, (int)(mousePositionAcrossWidget * valueCount));

		if (valueIndex != newValueIndex) {
			this.valueIndex = newValueIndex;

			this.updateLabels();
		}
	}

	private void onReleased() {
		mouseDown = false;

		this.queue();
		this.navigation.refresh();

		GuiUtil.playButtonClickSound();
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			if (Screen.hasShiftDown()) {
				if (this.applyOriginalValue()) {
					this.navigation.refresh();
				}
				GuiUtil.playButtonClickSound();

				return true;
			}

			mouseDown = true;
			GuiUtil.playButtonClickSound();

			return true;
		}

		// Do not use base widget's button click behavior
		return false;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			this.onReleased();

			return true;
		}
		return super.mouseReleased(mx, my, button);
	}
}
