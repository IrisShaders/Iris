package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Collections;
import java.util.List;

public class UnimplementedWarningWidget extends AbstractParentElement implements Drawable {
	private final int x, y;

	private static final int width = 13;
	private static final int height = 15;

	public UnimplementedWarningWidget(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		GuiUtil.bindIrisWidgetsTexture();
		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.clearColor(1, 1, 1, 1);
		if (this.isHovered(mouseX, mouseY)) {
			drawTexture(matrices, this.x, this.y, 13, 146, width, height);
		} else {
			drawTexture(matrices, this.x, this.y, 0, 146, width, height);
		}
	}

	@Override
	public List<? extends Element> children() {
		return Collections.emptyList();
	}

	public boolean isHovered(int mouseX, int mouseY) {
		return (mouseX >= x) && (mouseX <= x + width) && (mouseY >= y) && (mouseY <= y + height);
	}
}
