package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;

public abstract class BaseOptionElementWidget extends AbstractElementWidget {
	protected static final Component DIVIDER = new TextComponent(": ");
	protected static final int VALUE_SECTION_WIDTH = 35;

	protected final MutableComponent label;

	protected Component trimmedLabel;
	protected Component valueLabel;

	private boolean isLabelTrimmed;
	private int maxLabelWidth;

	protected BaseOptionElementWidget(MutableComponent label) {
		this.label = label;
	}

	protected final void renderOptionWithValue(PoseStack poseStack, int x, int y, int width, int height, boolean hovered, float sliderPosition, int sliderWidth) {
		GuiUtil.bindIrisWidgetsTexture();

		// Lazy init of value label
		if (this.valueLabel == null) {
			this.valueLabel = createValueLabel();
		}

		// Draw button background
		GuiUtil.drawButton(poseStack, x, y, width, height, hovered, false);

		// Determine the width of the value box
		Font font = Minecraft.getInstance().font;
		int valueSectionWidth = Math.max(sliderPosition < 0 ? VALUE_SECTION_WIDTH : VALUE_SECTION_WIDTH + 15, font.width(this.valueLabel) + 8);

		// Determine maximum width of trimmed label
		this.maxLabelWidth = (width - 8) - valueSectionWidth;

		// Lazy init of trimmed label, and make sure it is only trimmed when necessary
		if (this.trimmedLabel == null || font.width(this.label) > this.maxLabelWidth != isLabelTrimmed) {
			updateLabels();
		}

		// Set whether the label has been trimmed (used when updating label and determining whether to render tooltips)
		this.isLabelTrimmed = font.width(this.label) > this.maxLabelWidth;

		// Draw the value box
		GuiUtil.drawButton(poseStack, (x + width) - (valueSectionWidth + 2), y + 2, valueSectionWidth, height - 4, false, true);

		// Draw the preview slider
		if (sliderPosition >= 0) {
			// Range of x values the slider can occupy
			int sliderSpace = (valueSectionWidth - 4) - sliderWidth;

			// Position of slider
			int sliderPos = ((x + width) - valueSectionWidth) + (int)(sliderPosition * sliderSpace);

			GuiUtil.drawButton(poseStack, sliderPos, y + 4, sliderWidth, height - 8, false, false);
		}

		// Draw the label
		font.drawShadow(poseStack, trimmedLabel, x + 6, y + 7, 0xFFFFFF);
		// Draw the value label
		font.drawShadow(poseStack, this.valueLabel, (x + (width - 2)) - (int)(valueSectionWidth * 0.5) - (int)(font.width(this.valueLabel) * 0.5), y + 7, 0xFFFFFF);
	}

	protected final void renderOptionWithValue(PoseStack poseStack, int x, int y, int width, int height, boolean hovered) {
		this.renderOptionWithValue(poseStack, x, y, width, height, hovered, -1, 0);
	}

	protected final void renderTooltipIfTrimmed(PoseStack poseStack, int mouseX, int mouseY, boolean hovered) {
		if (this.isLabelTrimmed) {
			renderTooltip(poseStack, mouseX, mouseY, hovered);
		}
	}

	protected final void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, boolean hovered) {
		if (hovered) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(Minecraft.getInstance().font, poseStack, this.label, mouseX + 2, mouseY - 16));
		}
	}

	protected final void updateLabels() {
		this.trimmedLabel = createTrimmedLabel();
		this.valueLabel = createValueLabel();
	}

	protected final void queueValueToPending() {
		Iris.addPendingShaderPackOption(this.getOptionName(), this.getValue());
	}

	protected final Component createTrimmedLabel() {
		MutableComponent label = GuiUtil.shortenText(
				Minecraft.getInstance().font,
				this.label.copy().append(DIVIDER),
				this.maxLabelWidth);

		if (!this.isValueOriginal()) {
			label = label.withStyle(style -> style.withColor(TextColor.fromRgb(0xffc94a)));
		}

		return label;
	}

	protected abstract Component createValueLabel();

	public abstract String getOptionName();

	public abstract String getValue();

	public abstract boolean isValueOriginal();

	protected final void onValueChanged() {
		this.queueValueToPending();
		this.updateLabels();
	}
}
