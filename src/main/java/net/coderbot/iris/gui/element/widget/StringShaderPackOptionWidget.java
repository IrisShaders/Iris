package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.StringOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

// TODO: Deduplicate a lot of code
public class StringShaderPackOptionWidget extends AbstractShaderPackOptionWidget {
	private static final Component DIVIDER = new TextComponent(": ");
	private static final int VALUE_SECTION_WIDTH = 35;

	private final StringOption option;
	private final int originalValueIndex;
	private final String originalValue;
	private final int valueCount;
	private final MutableComponent label;

	private @Nullable Component trimmedLabel = null;
	private Component valueLabel;
	private int valueIndex;
	private boolean needsTooltip;

	private int maxLabelWidth = -1;

	public StringShaderPackOptionWidget(StringOption option, String value) {
		this.option = option;
		this.label = new TranslatableComponent("option." + option.getName());

		List<String> values = option.getAllowedValues();
		this.valueCount = values.size();
		this.originalValueIndex = values.indexOf(value);
		this.valueIndex = originalValueIndex;
		this.originalValue = value;

		updateValueLabel();
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(poseStack, x, y, width, height, hovered, false);

		Font font = Minecraft.getInstance().font;

		int valueSectionWidth = Math.max(VALUE_SECTION_WIDTH, font.width(this.valueLabel) + 8);

		this.maxLabelWidth = (width - 8) - valueSectionWidth;

		if (this.trimmedLabel == null) {
			updateLabel();
		}

		GuiUtil.drawButton(poseStack, (x + width) - (valueSectionWidth + 2), y + 2, valueSectionWidth, height - 4, false, true);

		font.drawShadow(poseStack, trimmedLabel, x + 6, y + 7, 0xFFFFFF);
		font.drawShadow(poseStack, this.valueLabel, (x + (width - 2)) - (int)(valueSectionWidth * 0.5) - (int)(font.width(this.valueLabel) * 0.5), y + 7, 0xFFFFFF);

		if (hovered && this.needsTooltip) {
			// To prevent other elements from being drawn on top of the tooltip
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(font, poseStack, this.label, mouseX + 2, mouseY - 16));
		}
	}

	private void updateLabel() {
		Font font = Minecraft.getInstance().font;

		this.needsTooltip = font.width(label) > maxLabelWidth;

		MutableComponent label = GuiUtil.shortenText(font, this.label.copy().append(DIVIDER), maxLabelWidth);

		if (this.valueIndex != originalValueIndex) {
			label = label.withStyle(style -> style.withColor(TextColor.fromRgb(0xffc94a)));
		}
		this.trimmedLabel = label;

		updateValueLabel();
	}

	private void updateValueLabel() {
		String valueStr;
		if (this.valueIndex < 0) {
			valueStr = this.originalValue;
		} else {
			valueStr = this.option.getAllowedValues().get(this.valueIndex);
		}
		this.valueLabel = GuiUtil.translateOrDefault(
				new TextComponent(valueStr).withStyle(style -> style.withColor(TextColor.fromRgb(0x6688ff))),
				"value." + option.getName() + "." + valueStr);
	}

	private void next() {
		this.valueIndex = Math.max(this.valueIndex, 0);

		this.valueIndex++;

		if (this.valueIndex >= this.valueCount) {
			this.valueIndex = 0;
		}
	}

	private void queueOptionToPending() {
		Iris.addPendingShaderPackOption(this.option.getName(), this.option.getAllowedValues().get(valueIndex));
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			this.next();
			this.queueOptionToPending();
			this.updateLabel();

			GuiUtil.playButtonClickSound();
		}
		return super.mouseClicked(mx, my, button);
	}
}
