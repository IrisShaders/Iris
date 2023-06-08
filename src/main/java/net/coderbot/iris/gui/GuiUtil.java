package net.coderbot.iris.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;


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
	public static final ResourceLocation IRIS_WIDGETS_TEX = new ResourceLocation("iris", "textures/gui/widgets.png");
	private static final Component ELLIPSIS = Component.literal("...");

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
	 * Draws a translucent black panel
	 * with a light border.
	 *
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 * @param width The width of the panel
	 * @param height The height of the panel
	 */
	public static void drawPanel(PoseStack poseStack, int x, int y, int width, int height) {
		int borderColor = 0xDEDEDEDE;
		int innerColor = 0xDE000000;

		// Top border section
		GuiComponent.fill(poseStack, x, y, x + width, y + 1, borderColor);
		// Bottom border section
		GuiComponent.fill(poseStack, x, (y + height) - 1, x + width, y + height, borderColor);
		// Left border section
		GuiComponent.fill(poseStack, x, y + 1, x + 1, (y + height) - 1, borderColor);
		// Right border section
		GuiComponent.fill(poseStack, (x + width) - 1, y + 1, x + width, (y + height) - 1, borderColor);
		// Inner section
		GuiComponent.fill(poseStack, x + 1, y + 1, (x + width) - 1, (y + height) - 1, innerColor);
	}

	/**
	 * Draws a text with a panel behind it.
	 *
	 * @param text The text component to draw
	 * @param x The x position of the panel
	 * @param y The y position of the panel
	 */
	public static void drawTextPanel(Font font, PoseStack poseStack, Component text, int x, int y) {
		drawPanel(poseStack, x, y, font.width(text) + 8, 16);
		font.drawShadow(poseStack, text, x + 4, y + 4, 0xFFFFFF);
	}

	/**
	 * Shorten a text to a specific length, adding an ellipsis (...)
	 * to the end if shortened.
	 *
	 * Text may lose formatting.
	 *
	 * @param font Font to use for determining the width of text
	 * @param text Text to shorten
	 * @param width Width to shorten text to
	 * @return a shortened text
	 */
	public static MutableComponent shortenText(Font font, MutableComponent text, int width) {
		if (font.width(text) > width) {
			return Component.literal(font.plainSubstrByWidth(text.getString(), width - font.width(ELLIPSIS))).append(ELLIPSIS).setStyle(text.getStyle());
		}
		return text;
	}

	/**
	 * Creates a new translated text, if a translation
	 * is present. If not, will return the default text
	 * component passed.
	 *
	 * @param defaultText Default text to use if no translation is found
	 * @param translationDesc Translation key to try and use
	 * @param format Formatting arguments for the translated text, if created
	 * @return the translated text if found, otherwise the default provided
	 */
	public static MutableComponent translateOrDefault(MutableComponent defaultText, String translationDesc, Object ... format) {
		if (I18n.exists(translationDesc)) {
			return Component.translatable(translationDesc, format);
		}
		return defaultText;
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

	/**
	 * A class representing a section of a
	 * texture, to be easily drawn in GUIs.
	 */
	public static class Icon {
		public static final Icon SEARCH = new Icon(0, 0, 7, 8);
		public static final Icon CLOSE = new Icon(7, 0, 5, 6);
		public static final Icon REFRESH = new Icon(12, 0, 10, 10);
		public static final Icon EXPORT = new Icon(22, 0, 7, 8);
		public static final Icon EXPORT_COLORED = new Icon(29, 0, 7, 8);
		public static final Icon IMPORT = new Icon(22, 8, 7, 8);
		public static final Icon IMPORT_COLORED = new Icon(29, 8, 7, 8);

		private final int u;
		private final int v;
		private final int width;
		private final int height;

		public Icon(int u, int v, int width, int height) {
			this.u = u;
			this.v = v;
			this.width = width;
			this.height = height;
		}

		/**
		 * Draws this icon to the screen at the specified coordinates.
		 *
		 * @param x The x position to draw the icon at (left)
		 * @param y The y position to draw the icon at (top)
		 */
		public void draw(PoseStack poseStack, int x, int y) {
			// Sets RenderSystem to use solid white as the tint color for blend mode (1.16), and enables blend mode
			RenderSystem.enableBlend();

			// Draw the texture to the screen
			GuiComponent.blit(poseStack, x, y, u, v, width, height, 256, 256);
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}
}
