package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ShaderPackOptionList extends IrisObjectSelectionList<ShaderPackOptionList.BaseEntry> {
	private final ShaderPackScreen screen;
	private final NavigationController navigation;
	private OptionMenuContainer container;

	public ShaderPackOptionList(ShaderPackScreen screen, NavigationController navigation, ShaderPack pack, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 24);
		this.navigation = navigation;
		this.screen = screen;

		applyShaderPack(pack);
	}

	public void applyShaderPack(ShaderPack pack) {
		this.container = pack.getShaderPackOptions().getMenuContainer();
	}

	public void refresh() {
		this.clearEntries();
		this.setScrollAmount(0);
		container.applyToMinecraftGui(this.screen, this, navigation);
	}

	@Override
	public int getRowWidth() {
		return Math.min(400, width - 12);
	}

	public void addHeader(Component text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.screen, this.navigation, text, backButton));
	}

	public void addWidgets(int columns, List<AbstractElementWidget> elements) {
		List<AbstractElementWidget> row = new ArrayList<>();

		for (AbstractElementWidget element : elements) {
			row.add(element);

			if (row.size() >= columns) {
				this.addEntry(new ElementRowEntry(screen, this.navigation, row));
				row = new ArrayList<>(); // Clearing the list would affect the row entry created above
			}
		}

		if (row.size() > 0) {
			while (row.size() < columns) {
				row.add(AbstractElementWidget.EMPTY);
			}

			this.addEntry(new ElementRowEntry(screen, this.navigation, row));
		}
	}

	public NavigationController getNavigation() {
		return navigation;
	}

	public abstract static class BaseEntry extends ObjectSelectionList.Entry<BaseEntry> {
		protected final NavigationController navigation;

		protected BaseEntry(NavigationController navigation) {
			this.navigation = navigation;
		}
	}

	public static class HeaderEntry extends BaseEntry {
		public static final Component BACK_BUTTON_TEXT = new TextComponent("< ").append(new TranslatableComponent("options.iris.back").withStyle(ChatFormatting.ITALIC));
		public static final MutableComponent RESET_BUTTON_TEXT_INACTIVE = new TranslatableComponent("options.iris.reset").withStyle(ChatFormatting.GRAY);
		public static final MutableComponent RESET_BUTTON_TEXT_ACTIVE = new TranslatableComponent("options.iris.reset").withStyle(ChatFormatting.YELLOW);

		public static final MutableComponent RESET_HOLD_SHIFT_TOOLTIP = new TranslatableComponent("options.iris.reset.tooltip.holdShift").withStyle(ChatFormatting.GOLD);
		public static final MutableComponent RESET_TOOLTIP = new TranslatableComponent("options.iris.reset.tooltip").withStyle(ChatFormatting.RED);

		private static final int SIDE_BUTTON_WIDTH = 42;
		private static final int SIDE_BUTTON_HEIGHT = 16;

		private final ShaderPackScreen screen;
		private final boolean hasBackButton;
		private final Component text;

		private int cachedPosX;
		private int cachedPosY;
		private int cachedWidth;

		public HeaderEntry(ShaderPackScreen screen, NavigationController navigation, Component text, boolean hasBackButton) {
			super(navigation);

			this.screen = screen;
			this.hasBackButton = hasBackButton;
			this.text = text;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.cachedPosX = x;
			this.cachedPosY = y;
			this.cachedWidth = entryWidth;

			// Draw dividing line
			fill(poseStack, x - 3, (y + entryHeight) - 2, x + entryWidth, (y + entryHeight) - 1, 0x66BEBEBE);

			Font font = Minecraft.getInstance().font;

			// Draw header text
			drawCenteredString(poseStack, font, text, x + (int)(entryWidth * 0.5), y + 5, 0xFFFFFF);

			GuiUtil.bindIrisWidgetsTexture();

			// Draw back button if present
			if (hasBackButton) {
				GuiUtil.drawButton(poseStack,
						x, y,
						SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT,
						hovered && mouseX < x + SIDE_BUTTON_WIDTH && mouseY < y + SIDE_BUTTON_HEIGHT,
						false);

				drawCenteredString(poseStack, font, BACK_BUTTON_TEXT, x + (int)(0.5 * SIDE_BUTTON_WIDTH), y + 4, 0xFFFFFF);
			}

			boolean shiftDown = Screen.hasShiftDown();
			boolean resetButtonHovered = hovered && mouseX > (x + (entryWidth - 3)) - SIDE_BUTTON_WIDTH && mouseY < y + SIDE_BUTTON_HEIGHT;

			GuiUtil.bindIrisWidgetsTexture();

			// Draw reset button
			GuiUtil.drawButton(poseStack,
					(x + (entryWidth - 3)) - SIDE_BUTTON_WIDTH, y,
					SIDE_BUTTON_WIDTH, SIDE_BUTTON_HEIGHT,
					resetButtonHovered,
					!shiftDown);

			drawCenteredString(poseStack,
					font,
					shiftDown ? RESET_BUTTON_TEXT_ACTIVE : RESET_BUTTON_TEXT_INACTIVE,
					(x + (entryWidth - 3)) - (int)(0.5 * SIDE_BUTTON_WIDTH), y + 4,
					0xFFFFFF);

			// Draw reset button tooltip
			if (resetButtonHovered) {
				Component tooltip = shiftDown ? RESET_TOOLTIP : RESET_HOLD_SHIFT_TOOLTIP;
				ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
						font, poseStack, tooltip,
						mouseX - (font.width(tooltip) + 10), mouseY - 16
						));
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
				if (hasBackButton && mouseX < cachedPosX + SIDE_BUTTON_WIDTH && mouseY < cachedPosY + SIDE_BUTTON_HEIGHT) {
					return backButtonClicked(mouseX, mouseY, button);
				}
				if (mouseX > (cachedPosX + (cachedWidth - 3)) - SIDE_BUTTON_WIDTH && mouseY < cachedPosY + SIDE_BUTTON_HEIGHT) {
					return resetButtonClicked(mouseX, mouseY, button);
				}
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}

		private boolean backButtonClicked(double mouseX, double mouseY, int button) {
			this.navigation.back();
			GuiUtil.playButtonClickSound();

			return true;
		}

		private boolean resetButtonClicked(double mouseX, double mouseY, int button) {
			if (Screen.hasShiftDown()) {
				Iris.resetShaderPackOptionsOnNextReload();
				this.screen.applyChanges();
				GuiUtil.playButtonClickSound();

				return true;
			}

			return false;
		}
	}

	public static class ElementRowEntry extends BaseEntry {
		private final List<AbstractElementWidget> widgets;
		private final ShaderPackScreen screen;

		private int cachedWidth;
		private int cachedPosX;

		public ElementRowEntry(ShaderPackScreen screen, NavigationController navigation, List<AbstractElementWidget> widgets) {
			super(navigation);

			this.screen = screen;
			this.widgets = widgets;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.cachedWidth = entryWidth;
			this.cachedPosX = x;

			// The amount of space widgets will occupy, excluding margins. Will be divided up between widgets.
			int totalWidthWithoutMargins = entryWidth - (2 * (widgets.size() - 1));

			totalWidthWithoutMargins -= 3; // Centers it for some reason

			// Width of a single widget
			float singleWidgetWidth = (float) totalWidthWithoutMargins / widgets.size();

			for (int i = 0; i < widgets.size(); i++) {
				AbstractElementWidget widget = widgets.get(i);
				boolean widgetHovered = hovered && (getHoveredWidget(mouseX) == i);
				widget.render(poseStack, x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, entryHeight + 2, mouseX, mouseY, tickDelta, widgetHovered);

				screen.setElementHoveredStatus(widget, widgetHovered);
			}
		}

		public int getHoveredWidget(int mouseX) {
			float positionAcrossWidget = ((float) Mth.clamp(mouseX - cachedPosX, 0, cachedWidth)) / cachedWidth;

			return Mth.clamp((int) Math.floor(widgets.size() * positionAcrossWidget), 0, widgets.size() - 1);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return this.widgets.get(getHoveredWidget((int) mouseX)).mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return this.widgets.get(getHoveredWidget((int) mouseX)).mouseReleased(mouseX, mouseY, button);
		}
	}
}
