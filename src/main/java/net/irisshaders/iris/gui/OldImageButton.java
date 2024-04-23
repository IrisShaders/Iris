package net.irisshaders.iris.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class OldImageButton extends Button {
	protected final ResourceLocation resourceLocation;
	protected final int xTexStart;
	protected final int yTexStart;
	protected final int yDiffTex;
	protected final int textureWidth;
	protected final int textureHeight;

	public OldImageButton(
		int pImageButton0, int pInt1, int pInt2, int pInt3, int pInt4, int pInt5, ResourceLocation pResourceLocation6, Button.OnPress pButton$OnPress7
	) {
		this(pImageButton0, pInt1, pInt2, pInt3, pInt4, pInt5, pInt3, pResourceLocation6, 256, 256, pButton$OnPress7);
	}

	public OldImageButton(
		int pImageButton0,
		int pInt1,
		int pInt2,
		int pInt3,
		int pInt4,
		int pInt5,
		int pInt6,
		ResourceLocation pResourceLocation7,
		Button.OnPress pButton$OnPress8
	) {
		this(pImageButton0, pInt1, pInt2, pInt3, pInt4, pInt5, pInt6, pResourceLocation7, 256, 256, pButton$OnPress8);
	}

	public OldImageButton(
		int pImageButton0,
		int pInt1,
		int pInt2,
		int pInt3,
		int pInt4,
		int pInt5,
		int pInt6,
		ResourceLocation pResourceLocation7,
		int pInt8,
		int pInt9,
		Button.OnPress pButton$OnPress10
	) {
		this(pImageButton0, pInt1, pInt2, pInt3, pInt4, pInt5, pInt6, pResourceLocation7, pInt8, pInt9, pButton$OnPress10, CommonComponents.EMPTY);
	}

	public OldImageButton(
		int pImageButton0,
		int pInt1,
		int pInt2,
		int pInt3,
		int pInt4,
		int pInt5,
		int pInt6,
		ResourceLocation pResourceLocation7,
		int pInt8,
		int pInt9,
		Button.OnPress pButton$OnPress10,
		Component pComponent11
	) {
		super(pImageButton0, pInt1, pInt2, pInt3, pComponent11, pButton$OnPress10, DEFAULT_NARRATION);
		this.textureWidth = pInt8;
		this.textureHeight = pInt9;
		this.xTexStart = pInt4;
		this.yTexStart = pInt5;
		this.yDiffTex = pInt6;
		this.resourceLocation = pResourceLocation7;
	}

	@Override
	public void renderWidget(GuiGraphics pImageButton0, int pInt1, int pInt2, float pFloat3) {
		this.renderTexture(
			pImageButton0,
			this.resourceLocation,
			this.getX(),
			this.getY(),
			this.xTexStart,
			this.yTexStart,
			this.yDiffTex,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}

	public void renderTexture(
		GuiGraphics pAbstractWidget0,
		ResourceLocation pResourceLocation1,
		int pInt2,
		int pInt3,
		int pInt4,
		int pInt5,
		int pInt6,
		int pInt7,
		int pInt8,
		int pInt9,
		int pInt10
	) {
		int lvInt12 = pInt5;
		if (!this.isActive()) {
			lvInt12 = pInt5 + pInt6 * 2;
		} else if (this.isHoveredOrFocused()) {
			lvInt12 = pInt5 + pInt6;
		}

		RenderSystem.enableDepthTest();
		pAbstractWidget0.blit(pResourceLocation1, pInt2, pInt3, (float) pInt4, (float) lvInt12, pInt7, pInt8, pInt9, pInt10);
	}
}
