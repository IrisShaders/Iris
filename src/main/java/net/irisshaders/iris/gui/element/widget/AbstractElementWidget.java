package net.irisshaders.iris.gui.element.widget;

import net.irisshaders.iris.gui.NavigationController;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.irisshaders.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractElementWidget<T extends OptionMenuElement> implements GuiEventListener, NarratableEntry {
	public static final AbstractElementWidget<OptionMenuElement> EMPTY = new AbstractElementWidget<>(null) {
		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta, boolean hovered) {
		}

		@Override
		public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent pGuiEventListener0) {
			return null;
		}

		@Override
		public @NotNull ScreenRectangle getRectangle() {
			return ScreenRectangle.empty();
		}
	};
	protected final T element;
	public ScreenRectangle bounds = ScreenRectangle.empty();
	private boolean focused;

	public AbstractElementWidget(T element) {
		this.element = element;
	}

	public void init(ShaderPackScreen screen, NavigationController navigation) {
	}

	public abstract void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta, boolean hovered);

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		return false;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		return false;
	}

	@Override
	public boolean keyPressed(int keycode, int scancode, int modifiers) {
		return false;
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
	public ScreenRectangle getRectangle() {
		return bounds;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput p0) {

	}
}
