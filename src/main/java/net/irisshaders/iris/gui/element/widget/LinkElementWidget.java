package net.irisshaders.iris.gui.element.widget;

import com.mojang.blaze3d.platform.InputConstants;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuLinkElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

public class LinkElementWidget extends CommentedElementWidget<OptionMenuLinkElement> {
	private static final Component ARROW = Component.literal(">");

	private final String targetScreenId;
	private final MutableComponent label;

	private NavigationController navigation;
	private MutableComponent trimmedLabel = null;
	private boolean isLabelTrimmed = false;

	public LinkElementWidget(OptionMenuLinkElement element) {
		super(element);

		this.targetScreenId = element.targetScreenId;
		this.label = GuiUtil.translateOrDefault(Component.literal(element.targetScreenId), "screen." + element.targetScreenId);
	}

	@Override
	public void init(ShaderPackScreen screen, NavigationController navigation) {
		this.navigation = navigation;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(guiGraphics, bounds.position().x(), bounds.position().y(), bounds.width(), bounds.height(), hovered || isFocused(), false);

		Font font = Minecraft.getInstance().font;

		int maxLabelWidth = bounds.width() - 9;

		if (font.width(this.label) > maxLabelWidth) {
			this.isLabelTrimmed = true;
		}

		if (this.trimmedLabel == null) {
			this.trimmedLabel = GuiUtil.shortenText(font, this.label, maxLabelWidth);
		}

		int labelWidth = font.width(this.trimmedLabel);

		guiGraphics.drawString(font, this.trimmedLabel, bounds.getCenterInAxis(ScreenAxis.HORIZONTAL) - (int) (labelWidth * 0.5) - (int) (0.5 * Math.max(labelWidth - (bounds.width() - 18), 0)), bounds.position().y() + 7, 0xFFFFFF);
		guiGraphics.drawString(font, ARROW, bounds.getBoundInDirection(ScreenDirection.RIGHT) - 9, bounds.position().y() + 7, 0xFFFFFF);

		if (hovered && this.isLabelTrimmed) {
			// To prevent other elements from being drawn on top of the tooltip
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(font, guiGraphics, this.label, mouseX + 2, mouseY - 16));
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			this.navigation.open(targetScreenId);
			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int pInt1, int pInt2) {
		if (keyCode == InputConstants.KEY_RETURN) {
			this.navigation.open(targetScreenId);
			GuiUtil.playButtonClickSound();

			return true;
		}

		return super.keyPressed(keyCode, pInt1, pInt2);
	}

	@Override
	public Optional<Component> getCommentTitle() {
		return Optional.of(this.label);
	}

	@Override
	public Optional<Component> getCommentBody() {
		String translation = "screen." + this.targetScreenId + ".comment";
		return Optional.ofNullable(I18n.exists(translation) ? Component.translatable(translation) : null);
	}
}
