package net.coderbot.iris.gui.element.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractElementWidget<T extends OptionMenuElement> implements GuiEventListener, NarratableEntry {
	protected final T element;
	private boolean focused;
	public ScreenRectangle bounds = ScreenRectangle.empty();

	public static final AbstractElementWidget<OptionMenuElement> EMPTY = new AbstractElementWidget<OptionMenuElement>(null) {
		@Override
		public void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta, boolean hovered) {}

		@Override
		public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent pGuiEventListener0) {
			return null;
		}

		@Override
		public @NotNull ScreenRectangle getRectangle() {
			return ScreenRectangle.empty();
		}
	};

	public AbstractElementWidget(T element) {
		this.element = element;
	}

	public void init(ShaderPackScreen screen, NavigationController navigation) {}

	public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float tickDelta, boolean hovered);

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
