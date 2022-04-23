package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.BooleanOption;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuBooleanOptionElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class BooleanElementWidget extends BaseOptionElementWidget<OptionMenuBooleanOptionElement> {
	private static final Component TEXT_TRUE = new TranslatableComponent("label.iris.true").withStyle(ChatFormatting.GREEN);
	private static final Component TEXT_FALSE = new TranslatableComponent("label.iris.false").withStyle(ChatFormatting.RED);

	private final BooleanOption option;

	private boolean appliedValue;
	private boolean value;

	public BooleanElementWidget(OptionMenuBooleanOptionElement element) {
		super(element);

		this.option = element.option;
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		super.init(screen, navigation);

		// The value currently in use by the shader pack
		this.appliedValue = this.element.getAppliedOptionValues().getBooleanValueOrDefault(this.option.getName());

		// The yet-to-be-applied value that has been queued (if that is the case)
		// Might be equal to the applied value
		this.value = this.element.getPendingOptionValues().getBooleanValueOrDefault(this.option.getName());

		this.setLabel(GuiUtil.translateOrDefault(new TextComponent(this.option.getName()), "option." + this.option.getName()));
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(width, 28);

		this.renderOptionWithValue(poseStack, x, y, width, height, hovered);
		this.tryRenderTooltip(poseStack, mouseX, mouseY, hovered);
	}

	@Override
	protected Component createValueLabel() {
		return this.value ? TEXT_TRUE : TEXT_FALSE;
	}

	@Override
	public String getCommentKey() {
		return "option." + this.option.getName() + ".comment";
	}

	public String getValue() {
		return Boolean.toString(this.value);
	}

	private void queue() {
		Iris.getShaderPackOptionQueue().put(this.option.getName(), this.getValue());
	}

	@Override
	public boolean applyNextValue() {
		this.value = !this.value;
		this.queue();

		return true;
	}

	@Override
	public boolean applyPreviousValue() {
		return this.applyNextValue();
	}

	@Override
	public boolean applyOriginalValue() {
		this.value = this.option.getDefaultValue();
		this.queue();

		return true;
	}

	@Override
	public boolean isValueModified() {
		return this.value != this.appliedValue;
	}
}
