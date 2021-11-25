package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ShaderPackOptionList extends IrisObjectSelectionList<ShaderPackOptionList.BaseEntry> {
	private final NavigationController navigation;
	private OptionMenuContainer container;

	public ShaderPackOptionList(NavigationController navigation, ShaderPack pack, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 24);
		this.navigation = navigation;

		applyShaderPack(pack);
	}

	public void applyShaderPack(ShaderPack pack) {
		this.container = pack.getShaderPackOptions().getMenuContainer();
	}

	public void refresh() {
		this.clearEntries();
		this.setScrollAmount(0);
		container.applyToMinecraftGui(this, navigation);
	}

	@Override
	public int getRowWidth() {
		return Math.min(360, width - 50);
	}

	public void addHeader(Component text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.navigation, text, backButton));
	}

	public void addWidgets(int columns, List<AbstractShaderPackOptionWidget> elements) {
		List<AbstractShaderPackOptionWidget> row = new ArrayList<>();

		for (AbstractShaderPackOptionWidget element : elements) {
			row.add(element);

			if (row.size() >= columns) {
				this.addEntry(new OptionRowEntry(this.navigation, row));
				row = new ArrayList<>();
			}
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

		private static final int BACK_BUTTON_WIDTH = 42;
		private static final int BACK_BUTTON_HEIGHT = 16;

		private final boolean hasBackButton;
		private final Component text;

		private int cachedPosX;
		private int cachedPosY;

		public HeaderEntry(NavigationController navigation, Component text, boolean hasBackButton) {
			super(navigation);

			this.hasBackButton = hasBackButton;
			this.text = text;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			// Draw header text
			drawCenteredString(poseStack, Minecraft.getInstance().font, text, x + (int)(entryWidth * 0.5), y + 5, 0xFFFFFF);

			// Draw back button if present
			if (hasBackButton) {
				GuiUtil.bindIrisWidgetsTexture();
				GuiUtil.drawButton(poseStack, x, y, BACK_BUTTON_WIDTH, BACK_BUTTON_HEIGHT,
						hovered && mouseX < x + BACK_BUTTON_WIDTH && mouseY < y + BACK_BUTTON_HEIGHT,
						false);

				drawCenteredString(poseStack, Minecraft.getInstance().font, BACK_BUTTON_TEXT, x + (int)(0.5 * BACK_BUTTON_WIDTH), y + 4, 0xFFFFFF);
			}

			this.cachedPosX = x;
			this.cachedPosY = y;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (hasBackButton && button == GLFW.GLFW_MOUSE_BUTTON_1 && mouseX < cachedPosX + BACK_BUTTON_WIDTH && mouseY < cachedPosY + BACK_BUTTON_HEIGHT) {
				this.navigation.back();
				GuiUtil.playButtonClickSound();
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

	public static class OptionRowEntry extends BaseEntry {
		private final List<AbstractShaderPackOptionWidget> widgets;

		private int cachedWidth;
		private int cachedPosX;

		public OptionRowEntry(NavigationController navigation, List<AbstractShaderPackOptionWidget> widgets) {
			super(navigation);

			this.widgets = widgets;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			// The amount of space widgets will occupy, excluding margins. Will be divided up between widgets.
			int totalWidthWithoutMargins = entryWidth - (2 * (widgets.size() - 1));

			totalWidthWithoutMargins -= 3; // Centers it for some reason

			// Width of a single widget
			float singleWidgetWidth = (float) totalWidthWithoutMargins / widgets.size();

			for (int i = 0; i < widgets.size(); i++) {
				AbstractShaderPackOptionWidget widget = widgets.get(i);
				widget.render(poseStack, x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, entryHeight + 2, mouseX, mouseY, tickDelta, hovered && (getHoveredWidget(mouseX) == i));
			}

			this.cachedWidth = entryWidth;
			this.cachedPosX = x;
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
