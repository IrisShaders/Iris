package net.irisshaders.iris.gui.element.widget;

import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
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
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(35);


		if (!hovered && !isFocused()) {
			if (usedKeyboard) {
				usedKeyboard = false;
				mouseDown = false;
			}
			this.renderOptionWithValue(guiGraphics, false, (float) valueIndex / (valueCount - 1), PREVIEW_SLIDER_WIDTH);
		} else {
			this.renderSlider(guiGraphics);
		}

		if (usedKeyboard) {
			if (Screen.hasShiftDown()) {
				renderTooltip(guiGraphics, SET_TO_DEFAULT, bounds.getBoundInDirection(ScreenDirection.RIGHT), bounds.position().y(), hovered);
			} else if (!this.screen.isDisplayingComment()) {
				renderTooltip(guiGraphics, this.unmodifiedLabel, bounds.getBoundInDirection(ScreenDirection.RIGHT), bounds.position().y(), hovered);
			}
		} else {
			if (Screen.hasShiftDown()) {
				renderTooltip(guiGraphics, SET_TO_DEFAULT, mouseX, mouseY, hovered);
			} else if (!this.screen.isDisplayingComment()) {
				renderTooltip(guiGraphics, this.unmodifiedLabel, mouseX, mouseY, hovered);
			}
		}

		if (usedKeyboard) {
			if (!isFocused()) {
				usedKeyboard = false;
				this.onReleased();
			}
		}

		if (this.mouseDown && !usedKeyboard) {
			// Release if the mouse went off the slider
			if (!hovered) {
				this.onReleased();
			}

			whileDragging(mouseX);
		}
	}

	private void renderSlider(GuiGraphics guiGraphics) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw background button
		GuiUtil.drawButton(guiGraphics, bounds.position().x(), bounds.position().y(), bounds.width(), bounds.height(), isFocused(), false);
		// Draw slider area
		GuiUtil.drawButton(guiGraphics, bounds.position().x() + 2, bounds.position().y() + 2, bounds.width() - 4, bounds.height() - 4, false, true);

		// Range of x values the slider can occupy
		int sliderSpace = (bounds.width() - 8) - ACTIVE_SLIDER_WIDTH;
		// Position of slider
		int sliderPos = (bounds.position().x() + 4) + (int) (((float) valueIndex / (valueCount - 1)) * sliderSpace);
		// Draw slider
		GuiUtil.drawButton(guiGraphics, sliderPos, bounds.position().y() + 4, ACTIVE_SLIDER_WIDTH, bounds.height() - 8, this.mouseDown, false);

		// Draw value label
		Font font = Minecraft.getInstance().font;
		guiGraphics.drawString(font, this.valueLabel, bounds.getCenterInAxis(ScreenAxis.HORIZONTAL) - (int) (font.width(this.valueLabel) * 0.5), bounds.position().y() + 7, 0xFFFFFF);
	}

	private void whileDragging(int mouseX) {
		float mousePositionAcrossWidget = Mth.clamp((float) (mouseX - (bounds.position().x() + 4)) / (bounds.width() - 8), 0, 1);

		int newValueIndex = Math.min(valueCount - 1, (int) (mousePositionAcrossWidget * valueCount));

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
	public boolean keyPressed(int keycode, int scancode, int modifiers) {
		if (keycode == GLFW.GLFW_KEY_ENTER) {
			if (Screen.hasShiftDown()) {
				if (this.applyOriginalValue()) {
					this.navigation.refresh();
				}
				GuiUtil.playButtonClickSound();

				return true;
			}

			mouseDown = !mouseDown;
			usedKeyboard = true;
			GuiUtil.playButtonClickSound();

			return true;
		}

		if (mouseDown && usedKeyboard) {
			if (keycode == GLFW.GLFW_KEY_LEFT) {
				valueIndex = Math.max(0, valueIndex - 1);
				this.updateLabels();
				return true;
			} else if (keycode == GLFW.GLFW_KEY_RIGHT) {
				valueIndex = Math.min(valueCount - 1, valueIndex + 1);
				this.updateLabels();
				return true;
			}
		}

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
