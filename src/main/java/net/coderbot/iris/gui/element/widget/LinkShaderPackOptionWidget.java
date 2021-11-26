package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;

public class LinkShaderPackOptionWidget extends AbstractShaderPackOptionWidget {
	private static final Component ARROW = new TextComponent(">");

	private final NavigationController navigation;
	private final String targetScreenId;
	private final MutableComponent label;

	private MutableComponent trimmedLabel = null;
	private boolean needsTooltip = false;

	public LinkShaderPackOptionWidget(NavigationController navigation, MutableComponent label, String targetScreenId) {
		this.navigation = navigation;
		this.label = label;
		this.targetScreenId = targetScreenId;
	}

	@Override
	public void render(PoseStack poseStack, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(poseStack, x, y, width, height, hovered, false);

		Font font = Minecraft.getInstance().font;

		int labelWidth = width - 20;
		if (font.width(this.label) > labelWidth) {
			this.needsTooltip = true;
		}
		if (this.trimmedLabel == null) {
			this.trimmedLabel = GuiUtil.shortenText(font, this.label, labelWidth);
		}

		font.drawShadow(poseStack, this.trimmedLabel, x + (int)(width * 0.5) - (int)(font.width(this.trimmedLabel) * 0.5), y + 7, 0xFFFFFF);
		font.draw(poseStack, ARROW, (x + width) - 10, y + 7, 0xFFFFFF);

		if (hovered && this.needsTooltip) {
			// To prevent other elements from being drawn on top of the tooltip
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(font, poseStack, this.label, mouseX + 2, mouseY - 16));
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
			navigation.open(targetScreenId);
			GuiUtil.playButtonClickSound();

			return true;
		}
		return super.mouseClicked(mx, my, button);
	}
}
