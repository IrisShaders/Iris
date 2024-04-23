package net.irisshaders.iris.gui.element.widget;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.shaderpack.option.StringOption;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuStringOptionElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;

import java.util.List;

public class StringElementWidget extends BaseOptionElementWidget<OptionMenuStringOptionElement> {
	protected final StringOption option;

	protected String appliedValue;
	protected int valueCount;
	protected int valueIndex;
	protected MutableComponent prefix, suffix;

	public StringElementWidget(OptionMenuStringOptionElement element) {
		super(element);

		this.option = element.option;
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		super.init(screen, navigation);

		// The yet-to-be-applied value that has been queued (if that is the case)
		// Might be equal to the applied value
		String actualPendingValue = this.element.getPendingOptionValues().getStringValueOrDefault(this.option.getName());

		// The value currently in use by the shader pack
		this.appliedValue = this.element.getAppliedOptionValues().getStringValueOrDefault(this.option.getName());

		// Do not use I18n, it'll cause issues with packs trying to use % as prefixes/suffixes.
		this.prefix = Component.literal(Language.getInstance().has("prefix." + this.option.getName()) ? Language.getInstance().getOrDefault("prefix." + this.option.getName()) : "");
		this.suffix = Component.literal(Language.getInstance().has("suffix." + this.option.getName()) ? Language.getInstance().getOrDefault("suffix." + this.option.getName()) : "");
		this.setLabel(GuiUtil.translateOrDefault(Component.literal(this.option.getName()), "option." + this.option.getName()));

		List<String> values = this.option.getAllowedValues();

		this.valueCount = values.size();
		this.valueIndex = values.indexOf(actualPendingValue);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		this.updateRenderParams(0);

		this.renderOptionWithValue(guiGraphics, hovered || isFocused());
		if (usedKeyboard) {
			tryRenderTooltip(guiGraphics, bounds.getBoundInDirection(ScreenDirection.RIGHT), bounds.position().y(), hovered);
		} else {
			tryRenderTooltip(guiGraphics, mouseX, mouseY, hovered);
		}
	}

	private void increment(int amount) {
		this.valueIndex = Math.max(this.valueIndex, 0);

		this.valueIndex = Math.floorMod(this.valueIndex + amount, this.valueCount);
	}

	@Override
	protected Component createValueLabel() {
		return prefix.copy().append(GuiUtil.translateOrDefault(
			Component.literal(getValue()).append(suffix),
			"value." + this.option.getName() + "." + getValue())).withStyle(style -> style.withColor(TextColor.fromRgb(0x6688ff)));
	}

	@Override
	public String getCommentKey() {
		return "option." + this.option.getName() + ".comment";
	}

	public String getValue() {
		if (this.valueIndex < 0) {
			return this.appliedValue;
		}
		return this.option.getAllowedValues().get(this.valueIndex);
	}

	protected void queue() {
		Iris.getShaderPackOptionQueue().put(this.option.getName(), this.getValue());
	}

	@Override
	public boolean applyNextValue() {
		this.increment(1);
		this.queue();

		return true;
	}

	@Override
	public boolean applyPreviousValue() {
		this.increment(-1);
		this.queue();

		return true;
	}

	@Override
	public boolean applyOriginalValue() {
		this.valueIndex = this.option.getAllowedValues().indexOf(this.option.getDefaultValue());
		this.queue();

		return true;
	}

	@Override
	public boolean isValueModified() {
		return !this.appliedValue.equals(this.getValue());
	}
}
