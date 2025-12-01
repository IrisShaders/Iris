package net.irisshaders.iris.gui.element.screen;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class IrisButton extends Button {
	private final FloatSupplier alphaSupplier;

	public IrisButton(int pButton0, int pInt1, int pInt2, int pInt3, Component pComponent4, OnPress pButton$OnPress5, CreateNarration pButton$CreateNarration6, FloatSupplier alpha) {
		super(pButton0, pInt1, pInt2, pInt3, pComponent4, pButton$OnPress5, pButton$CreateNarration6);
		this.alphaSupplier = alpha;
	}

	public static IrisButton.Builder iris$builder(Component pComponent0, Button.OnPress pButton$OnPress1, FloatSupplier alpha) {
		return new IrisButton.Builder(pComponent0, pButton$OnPress1, alpha);
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int pInt1, int pInt2, float pFloat3) {
		Minecraft lvMinecraft5 = Minecraft.getInstance();
		//guiGraphics.flush();
		// TODO 1.21.6
		//RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.isHoveredOrFocused() ? this.alphaSupplier.getAsFloat() * 1.8f : this.alphaSupplier.getAsFloat());
		GlStateManager._enableBlend();
		GlStateManager._enableDepthTest();
		GuiUtil.bindIrisWidgetsTexture();
		GuiUtil.drawButton(guiGraphics, this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.isHoveredOrFocused(), !this.isActive());
		//guiGraphics.flush();
		//RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alphaSupplier.getAsFloat());
		int lvInt6 = this.active ? 16777215 : 10526880;
		this.renderString(guiGraphics, lvMinecraft5.font, lvInt6 | Mth.ceil(this.alphaSupplier.getAsFloat() * 255.0F) << 24);
		//guiGraphics.flush();
		//RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

	}

	public static class Builder {
		private final Component message;
		private final Button.OnPress onPress;
		private final FloatSupplier alpha;
		@Nullable
		private Tooltip tooltip;
		private int x;
		private int y;
		private int width = 150;
		private int height = 20;
		private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

		public Builder(Component pButton$Builder0, Button.OnPress pButton$OnPress1, FloatSupplier alpha) {
			this.message = pButton$Builder0;
			this.onPress = pButton$OnPress1;
			this.alpha = alpha;
		}

		public IrisButton.Builder pos(int pButton$Builder0, int pInt1) {
			this.x = pButton$Builder0;
			this.y = pInt1;
			return this;
		}

		public IrisButton.Builder width(int pButton$Builder0) {
			this.width = pButton$Builder0;
			return this;
		}

		public IrisButton.Builder size(int pButton$Builder0, int pInt1) {
			this.width = pButton$Builder0;
			this.height = pInt1;
			return this;
		}

		public IrisButton.Builder bounds(int pButton$Builder0, int pInt1, int pInt2, int pInt3) {
			return this.pos(pButton$Builder0, pInt1).size(pInt2, pInt3);
		}

		public IrisButton.Builder tooltip(@Nullable Tooltip pButton$Builder0) {
			this.tooltip = pButton$Builder0;
			return this;
		}

		public IrisButton.Builder createNarration(Button.CreateNarration pButton$Builder0) {
			this.createNarration = pButton$Builder0;
			return this;
		}

		public IrisButton build() {
			IrisButton lvButton1 = new IrisButton(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration, this.alpha);
			lvButton1.setTooltip(this.tooltip);
			lvButton1.active = true;
			return lvButton1;
		}
	}
}
