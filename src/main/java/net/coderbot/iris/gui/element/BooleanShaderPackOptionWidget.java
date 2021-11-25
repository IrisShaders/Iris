package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.BooleanOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import org.lwjgl.glfw.GLFW;

public class BooleanShaderPackOptionWidget extends AbstractShaderPackOptionWidget {
	private static final Component TEXT_TRUE = new TranslatableComponent("label.iris.true").withStyle(ChatFormatting.GREEN);
	private static final Component TEXT_FALSE = new TranslatableComponent("label.iris.false").withStyle(ChatFormatting.RED);
	private static final Component DIVIDER = new TextComponent(": ");
	private static final int VALUE_SECTION_WIDTH = 35;

	private final BooleanOption option;
	private final boolean originalValue;

	private Component label;
	private boolean value;

	private int maxLabelWidth = -1;

	public BooleanShaderPackOptionWidget(BooleanOption option, boolean value) {
		this.option = option;
		this.originalValue = value;
		this.value = value;

		updateLabel();
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(poseStack, x, y, width, height, hovered, false);

		GuiUtil.drawButton(poseStack, (x + width) - (VALUE_SECTION_WIDTH + 2), y + 2, VALUE_SECTION_WIDTH, height - 4, false, true);

		if (this.maxLabelWidth < 0) {
			this.maxLabelWidth = (width - 8) - VALUE_SECTION_WIDTH;

			updateLabel();
		}

		Font font = Minecraft.getInstance().font;
		font.drawShadow(poseStack, label, x + 6, y + 7, 0xFFFFFF);

		Component valueLabel = value ? TEXT_TRUE : TEXT_FALSE;
		font.drawShadow(poseStack, valueLabel, (x + (width - 2)) - (int)(VALUE_SECTION_WIDTH * 0.5) - (int)(font.width(valueLabel) * 0.5), y + 7, 0xFFFFFF);
	}

	private void updateLabel() {
		MutableComponent label = GuiUtil.shortenText(
				Minecraft.getInstance().font,
				new TranslatableComponent("option." + option.getName()).append(DIVIDER),
				maxLabelWidth);

		if (this.value != originalValue) {
			label = label.withStyle(style -> style.withColor(TextColor.fromRgb(0xffc94a)));
		}

		this.label = label;
	}

	private void next() {
		this.value = !this.value;
	}

	private void queueOptionToPending() {
		Iris.addPendingShaderPackOption(this.option.getName(), Boolean.toString(this.value));
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
