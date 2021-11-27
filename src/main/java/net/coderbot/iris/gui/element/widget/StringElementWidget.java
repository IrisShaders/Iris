package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.StringOption;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class StringElementWidget extends BaseOptionElementWidget {
	private final StringOption option;
	private final int originalValueIndex;
	private final String originalValue;
	private final int valueCount;

	private int valueIndex;

	public StringElementWidget(StringOption option, String value) {
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
		this.updateRenderParams(width, VALUE_SECTION_WIDTH);

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
		this.renderTooltipIfTrimmed(poseStack, mouseX, mouseY, hovered);
	}

	private void increment(int amount) {
		this.valueIndex = Math.max(this.valueIndex, 0);

		this.valueIndex += amount;

		if (this.valueIndex >= this.valueCount) {
			this.valueIndex = 0;
		}
		if (this.valueIndex < 0) {
			this.valueIndex = this.valueCount - 1;
		}
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
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			this.increment(button == GLFW.GLFW_MOUSE_BUTTON_1 ? 1 : -1);
			this.onValueChanged();

			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
