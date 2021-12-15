package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.StringOption;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

public class StringElementWidget extends BaseOptionElementWidget {
	private final StringOption option;
	private final String appliedValue;
	private final int valueCount;

	private int valueIndex;

	public StringElementWidget(ShaderPackScreen screen, NavigationController navigation, StringOption option, Optional<String> pendingValue, Optional<String> appliedValue) {
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
		this.updateRenderParams(width, 0);

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
		this.tryRenderTooltip(poseStack, mouseX, mouseY, hovered);
	}

	private void increment(int amount) {
		this.valueIndex = Math.max(this.valueIndex, 0);

		this.valueIndex = Math.floorMod(this.valueIndex + amount, this.valueCount);
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
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			if (Screen.hasShiftDown()) {
				this.valueIndex = this.option.getAllowedValues().indexOf(this.option.getDefaultValue());
			} else {
				this.increment(button == GLFW.GLFW_MOUSE_BUTTON_1 ? 1 : -1);
			}
			this.onValueChanged();

			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
