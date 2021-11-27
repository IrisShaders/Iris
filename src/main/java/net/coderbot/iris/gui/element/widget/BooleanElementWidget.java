package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.shaderpack.option.BooleanOption;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.lwjgl.glfw.GLFW;

public class BooleanElementWidget extends BaseOptionElementWidget {
	private static final Component TEXT_TRUE = new TranslatableComponent("label.iris.true").withStyle(ChatFormatting.GREEN);
	private static final Component TEXT_FALSE = new TranslatableComponent("label.iris.false").withStyle(ChatFormatting.RED);

	private final BooleanOption option;
	private final boolean originalValue;

	private boolean value;

	public BooleanElementWidget(BooleanOption option, boolean value) {
		super(new TranslatableComponent("option." + option.getName()));
		this.option = option;
		this.originalValue = value;
		this.value = value;
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, VALUE_SECTION_WIDTH);

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
		this.tryRenderTooltip(poseStack, mouseX, mouseY, hovered);
	}

	@Override
	protected Component createValueLabel() {
		return this.value ? TEXT_TRUE : TEXT_FALSE;
	}

	@Override
	public String getOptionName() {
		return this.option.getName();
	}

	@Override
	public String getValue() {
		return Boolean.toString(this.value);
	}

	@Override
	public boolean isValueOriginal() {
		return this.value == this.originalValue;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			if (Screen.hasShiftDown()) {
				this.value = this.option.getDefaultValue();
			} else {
				this.value = !this.value;
			}
			this.onValueChanged();

			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
