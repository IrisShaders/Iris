package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.StringOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SliderElementWidget extends BaseOptionElementWidget {
	private static final int PREVIEW_SLIDER_WIDTH = 4;
	private static final int ACTIVE_SLIDER_WIDTH = 6;

	private final StringOption option;
	private final int originalValueIndex;
	private final String originalValue;
	private final int valueCount;

	private int valueIndex;
	private boolean mouseDown = false;

	public SliderElementWidget(StringOption option, String value) {
		super(new TranslatableComponent("option." + option.getName()));

		this.option = option;

		List<String> values = option.getAllowedValues();
		this.valueCount = values.size();
		this.originalValueIndex = values.indexOf(value);
		this.valueIndex = originalValueIndex;
		this.originalValue = value;
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		if (!hovered) {
			this.renderOptionWithValue(poseStack, x, y, width, height, false, (float)valueIndex / (valueCount - 1), PREVIEW_SLIDER_WIDTH);
		} else {
			this.renderSlider(poseStack, x, y, width, height, mouseX, mouseY, tickDelta);
		}

		this.renderTooltip(poseStack, mouseX, mouseY, hovered);

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
			return this.originalValue;
		}
		return this.option.getAllowedValues().get(this.valueIndex);
	}

	@Override
	public boolean isValueOriginal() {
		return this.originalValue.equals(this.getValue());
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
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
