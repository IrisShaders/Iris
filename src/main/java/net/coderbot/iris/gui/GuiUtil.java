package net.coderbot.iris.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

/**
 * Class serving as abstraction and
 * centralization for common GUI
 * rendering/other code calls.
 *
 * Helps allow for easier portability
 * to Minecraft 1.17 by abstracting
 * some code that will be changed.
 */
public final class GuiUtil {
	private static final ResourceLocation IRIS_WIDGETS_TEX = new ResourceLocation("iris", "textures/gui/widgets.png");

	private GuiUtil() {}

	private static Minecraft client() {
		return Minecraft.getInstance();
	}

	/**
	 * Binds Iris's widgets texture to be
	 * used for succeeding draw calls.
	 */
	public static void bindIrisWidgetsTexture() {
		RenderSystem.setShaderTexture(0, IRIS_WIDGETS_TEX);
	}

	/**
	 * Draws a button. Button textures must be mapped with the
	 * same coordinates as those on the vanilla widgets texture.
	 *
	 * @param x X position of the left of the button
	 * @param y Y position of the top of the button
	 * @param width Width of the button, maximum 398
	 * @param height Height of the button, maximum 20
	 * @param hovered Whether the button is being hovered over with the mouse
	 * @param disabled Whether the button should use the "disabled" texture
	 */
	public static void drawButton(PoseStack poseStack, int x, int y, int width, int height, boolean hovered, boolean disabled) {
		// Create variables for half of the width and height.
		// Will not be exact when width and height are odd, but
		// that case is handled within the draw calls.
		int halfWidth = width / 2;
		int halfHeight = height / 2;

		// V offset for which button texture to use
		int vOffset = disabled ? 46 : hovered ? 86 : 66;

		// Sets RenderSystem to use solid white as the tint color for blend mode, and enables blend mode
		RenderSystem.enableBlend();

		// Sets RenderSystem to be able to use textures when drawing
		// This doesn't do anything on 1.17
		RenderSystem.enableTexture();

		// Top left section
		GuiComponent.blit(poseStack, x, y, 0, vOffset, halfWidth, halfHeight, 256, 256);
		// Top right section
		GuiComponent.blit(poseStack, x + halfWidth, y, 200 - (width - halfWidth), vOffset, width - halfWidth, halfHeight, 256, 256);
		// Bottom left section
		GuiComponent.blit(poseStack, x, y + halfHeight, 0, vOffset + (20 - (height - halfHeight)), halfWidth, height - halfHeight, 256, 256);
		// Bottom right section
		GuiComponent.blit(poseStack, x + halfWidth, y + halfHeight, 200 - (width - halfWidth), vOffset + (20 - (height - halfHeight)), width - halfWidth, height - halfHeight, 256, 256);
	}

	/**
	 * Plays the {@code UI_BUTTON_CLICK} sound event as a
	 * master sound effect.
	 *
	 * Used in non-{@code ButtonWidget} UI elements upon click
	 * or other action.
	 */
	public static void playButtonClickSound() {
		client().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
	}
}
