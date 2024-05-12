package net.irisshaders.iris.gui.element.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public abstract class BaseOptionElementWidget<T extends OptionMenuElement> extends CommentedElementWidget<T> {
	protected static final Component SET_TO_DEFAULT = Component.translatable("options.iris.setToDefault").withStyle(ChatFormatting.GREEN);
	protected static final Component DIVIDER = Component.literal(": ");

	protected MutableComponent unmodifiedLabel;
	protected ShaderPackScreen screen;
	protected NavigationController navigation;
	protected Component trimmedLabel;
	protected Component valueLabel;
	protected boolean usedKeyboard;
	private MutableComponent label;
	private boolean isLabelTrimmed;
	private int maxLabelWidth;
	private int valueSectionWidth;

	public BaseOptionElementWidget(T element) {
		super(element);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		this.screen = screen;
		this.navigation = navigation;
		this.valueLabel = null;
		this.trimmedLabel = null;
	}

	protected final void setLabel(MutableComponent label) {
		this.label = label.copy().append(DIVIDER);
		this.unmodifiedLabel = label;
	}

	protected final void updateRenderParams(int minValueSectionWidth) {
		// Check if we used the keyboard to access this value
		usedKeyboard = isFocused();

		// Lazy init of value label
		if (this.valueLabel == null) {
			this.valueLabel = createValueLabel();
		}

		// Determine the width of the value box
		Font font = Minecraft.getInstance().font;
		this.valueSectionWidth = Math.max(minValueSectionWidth, font.width(this.valueLabel) + 8);

		// Determine maximum width of trimmed label
		this.maxLabelWidth = (bounds.width() - 8) - this.valueSectionWidth;

		// Lazy init of trimmed label, and make sure it is only trimmed when necessary
		if (this.trimmedLabel == null || font.width(this.label) > this.maxLabelWidth != isLabelTrimmed) {
			updateLabels();
		}

		// Set whether the label has been trimmed (used when updating label and determining whether to render tooltips)
		this.isLabelTrimmed = font.width(this.label) > this.maxLabelWidth;
	}

	protected final void renderOptionWithValue(GuiGraphics guiGraphics, boolean hovered, float sliderPosition, int sliderWidth) {
		GuiUtil.bindIrisWidgetsTexture();

		// Draw button background
		GuiUtil.drawButton(guiGraphics, bounds.position().x(), bounds.position().y(), bounds.width(), bounds.height(), hovered, false);

		// Draw the value box
		GuiUtil.drawButton(guiGraphics, bounds.getBoundInDirection(ScreenDirection.RIGHT) - (this.valueSectionWidth + 2), bounds.position().y() + 2, this.valueSectionWidth, bounds.height() - 4, false, true);

		// Draw the preview slider
		if (sliderPosition >= 0) {
			// Range of x values the slider can occupy
			int sliderSpace = (this.valueSectionWidth - 4) - sliderWidth;

			// Position of slider
			int sliderPos = (bounds.getBoundInDirection(ScreenDirection.RIGHT) - this.valueSectionWidth) + (int) (sliderPosition * sliderSpace);

			GuiUtil.drawButton(guiGraphics, sliderPos, bounds.position().y() + 4, sliderWidth, bounds.height() - 8, false, false);
		}

		Font font = Minecraft.getInstance().font;

		// Draw the label
		guiGraphics.drawString(font, this.trimmedLabel, bounds.position().x() + 6, bounds.position().y() + 7, 0xFFFFFF);
		// Draw the value label
		guiGraphics.drawString(font, this.valueLabel, (bounds.getBoundInDirection(ScreenDirection.RIGHT) - 2) - (int) (this.valueSectionWidth * 0.5) - (int) (font.width(this.valueLabel) * 0.5), bounds.position().y() + 7, 0xFFFFFF);
	}

	protected final void renderOptionWithValue(GuiGraphics guiGraphics, boolean hovered) {
		this.renderOptionWithValue(guiGraphics, hovered, -1, 0);
	}

	protected final void tryRenderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered) {
		if (Screen.hasShiftDown()) {
			renderTooltip(guiGraphics, SET_TO_DEFAULT, mouseX, mouseY, hovered);
		} else if (this.isLabelTrimmed && !this.screen.isDisplayingComment()) {
			renderTooltip(guiGraphics, this.unmodifiedLabel, mouseX, mouseY, hovered);
		}
	}

	protected final void renderTooltip(GuiGraphics guiGraphics, Component text, int mouseX, int mouseY, boolean hovered) {
		if (hovered) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(Minecraft.getInstance().font, guiGraphics, text, mouseX + 2, mouseY - 16));
		}
	}

	protected final void updateLabels() {
		this.trimmedLabel = createTrimmedLabel();
		this.valueLabel = createValueLabel();
	}

	protected final Component createTrimmedLabel() {
		MutableComponent label = GuiUtil.shortenText(
			Minecraft.getInstance().font,
			this.label.copy(),
			this.maxLabelWidth);

		if (this.isValueModified()) {
			label = label.withStyle(style -> style.withColor(TextColor.fromRgb(0xffc94a)));
		}

		return label;
	}

	protected abstract Component createValueLabel();

	public abstract boolean applyNextValue();

	public abstract boolean applyPreviousValue();

	public abstract boolean applyOriginalValue();

	public abstract boolean isValueModified();

	public abstract @Nullable String getCommentKey();

	@Override
	public Optional<Component> getCommentTitle() {
		return Optional.of(this.unmodifiedLabel);
	}

	@Override
	public Optional<Component> getCommentBody() {
		return Optional.ofNullable(getCommentKey()).map(key -> I18n.exists(key) ? Component.translatable(key) : null);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1 || button == GLFW.GLFW_MOUSE_BUTTON_2) {
			boolean refresh = false;

			if (Screen.hasShiftDown()) {
				refresh = applyOriginalValue();
			}
			if (!refresh) {
				if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
					refresh = applyNextValue();
				} else {
					refresh = applyPreviousValue();
				}
			}

			if (refresh) {
				this.navigation.refresh();
			}

			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keycode, int scancode, int modifiers) {
		if (keycode == InputConstants.KEY_RETURN) {
			boolean refresh = Screen.hasControlDown()
				? applyOriginalValue()
				: (Screen.hasShiftDown() ? applyPreviousValue() : applyNextValue());

			if (refresh) {
				this.navigation.refresh();
			}

			GuiUtil.playButtonClickSound();

			return true;
		}

		return false;
	}
}
