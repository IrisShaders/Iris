package net.coderbot.iris.gui.element;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.FileDialogUtil;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.NavigationController;
import net.coderbot.iris.gui.element.widget.AbstractElementWidget;
import net.coderbot.iris.gui.element.widget.OptionMenuConstructor;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.menu.OptionMenuContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ShaderPackOptionList extends IrisContainerObjectSelectionList<ShaderPackOptionList.BaseEntry> {
	private final List<AbstractElementWidget<?>> elementWidgets = new ArrayList<>();
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
		this.container = pack.getMenuContainer();
	}

	public void rebuild() {
		this.clearEntries();
		this.setScrollAmount(0);
		OptionMenuConstructor.constructAndApplyToScreen(this.container, this.screen, this, navigation);
	}

	public void refresh() {
		this.elementWidgets.forEach(widget -> widget.init(this.screen, this.navigation));
	}

	@Override
	public int getRowWidth() {
		return Math.min(400, width - 12);
	}

	public void addHeader(Component text, boolean backButton) {
		this.addEntry(new HeaderEntry(this.screen, this.navigation, text, backButton));
	}

	public void addWidgets(int columns, List<AbstractElementWidget<?>> elements) {
		this.elementWidgets.addAll(elements);

		List<AbstractElementWidget<?>> row = new ArrayList<>();
		for (AbstractElementWidget<?> element : elements) {
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

	public abstract static class BaseEntry extends ContainerObjectSelectionList.Entry<BaseEntry> {
		protected final NavigationController navigation;

		protected BaseEntry(NavigationController navigation) {
			this.navigation = navigation;
		}
	}

	public static class HeaderEntry extends BaseEntry {
		public static final Component BACK_BUTTON_TEXT = Component.literal("< ").append(Component.translatable("options.iris.back").withStyle(ChatFormatting.ITALIC));
		public static final MutableComponent RESET_BUTTON_TEXT_INACTIVE = Component.translatable("options.iris.reset").withStyle(ChatFormatting.GRAY);
		public static final MutableComponent RESET_BUTTON_TEXT_ACTIVE = Component.translatable("options.iris.reset").withStyle(ChatFormatting.YELLOW);

		public static final MutableComponent RESET_HOLD_SHIFT_TOOLTIP = Component.translatable("options.iris.reset.tooltip.holdShift").withStyle(ChatFormatting.GOLD);
		public static final MutableComponent RESET_TOOLTIP = Component.translatable("options.iris.reset.tooltip").withStyle(ChatFormatting.RED);
		public static final MutableComponent IMPORT_TOOLTIP = Component.translatable("options.iris.importSettings.tooltip")
				.withStyle(style -> style.withColor(TextColor.fromRgb(0x4da6ff)));
		public static final MutableComponent EXPORT_TOOLTIP = Component.translatable("options.iris.exportSettings.tooltip")
				.withStyle(style -> style.withColor(TextColor.fromRgb(0xfc7d3d)));

		private static final int MIN_SIDE_BUTTON_WIDTH = 42;
		private static final int BUTTON_HEIGHT = 16;

		private final ShaderPackScreen screen;
		private final @Nullable IrisElementRow backButton;
		private final IrisElementRow utilityButtons = new IrisElementRow();
		private final IrisElementRow.TextButtonElement resetButton;
		private final IrisElementRow.IconButtonElement importButton;
		private final IrisElementRow.IconButtonElement exportButton;
		private final Component text;

		public HeaderEntry(ShaderPackScreen screen, NavigationController navigation, Component text, boolean hasBackButton) {
			super(navigation);

			if (hasBackButton) {
				this.backButton = new IrisElementRow().add(
						new IrisElementRow.TextButtonElement(BACK_BUTTON_TEXT, this::backButtonClicked),
						Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getInstance().font.width(BACK_BUTTON_TEXT) + 8)
				);
			} else {
				this.backButton = null;
			}

			this.resetButton = new IrisElementRow.TextButtonElement(
					RESET_BUTTON_TEXT_INACTIVE, this::resetButtonClicked);
			this.importButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.IMPORT, GuiUtil.Icon.IMPORT_COLORED, this::importSettingsButtonClicked);
			this.exportButton = new IrisElementRow.IconButtonElement(
					GuiUtil.Icon.EXPORT, GuiUtil.Icon.EXPORT_COLORED, this::exportSettingsButtonClicked);

			this.utilityButtons
					.add(this.importButton, 15)
					.add(this.exportButton, 15)
					.add(this.resetButton, Math.max(MIN_SIDE_BUTTON_WIDTH, Minecraft.getInstance().font.width(RESET_BUTTON_TEXT_INACTIVE) + 8));

			this.screen = screen;
			this.text = text;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			// Draw dividing line
			fill(poseStack, x - 3, (y + entryHeight) - 2, x + entryWidth, (y + entryHeight) - 1, 0x66BEBEBE);

			Font font = Minecraft.getInstance().font;

			// Draw header text
			drawCenteredString(poseStack, font, text, x + (int)(entryWidth * 0.5), y + 5, 0xFFFFFF);

			GuiUtil.bindIrisWidgetsTexture();

			// Draw back button if present
			if (this.backButton != null) {
				backButton.render(poseStack, x, y, BUTTON_HEIGHT, mouseX, mouseY, tickDelta, hovered);
			}

			boolean shiftDown = Screen.hasShiftDown();

			// Set the appearance of the reset button
			this.resetButton.disabled = !shiftDown && !resetButton.isFocused();
			this.resetButton.text = !resetButton.disabled ? RESET_BUTTON_TEXT_ACTIVE : RESET_BUTTON_TEXT_INACTIVE;

			// Draw the utility buttons
			this.utilityButtons.renderRightAligned(poseStack, (x + entryWidth) - 3, y, BUTTON_HEIGHT, mouseX, mouseY, tickDelta, hovered);

			// Draw the reset button's tooltip
			if (this.resetButton.isHovered() || this.resetButton.isFocused()) {
				Component tooltip = !resetButton.disabled ? RESET_TOOLTIP : RESET_HOLD_SHIFT_TOOLTIP;
				queueBottomRightAnchoredTooltip(poseStack, mouseX, mouseY, font, tooltip);
			}
			// Draw the import/export button tooltips
			if (this.importButton.isHovered() || this.importButton.isFocused()) {
				queueBottomRightAnchoredTooltip(poseStack, mouseX, mouseY, font, IMPORT_TOOLTIP);
			}
			if (this.exportButton.isHovered() || this.exportButton.isFocused()) {
				queueBottomRightAnchoredTooltip(poseStack, mouseX, mouseY, font, EXPORT_TOOLTIP);
			}
		}

		private void queueBottomRightAnchoredTooltip(PoseStack poseStack, int x, int y, Font font, Component text) {
			ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() -> GuiUtil.drawTextPanel(
					font, poseStack, text,
					x - (font.width(text) + 10), y - 16
			));
		}

		@Override
		public List<? extends GuiEventListener> children() {
			if (backButton != null)
				return ImmutableList.copyOf(Iterables.concat(utilityButtons.children(), backButton.children()));
			return ImmutableList.copyOf(utilityButtons.children());
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			boolean backButtonResult = backButton != null && backButton.mouseClicked(mouseX, mouseY, button);
			boolean utilButtonResult = utilityButtons.mouseClicked(mouseX, mouseY, button);

			return backButtonResult || utilButtonResult;
		}

		@Override
		public boolean keyPressed(int keycode, int scancode, int modifiers) {
			if (backButton != null && backButton.keyPressed(keycode, scancode, modifiers)) {
				return true;
			}

			return utilityButtons.keyPressed(keycode, scancode, modifiers);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return ImmutableList.of();
		}

		private boolean backButtonClicked(IrisElementRow.TextButtonElement button) {
			this.navigation.back();
			GuiUtil.playButtonClickSound();

			return true;
		}

		private boolean resetButtonClicked(IrisElementRow.TextButtonElement button) {
			if (Screen.hasShiftDown()) {
				Iris.resetShaderPackOptionsOnNextReload();
				this.screen.applyChanges();
				GuiUtil.playButtonClickSound();

				return true;
			}

			return false;
		}

		private boolean importSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (Minecraft.getInstance().getWindow().isFullscreen()) {
				this.screen.displayNotification(
					Component.translatable("options.iris.mustDisableFullscreen")
						.withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
				return false;
			}

			final ShaderPackScreen originalScreen = this.screen; // Also used to prevent invalid state

			FileDialogUtil.fileSelectDialog(
					FileDialogUtil.DialogType.OPEN, "Import Shader Settings from File",
					Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
					 "Shader Pack Settings (.txt)", "*.txt")
			.whenComplete((path, err) -> {
				if (err != null) {
					Iris.logger.error("Error selecting shader settings from file", err);

					return;
				}

				if (Minecraft.getInstance().screen == originalScreen) {
					path.ifPresent(originalScreen::importPackOptions);
				}
			});

			return true;
		}

		private boolean exportSettingsButtonClicked(IrisElementRow.IconButtonElement button) {
			GuiUtil.playButtonClickSound();

			// Invalid state to be in
			if (!Iris.getCurrentPack().isPresent()) {
				return false;
			}

			// Displaying a dialog when the game is full-screened can cause severe issues
			// https://github.com/IrisShaders/Iris/issues/1258
			if (Minecraft.getInstance().getWindow().isFullscreen()) {
				this.screen.displayNotification(
					Component.translatable("options.iris.mustDisableFullscreen")
						.withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
				return false;
			}

			FileDialogUtil.fileSelectDialog(
					FileDialogUtil.DialogType.SAVE, "Export Shader Settings to File",
					Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt"),
					"Shader Pack Settings (.txt)", "*.txt")
			.whenComplete((path, err) -> {
				if (err != null) {
					Iris.logger.error("Error selecting file to export shader settings", err);

					return;
				}

				path.ifPresent(p -> {
					Properties toSave = new Properties();

					// Dirty way of getting the currently applied settings as a Properties, directly
					// opens and copies out of the saved settings file if it is present
					Path sourceTxtPath = Iris.getShaderpacksDirectory().resolve(Iris.getCurrentPackName() + ".txt");
					if (Files.exists(sourceTxtPath)) {
						try (InputStream in = Files.newInputStream(sourceTxtPath)) {
							toSave.load(in);
						} catch (IOException ignored) {}
					}

					// Save properties to user determined file
					try (OutputStream out = Files.newOutputStream(p)) {
						toSave.store(out, null);
					} catch (IOException e) {
						Iris.logger.error("Error saving properties to \"" + p + "\"", e);
					}
				});
			});

			return true;
		}
	}

	public static class ElementRowEntry extends BaseEntry {
		private final List<AbstractElementWidget<?>> widgets;
		private final ShaderPackScreen screen;

		private int cachedWidth;
		private int cachedPosX;

		public ElementRowEntry(ShaderPackScreen screen, NavigationController navigation, List<AbstractElementWidget<?>> widgets) {
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
				AbstractElementWidget<?> widget = widgets.get(i);
				boolean widgetHovered = (hovered && (getHoveredWidget(mouseX) == i)) || getFocused() == widget;

				widget.bounds = new ScreenRectangle(x + (int)((singleWidgetWidth + 2) * i), y, (int) singleWidgetWidth, entryHeight + 2);
				widget.render(poseStack, mouseX, mouseY, tickDelta, widgetHovered);

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

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return ImmutableList.copyOf(widgets);
		}

		@Override
		public @NotNull List<? extends NarratableEntry> narratables() {
			return ImmutableList.copyOf(widgets);
		}
	}
}
