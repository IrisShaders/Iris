package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.StringOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class SliderElementWidget extends BaseOptionElementWidget {
	private static final int PREVIEW_SLIDER_WIDTH = 4;
	private static final int ACTIVE_SLIDER_WIDTH = 6;

	private final StringOption option;
	private final String appliedValue;
	private final int valueCount;

	private int valueIndex;
	private boolean mouseDown = false;

	public SliderElementWidget(ShaderPackScreen screen, NavigationController navigation, StringOption option, Optional<String> pendingValue, Optional<String> appliedValue) {
		super(screen, navigation, GuiUtil.translateOrDefault(new TextComponent(option.getName()), "option." + option.getName()));

		this.option = option;

		List<String> values = option.getAllowedValues();

		this.appliedValue = appliedValue.orElse(option.getDefaultValue()); // The value currently in use by the shader
		String actualSetValue = pendingValue.orElse(this.appliedValue); // The unapplied value that has been queued (if that is the case)

		this.valueCount = values.size();
		this.valueIndex = values.indexOf(actualSetValue);
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, 35);

		if (!hovered) {
			this.renderOptionWithValue(poseStack, x, y, width, height, false, (float)valueIndex / (valueCount - 1), PREVIEW_SLIDER_WIDTH);
		} else {
			this.renderSlider(poseStack, x, y, width, height, mouseX, mouseY, tickDelta);
		}

		if (!this.screen.isDisplayingComment()) {
			renderTooltip(poseStack, this.unmodifiedLabel, mouseX, mouseY, hovered);
		} else if (Screen.hasShiftDown()) {
			renderTooltip(poseStack, SET_TO_DEFAULT, mouseX, mouseY, hovered);
		}

		if (this.mouseDown) {
			// Release if the mouse went off the slider
			if (!hovered) {
				this.onReleased();
			}

			whileDragging(x, width, mouseX);
		}
	}

	private void renderSlider(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw background button
		GuiUtil.drawButton(poseStack, x, y, width, height, false, false);
		// Draw slider area
		GuiUtil.drawButton(poseStack, x + 2, y + 2, width - 4, height - 4, false, true);

		// Range of x values the slider can occupy
		int sliderSpace = (width - 8) - ACTIVE_SLIDER_WIDTH;
		// Position of slider
		int sliderPos = (x + 4) + (int)(((float)valueIndex / (valueCount - 1)) * sliderSpace);
		// Draw slider
		GuiUtil.drawButton(poseStack, sliderPos, y + 4, ACTIVE_SLIDER_WIDTH, height - 8, this.mouseDown, false);

		// Draw value label
		Font font = Minecraft.getInstance().font;
		font.drawShadow(poseStack, this.valueLabel, (int)(x + (width * 0.5)) - (int)(font.width(this.valueLabel) * 0.5), y + 7, 0xFFFFFF);
	}

	private void whileDragging(int x, int width, int mouseX) {
		float mousePositionAcrossWidget = Mth.clamp((float)(mouseX - (x + 4)) / (width - 8), 0, 1);

		int newValueIndex = Math.min(valueCount - 1, (int)(mousePositionAcrossWidget * valueCount));

		if (valueIndex != newValueIndex) {
			this.valueIndex = newValueIndex;

			this.updateLabels();
		}
	}

	private void onReleased() {
		mouseDown = false;

		this.onValueChanged();
	}

	@Override
	protected Component createValueLabel() {
		return GuiUtil.translateOrDefault(
				new TextComponent(getValue()).withStyle(style -> style.withColor(TextColor.fromRgb(0x6688ff))),
				"value." + option.getName() + "." + getValue());
	}

	@Override
	public String getOptionName() {
		return this.option.getName();
	}

	@Override
	public String getValue() {
		if (this.valueIndex < 0) {
			return this.appliedValue;
		}
		return this.option.getAllowedValues().get(this.valueIndex);
	}

	@Override
	public boolean isValueOriginal() {
		return this.appliedValue.equals(this.getValue());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			if (Screen.hasShiftDown()) {
				this.valueIndex = this.option.getAllowedValues().indexOf(this.option.getDefaultValue());
				GuiUtil.playButtonClickSound();

				return true;
			}

			mouseDown = true;
			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
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
