package net.irisshaders.iris.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.GuiUtil;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.function.Function;

public class ShaderPackSelectionList extends IrisObjectSelectionList<ShaderPackSelectionList.BaseEntry> {
	private static final Component PACK_LIST_LABEL = Component.translatable("pack.iris.list.label").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
	private static final ResourceLocation MENU_LIST_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_background.png");
	private final ShaderPackScreen screen;
	private final TopButtonRowEntry topButtonRow;
	private final WatchService watcher;
	private final WatchKey key;
	private final PinnedEntry downloadButton;
	private boolean keyValid;
	private ShaderPackEntry applied = null;

	public ShaderPackSelectionList(ShaderPackScreen screen, Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, bottom, top + 4, bottom, left, right, 20);
		WatchKey key1;
		WatchService watcher1;

		this.screen = screen;
		this.topButtonRow = new TopButtonRowEntry(this, Iris.getIrisConfig().areShadersEnabled());
		this.downloadButton = new PinnedEntry(Component.literal("Download Shaders"), () -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
			if (bl) {
				Util.getPlatform().openUri("https://modrinth.com/shaders");
			}
			this.minecraft.setScreen(this.screen);
		}, "https://modrinth.com/shaders", true)), this);
		try {
			watcher1 = FileSystems.getDefault().newWatchService();
			key1 = Iris.getShaderpacksDirectory().register(watcher1,
				StandardWatchEventKinds.ENTRY_CREATE,
				StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);
			keyValid = true;
		} catch (IOException e) {
			Iris.logger.error("Couldn't register file watcher!", e);
			watcher1 = null;
			key1 = null;
			keyValid = false;
		}

		this.key = key1;
		this.watcher = watcher1;
		refresh();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isUp()) {
			if (getFocused() == this.children().getFirst()) return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void renderWidget(GuiGraphics pAbstractSelectionList0, int pInt1, int pInt2, float pFloat3) {
		if (keyValid) {
			for (WatchEvent<?> event : key.pollEvents()) {
				if (event.kind() == StandardWatchEventKinds.OVERFLOW) continue;

				refresh();
				break;
			}

			keyValid = key.reset();
		}

		super.renderWidget(pAbstractSelectionList0, pInt1, pInt2, pFloat3);
	}

	public void close() throws IOException {
		if (key != null) {
			key.cancel();
		}

		if (watcher != null) {
			watcher.close();
		}
	}

	@Override
	protected void renderListBackground(GuiGraphics pAbstractSelectionList0) {
		float transition = screen.listTransition.getAsFloat();
		if (transition < 0.02f) return;
		//if (transition < 0.99f) pAbstractSelectionList0.flush();
		//RenderSystem.enableBlend();
		// TODO 1.21.6
		//RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, Math.max(screen.listTransition.getAsFloat(), 0.01f));
		pAbstractSelectionList0.blit(RenderPipelines.GUI_TEXTURED,
			MENU_LIST_BACKGROUND,
			this.getX(), this.getY(), (float)this.getRight(), (float)(this.getBottom() + (int)this.scrollAmount()), this.getWidth(), this.getHeight(), 32, 32
		);
		//if (transition < 0.99f) pAbstractSelectionList0.flush();

		//RenderSystem.disableBlend();
		//RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	protected void renderListSeparators(GuiGraphics pAbstractSelectionList0) {
		float transition = screen.listTransition.getAsFloat();
		if (transition < 0.02f) return;
		//if (transition < 0.99f) pAbstractSelectionList0.flush();
		// TODO 1.21.6
		//RenderSystem.enableBlend();
		//RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, Math.max(screen.listTransition.getAsFloat(), 0.01f));
		int col = ARGB.colorFromFloat(transition, 1.0f, 1.0f, 1.0f);
		pAbstractSelectionList0.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.HEADER_SEPARATOR, this.getX(), this.getY() - 2, 0.0F, 0.0F, this.getWidth(), 2, 32, 2, col);
		pAbstractSelectionList0.blit(RenderPipelines.GUI_TEXTURED, CreateWorldScreen.FOOTER_SEPARATOR, this.getX(), this.getBottom(), 0.0F, 0.0F, this.getWidth(), 2, 32, 2, col);
		//if (transition < 0.99f) pAbstractSelectionList0.flush();
		//RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		//RenderSystem.disableBlend();
	}

	@Override
	public int getRowWidth() {
		return Math.min(308, width - 50);
	}

	@Override
	public int getRowTop(int index) {
		return super.getRowTop(index) + 2;
	}

	public void refresh() {
		this.clearEntries();

		List<String> names;

		try {
			names = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI", e);

			// Not translating this since it's going to be seen very rarely,
			// We're just trying to get more information on a seemingly untraceable bug:
			// - https://github.com/IrisShaders/Iris/issues/785
			this.addLabelEntries(
				Component.empty(),
				Component.literal("There was an error reading your shaderpacks directory")
					.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
				Component.empty(),
				Component.literal("Check your logs for more information."),
				Component.literal("Please file an issue report including a log file."),
				Component.literal("If you are able to identify the file causing this, " +
					"please include it in your report as well."),
				Component.literal("Note that this might be an issue with folder " +
					"permissions; ensure those are correct first.")
			);

			return;
		}

		this.addEntry(topButtonRow);

		if (names.isEmpty()) {
			this.addEntry(downloadButton);
		}

		// Only allow the enable/disable shaders button if the user has
		// added a shader pack. Otherwise, the button will be disabled.
		topButtonRow.allowEnableShadersButton = !names.isEmpty();

		int index = 0;

		for (String name : names) {
			index++;
			addPackEntry(index, name);
		}

		this.addLabelEntries(PACK_LIST_LABEL);
	}

	public void addPackEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);

		Iris.getIrisConfig().getShaderPackName().ifPresent(currentPackName -> {
			if (name.equals(currentPackName)) {
				setSelected(entry);
				setFocused(entry);
				centerScrollOn(entry);
				setApplied(entry);
			}
		});

		this.addEntry(entry);
	}

	public void addLabelEntries(Component... lines) {
		for (Component text : lines) {
			this.addEntry(new LabelEntry(text));
		}
	}

	public void select(String name) {
		for (int i = 0; i < getItemCount(); i++) {
			BaseEntry entry = this.children().get(i);

			if (entry instanceof ShaderPackEntry && ((ShaderPackEntry) entry).packName.equals(name)) {
				setSelected(entry);

				return;
			}
		}
	}

	public ShaderPackEntry getApplied() {
		return this.applied;
	}

	public void setApplied(ShaderPackEntry entry) {
		this.applied = entry;
	}

	public TopButtonRowEntry getTopButtonRow() {
		return topButtonRow;
	}

	public static abstract class BaseEntry extends AbstractSelectionList.Entry<BaseEntry> {
		protected BaseEntry() {
		}
	}

	public static class LabelEntry extends BaseEntry {
		private final Component label;

		public LabelEntry(Component label) {
			this.label = label;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovered, float tickDelta) {
			// Draw dividing line
			int x = getContentX();
			int y = getContentY();
			int entryWidth = getContentWidth();
			int entryHeight = getContentHeight();

			guiGraphics.drawCenteredString(Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFC2C2C2);
		}
	}

	public static class TopButtonRowEntry extends BaseEntry {
		private static final Component NONE_PRESENT_LABEL = Component.translatable("options.iris.shaders.nonePresent").withStyle(ChatFormatting.GRAY);
		private static final Component SHADERS_DISABLED_LABEL = Component.translatable("options.iris.shaders.disabled");
		private static final Component SHADERS_ENABLED_LABEL = Component.translatable("options.iris.shaders.enabled");

		private final ShaderPackSelectionList list;

		public boolean allowEnableShadersButton = true;
		public boolean shadersEnabled;

		public TopButtonRowEntry(ShaderPackSelectionList list, boolean shadersEnabled) {
			this.list = list;
			this.shadersEnabled = shadersEnabled;
		}

		public void setShadersEnabled(boolean shadersEnabled) {
			this.shadersEnabled = shadersEnabled;
			this.list.screen.refreshScreenSwitchButton();
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovered, float tickDelta) {
			// Draw dividing line
			int x = getContentX();
			int y = getContentY();
			int entryWidth = getContentWidth();
			int entryHeight = getContentHeight();

			GuiUtil.bindIrisWidgetsTexture();
			GuiUtil.drawButton(guiGraphics, x - 2, y - 2, entryWidth, entryHeight + 2, isHovered, !allowEnableShadersButton);
			guiGraphics.drawCenteredString(Minecraft.getInstance().font, getEnableDisableLabel(), (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFFFFFFF);
		}

		private Component getEnableDisableLabel() {
			return this.allowEnableShadersButton ? this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL : NONE_PRESENT_LABEL;
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean bl2) {
			if (this.allowEnableShadersButton) {
				setShadersEnabled(!this.shadersEnabled);
				GuiUtil.playButtonClickSound();
				return true;
			}

			return false;
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (event.isConfirmation()) {
				if (this.allowEnableShadersButton) {
					setShadersEnabled(!this.shadersEnabled);
					GuiUtil.playButtonClickSound();
					return true;
				}
			}

			return false;
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent pGuiEventListener0) {
			return (!isFocused()) ? ComponentPath.leaf(this) : null;
		}


		public boolean isFocused() {
			return this.list.getFocused() == this;
		}

		// Renders the label at an offset as to not look misaligned with the rest of the menu
		public static class EnableShadersButtonElement extends IrisElementRow.TextButtonElement {
			private int centerX;

			public EnableShadersButtonElement(Component text, Function<IrisElementRow.TextButtonElement, Boolean> onClick) {
				super(text, onClick);
			}

			@Override
			public void renderLabel(GuiGraphics guiGraphics, int x, int y, int width, int height, int mouseX, int mouseY, float tickDelta, boolean hovered) {
				int textX = this.centerX - (int) (this.font.width(this.text) * 0.5);
				int textY = y + (int) ((height - 8) * 0.5);

				guiGraphics.drawString(this.font, this.text, textX, textY, 0xFFFFFFFF);
			}
		}
	}

	private static class PinnedEntry extends BaseEntry {
		public final boolean allowPressButton = true;
		private final Component label;
		private final Runnable onClick;

		public PinnedEntry(Component label, Runnable onClick, ShaderPackSelectionList list) {
			this.label = label;
			this.onClick = onClick;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovered, float tickDelta) {
			// Draw dividing line
			int x = getContentX();
			int y = getContentY();
			int entryWidth = getContentWidth();
			int entryHeight = getContentHeight();

			GuiUtil.bindIrisWidgetsTexture();
			GuiUtil.drawButton(guiGraphics, x - 2, y - 2, entryWidth, entryHeight + 2, isHovered, !allowPressButton);
			guiGraphics.drawCenteredString(Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFFFFFFF);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean repeat) {
			if (this.allowPressButton) {
				GuiUtil.playButtonClickSound();
				onClick.run();
				return false;
			}

			return false;
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			if (event.isConfirmation()) {
				if (this.allowPressButton) {
					GuiUtil.playButtonClickSound();
					onClick.run();
					return false;
				}
			}

			return false;
		}
	}

	public class ShaderPackEntry extends BaseEntry {
		private final String packName;
		private final ShaderPackSelectionList list;
		private final int index;
		private ScreenRectangle bounds;
		private boolean focused;

		public ShaderPackEntry(int index, ShaderPackSelectionList list, String packName) {
			this.bounds = ScreenRectangle.empty();
			this.packName = packName;
			this.list = list;
			this.index = index;
		}

		@Override
		public ScreenRectangle getRectangle() {
			return bounds;
		}

		public boolean isApplied() {
			return list.getApplied() == this;
		}

		public boolean isSelected() {
			return list.getSelected() == this;
		}

		public String getPackName() {
			return packName;
		}

		@Override
		public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean isHovered, float tickDelta) {
			// Draw dividing line
			int x = getContentX();
			int y = getContentY();
			int entryWidth = getContentWidth();
			int entryHeight = getContentHeight();

			this.bounds = new ScreenRectangle(x, y, entryWidth, entryHeight);
			Font font = Minecraft.getInstance().font;
			int color = 0xFFFFFFFF;
			String name = packName;

			if (isHovered) {
				GuiUtil.bindIrisWidgetsTexture();
				GuiUtil.drawButton(guiGraphics, x - 2, y - 2, entryWidth + 4, entryHeight + 4, isHovered, false);
			}

			boolean shadersEnabled = list.getTopButtonRow().shadersEnabled;

			if (font.width(Component.literal(name).withStyle(ChatFormatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = font.plainSubstrByWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			MutableComponent text = Component.literal(name);

			if (this.isMouseOver(mouseX, mouseY)) {
				text = text.withStyle(ChatFormatting.BOLD);
			}

			if (shadersEnabled && this.isApplied()) {
				color = 0xFFFFF263;
			}

			if (!shadersEnabled && !this.isMouseOver(mouseX, mouseY)) {
				color = 0xFFA2A2A2;
			}

			guiGraphics.drawCenteredString(font, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent event, boolean repeat) {
			// Only do anything on left-click
			if (event.button() != 0) {
				return false;
			}

			return doThing();
		}

		@Override
		public boolean keyPressed(KeyEvent event) {
			// Only do anything on key-press
			if (!event.isConfirmation()) {
				return false;
			}

			return doThing();
		}

		private boolean doThing() {
			boolean didAnything = false;

			// UX: If shaders are disabled, then clicking a shader in the list will also
			//     enable shaders on apply. Previously, it was not possible to select
			//     a pack when shaders were disabled, but this was a source of confusion
			//     - people did not realize that they needed to enable shaders before
			//     selecting a shader pack.
			if (!list.getTopButtonRow().shadersEnabled) {
				list.getTopButtonRow().setShadersEnabled(true);
				didAnything = true;
			}

			if (!this.isSelected()) {
				this.list.select(this.index);
				didAnything = true;
			}

			ShaderPackSelectionList.this.screen.setFocused(ShaderPackSelectionList.this.screen.getBottomRowOption());

			return didAnything;
		}

		@Nullable
		@Override
		public ComponentPath nextFocusPath(FocusNavigationEvent pGuiEventListener0) {
			return (!isFocused()) ? ComponentPath.leaf(this) : null;
		}


		public boolean isFocused() {
			return this.list.getFocused() == this;
		}
	}
}
