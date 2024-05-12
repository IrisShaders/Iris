package net.irisshaders.iris.gui.element;

import com.google.common.collect.ImmutableList;
import net.irisshaders.iris.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Intended to make very simple rows of buttons easier to make
 */
public class IrisElementRow {
	private final Map<Element, Integer> elements = new HashMap<>();
	private final List<Element> orderedElements = new ArrayList<>();
	private final int spacing;
	private int x;
	private int y;
	private int width;
	private int height;

	public IrisElementRow(int spacing) {
		this.spacing = spacing;
	}

	public IrisElementRow() {
		this(1);
	}

	/**
	 * Adds an element to the right of this row.
	 *
	 * @param element The element to add
	 * @param width   The width of the element in this row
	 * @return {@code this}, to be used for chaining statements
	 */
	public IrisElementRow add(Element element, int width) {
		if (!this.orderedElements.contains(element)) {
			this.orderedElements.add(element);
		}
		this.elements.put(element, width);

		this.width += width + this.spacing;

		return this;
	}

	/**
	 * Modifies the width of an element.
	 *
	 * @param element The element whose width to modify
	 * @param width   The width to be assigned to the specified element
	 */
	public void setWidth(Element element, int width) {
		if (!this.elements.containsKey(element)) {
			return;
		}

		this.width -= this.elements.get(element) + 2;

		add(element, width);
	}

	/**
	 * Renders the row, with the anchor point being the top left.
	 */
	public void render(GuiGraphics guiGraphics, int x, int y, int height, int mouseX, int mouseY, float tickDelta, boolean rowHovered) {
		this.x = x;
		this.y = y;
		this.height = height;

		int currentX = x;

		for (Element element : this.orderedElements) {
			int currentWidth = this.elements.get(element);

			element.render(guiGraphics, currentX, y, currentWidth, height, mouseX, mouseY, tickDelta,
				rowHovered && sectionHovered(currentX, currentWidth, mouseX, mouseY));

			currentX += currentWidth + this.spacing;
		}
	}

	/**
	 * Renders the row, with the anchor point being the top right.
	 */
	public void renderRightAligned(GuiGraphics guiGraphics, int x, int y, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		render(guiGraphics, x - this.width, y, height, mouseX, mouseY, tickDelta, hovered);
	}

	private boolean sectionHovered(int sectionX, int sectionWidth, double mx, double my) {
		return mx > sectionX && mx < sectionX + sectionWidth &&
			my > this.y && my < this.y + this.height;
	}

	private Optional<Element> getHovered(double mx, double my) {
		int currentX = this.x;

		for (Element element : this.orderedElements) {
			int currentWidth = this.elements.get(element);

			if (sectionHovered(currentX, currentWidth, mx, my)) {
				return Optional.of(element);
			}

			currentX += currentWidth + this.spacing;
		}

		return Optional.empty();
	}

	private Optional<Element> getFocused() {
		return this.orderedElements.stream().filter(Element::isFocused).findFirst();
	}

	public boolean mouseClicked(double mx, double my, int button) {
		return getHovered(mx, my).map(element -> element.mouseClicked(mx, my, button)).orElse(false);
	}

	public boolean mouseReleased(double mx, double my, int button) {
		return getHovered(mx, my).map(element -> element.mouseReleased(mx, my, button)).orElse(false);
	}

	public boolean keyPressed(int keycode, int scancode, int modifiers) {
		return getFocused().map(element -> element.keyPressed(keycode, scancode, modifiers)).orElse(false);
	}

	public List<? extends GuiEventListener> children() {
		return ImmutableList.copyOf(this.orderedElements);
	}

	public static abstract class Element implements GuiEventListener {
		public boolean disabled = false;
		private boolean hovered = false;
		private boolean focused;
		private ScreenRectangle bounds = ScreenRectangle.empty();

		public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
			this.bounds = new ScreenRectangle(x, y, width, height);

			GuiUtil.bindIrisWidgetsTexture();
			GuiUtil.drawButton(guiGraphics, x, y, width, height, isHovered() || isFocused(), this.disabled);

			this.hovered = hovered;
			this.renderLabel(guiGraphics, x, y, width, height, mouseX, mouseY, tickDelta, hovered);
		}

		public abstract void renderLabel(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered);

		public boolean isHovered() {
			return hovered;
		}

		@Override
		public boolean isFocused() {
			return focused;
		}

		@Override
		public void setFocused(boolean focused) {
			this.focused = focused;
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent pGuiEventListener0) {
			return (!isFocused()) ? ComponentPath.leaf(this) : null;
		}

		@Override
		public @NotNull ScreenRectangle getRectangle() {
			return bounds;
		}
	}

	public static abstract class ButtonElement<T extends ButtonElement<T>> extends Element {
		private final Function<T, Boolean> onClick;

		protected ButtonElement(Function<T, Boolean> onClick) {
			this.onClick = onClick;
		}

		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (this.disabled) {
				return false;
			}

			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				return this.onClick.apply((T) this);
			}

			return super.mouseClicked(mx, my, button);
		}

		@Override
		public boolean keyPressed(int keycode, int scancode, int modifiers) {
			if (keycode == GLFW.GLFW_KEY_ENTER) {
				return this.onClick.apply((T) this);
			}
			return false;
		}
	}

	/**
	 * A clickable button element that uses a {@link net.irisshaders.iris.gui.GuiUtil.Icon} as its label.
	 */
	public static class IconButtonElement extends ButtonElement<IconButtonElement> {
		public GuiUtil.Icon icon;
		public GuiUtil.Icon hoveredIcon;

		public IconButtonElement(GuiUtil.Icon icon, GuiUtil.Icon hoveredIcon, Function<IconButtonElement, Boolean> onClick) {
			super(onClick);
			this.icon = icon;
			this.hoveredIcon = hoveredIcon;
		}

		public IconButtonElement(GuiUtil.Icon icon, Function<IconButtonElement, Boolean> onClick) {
			this(icon, icon, onClick);
		}

		@Override
		public void renderLabel(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
			int iconX = x + (int) ((width - this.icon.getWidth()) * 0.5);
			int iconY = y + (int) ((height - this.icon.getHeight()) * 0.5);

			GuiUtil.bindIrisWidgetsTexture();
			if (!this.disabled && (hovered || isFocused())) {
				this.hoveredIcon.draw(guiGraphics, iconX, iconY);
			} else {
				this.icon.draw(guiGraphics, iconX, iconY);
			}
		}
	}

	/**
	 * A clickable button element that uses a text component as its label.
	 */
	public static class TextButtonElement extends ButtonElement<TextButtonElement> {
		protected final Font font;
		public Component text;

		public TextButtonElement(Component text, Function<TextButtonElement, Boolean> onClick) {
			super(onClick);

			this.font = Minecraft.getInstance().font;
			this.text = text;
		}

		@Override
		public void renderLabel(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
			int textX = x + (int) ((width - this.font.width(this.text)) * 0.5);
			int textY = y + (int) ((height - 8) * 0.5);

			guiGraphics.drawString(this.font, this.text, textX, textY, 0xFFFFFF);
		}
	}
}
