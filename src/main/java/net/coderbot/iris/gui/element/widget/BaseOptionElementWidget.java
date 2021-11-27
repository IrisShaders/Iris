package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;

public abstract class BaseOptionElementWidget extends CommentedElementWidget {
	protected static final Component SET_TO_DEFAULT = new TranslatableComponent("options.iris.setToDefault").withStyle(ChatFormatting.GREEN);
	protected static final Component DIVIDER = new TextComponent(": ");
	protected static final int VALUE_SECTION_WIDTH = 35;

	protected final MutableComponent unmodifiedLabel;
	private final MutableComponent label;

	protected Component trimmedLabel;
	protected Component valueLabel;

	private boolean isLabelTrimmed;
	private int maxLabelWidth;
	private int valueSectionWidth;

	protected BaseOptionElementWidget(MutableComponent label) {
		this.label = label.copy().append(DIVIDER);
		this.unmodifiedLabel = label;
	}

	protected final void updateRenderParams(int width, int valueSectionWidth) {
		// Lazy init of value label
		if (this.valueLabel == null) {
			this.valueLabel = createValueLabel();
		}

		// Determine the width of the value box
		Font font = Minecraft.getInstance().font;
		this.valueSectionWidth = Math.max(valueSectionWidth, font.width(this.valueLabel) + 8);

		// Determine maximum width of trimmed label
		this.maxLabelWidth = (width - 8) - this.valueSectionWidth;

		// Lazy init of trimmed label, and make sure it is only trimmed when necessary
		if (this.trimmedLabel == null || font.width(this.label) > this.maxLabelWidth != isLabelTrimmed) {
			updateLabels();
		}

		// Set whether the label has been trimmed (used when updating label and determining whether to render tooltips)
		this.isLabelTrimmed = font.width(this.label) > this.maxLabelWidth;
	}

	protected final void renderOptionWithValue(PoseStack poseStack, int x, int y, int width, int height, boolean hovered, float sliderPosition, int sliderWidth) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw button background
		GuiUtil.drawButton(poseStack, x, y, width, height, hovered, false);

		// Draw the value box
		GuiUtil.drawButton(poseStack, (x + width) - (this.valueSectionWidth + 2), y + 2, this.valueSectionWidth, height - 4, false, true);

		// Draw the preview slider
		if (sliderPosition >= 0) {
			// Range of x values the slider can occupy
			int sliderSpace = (this.valueSectionWidth - 4) - sliderWidth;

			// Position of slider
			int sliderPos = ((x + width) - this.valueSectionWidth) + (int)(sliderPosition * sliderSpace);

			GuiUtil.drawButton(poseStack, sliderPos, y + 4, sliderWidth, height - 8, false, false);
		}

		Font font = Minecraft.getInstance().font;

		// Draw the label
		font.drawShadow(poseStack, this.trimmedLabel, x + 6, y + 7, 0xFFFFFF);
		// Draw the value label
		font.drawShadow(poseStack, this.valueLabel, (x + (width - 2)) - (int)(this.valueSectionWidth * 0.5) - (int)(font.width(this.valueLabel) * 0.5), y + 7, 0xFFFFFF);
	}

	protected final void renderOptionWithValue(PoseStack poseStack, int x, int y, int width, int height, boolean hovered) {
		this.renderOptionWithValue(poseStack, x, y, width, height, hovered, -1, 0);
	}

	protected final void tryRenderTooltip(PoseStack poseStack, int mouseX, int mouseY, boolean hovered) {
		if (this.isLabelTrimmed || Screen.hasShiftDown()) {
			renderTooltip(poseStack, mouseX, mouseY, hovered);
		}
	}

	protected final void renderTooltip(PoseStack poseStack, int mouseX, int mouseY, boolean hovered) {
		if (hovered) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
					Minecraft.getInstance().font, poseStack,
					Screen.hasShiftDown() ? SET_TO_DEFAULT : this.unmodifiedLabel,
					mouseX + 2, mouseY - 16));
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
				this.label.copy(),
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

	@Override
	public Component getCommentTitle() {
		return this.unmodifiedLabel;
	}

	@Override
	public Component getCommentBody() {
		String translation = "option." + getOptionName() + ".comment";
		return I18n.exists(translation) ? new TranslatableComponent(translation) : null;
	}

	protected final void onValueChanged() {
		this.queueValueToPending();
		this.updateLabels();
	}
}
