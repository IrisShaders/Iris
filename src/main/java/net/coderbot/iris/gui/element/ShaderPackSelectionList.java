package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.screen.ShaderPackScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;

public class ShaderPackSelectionList extends IrisObjectSelectionList<ShaderPackSelectionList.BaseEntry> {
	private static final Component PACK_LIST_LABEL = new TranslatableComponent("pack.iris.list.label").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
	private static final Component SHADERS_DISABLED_LABEL = new TranslatableComponent("options.iris.shaders.disabled");
	private static final Component SHADERS_ENABLED_LABEL = new TranslatableComponent("options.iris.shaders.enabled");

	private final TopButtonRowEntry topButtonRow;
	private ShaderPackEntry applied = null;

	public ShaderPackSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20);
		this.topButtonRow = new TopButtonRowEntry(this, Iris.getIrisConfig().areShadersEnabled());

		refresh();
	}

	@Override
	public int getRowWidth() {
		return Math.min(308, width - 50);
	}

	@Override
	protected int getRowTop(int index) {
		return super.getRowTop(index) + 2;
	}

	public void refresh() {
		this.clearEntries();

		Collection<String> names;

		try {
			names = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI");
			Iris.logger.catching(e);

			// Not translating this since it's going to be seen very rarely,
			// We're just trying to get more information on a seemingly untraceable bug:
			// - https://github.com/IrisShaders/Iris/issues/785
			this.addLabelEntries(
					TextComponent.EMPTY,
					new TextComponent("There was an error reading your shaderpacks directory")
							.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
					TextComponent.EMPTY,
					new TextComponent("Check your logs for more information."),
					new TextComponent("Please file an issue report including a log file."),
					new TextComponent("If you are able to identify the file causing this, " +
											 "please include it in your report as well."),
					new TextComponent("Note that this might be an issue with folder " +
											 "permissions; ensure those are correct first.")
			);

			return;
		}

		this.addEntry(topButtonRow);

		// Only show the enable / disable shaders button if the user has added a shader pack. Otherwise, the button
		// doesn't really make sense.
		topButtonRow.showEnableShadersButton = names.size() > 0;

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
				setApplied(entry);
			}
		});

		this.addEntry(entry);
	}

	public void addLabelEntries(Component ... lines) {
		for (Component text : lines) {
			this.addEntry(new LabelEntry(text));
		}
	}

	public void select(String name) {
		for (int i = 0; i < getItemCount(); i++) {
			BaseEntry entry = getEntry(i);

			if (entry instanceof ShaderPackEntry && ((ShaderPackEntry)entry).packName.equals(name)) {
				setSelected(entry);

				return;
			}
		}
	}

	public void setApplied(ShaderPackEntry entry) {
		this.applied = entry;
	}

	public ShaderPackEntry getApplied() {
		return this.applied;
	}

	public TopButtonRowEntry getTopButtonRow() {
		return topButtonRow;
	}

	public static abstract class BaseEntry extends ObjectSelectionList.Entry<BaseEntry> {
		protected BaseEntry() {}
	}

	public static class ShaderPackEntry extends BaseEntry {
		private final String packName;
		private final ShaderPackSelectionList list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackSelectionList list, String packName) {
			this.packName = packName;
			this.list = list;
			this.index = index;
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
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			Font font = Minecraft.getInstance().font;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getTopButtonRow().shadersEnabled;

			if (font.width(new TextComponent(name).withStyle(ChatFormatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = font.plainSubstrByWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			MutableComponent text = new TextComponent(name);

			if (shadersEnabled && this.isMouseOver(mouseX, mouseY)) {
				text = text.withStyle(ChatFormatting.BOLD);
			}

			if (this.isApplied()) {
				color = 0xFFF263;
			}

			if (!shadersEnabled) {
				color = 0xA2A2A2;
			}

			drawCenteredString(poseStack, font, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (list.getTopButtonRow().shadersEnabled && !this.isSelected() && button == 0) {
				this.list.select(this.index);

				return true;
			}

			return false;
		}
	}

	public static class LabelEntry extends BaseEntry {
		private final Component label;

		public LabelEntry(Component label) {
			this.label = label;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xC2C2C2);
		}
	}

	public static class TopButtonRowEntry extends BaseEntry {
		private static final Component REFRESH_SHADER_PACKS_LABEL = new TranslatableComponent("options.iris.refreshShaderPacks").withStyle(style -> style.withColor(TextColor.fromRgb(0x99ceff)));
		private static final int REFRESH_BUTTON_WIDTH = 18;

		private final ShaderPackSelectionList list;

		public boolean showEnableShadersButton = true;
		public boolean shadersEnabled;
		private int cachedButtonDivisionX;

		public TopButtonRowEntry(ShaderPackSelectionList list, boolean shadersEnabled) {
			this.list = list;
			this.shadersEnabled = shadersEnabled;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

			// Cache the x position dividing the enable/disable and refresh button
			this.cachedButtonDivisionX = (x + entryWidth) - (REFRESH_BUTTON_WIDTH + 3);

			if (showEnableShadersButton) {
				// Draw enable/disable button
				GuiUtil.bindIrisWidgetsTexture();
				GuiUtil.drawButton(poseStack, x - 2, y - 3, (entryWidth - REFRESH_BUTTON_WIDTH) - 1, 18, hovered && mouseX < cachedButtonDivisionX, false);

				// Draw enabled/disabled text
				Component label = this.shadersEnabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL;
				drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFFFFF);
			}

			boolean refreshButtonHovered = hovered && mouseX > cachedButtonDivisionX;

			// Draw refresh button
			GuiUtil.bindIrisWidgetsTexture();
			GuiUtil.drawButton(poseStack, (x + entryWidth) - (REFRESH_BUTTON_WIDTH + 2), y - 3, REFRESH_BUTTON_WIDTH, 18, refreshButtonHovered, false);
			GuiUtil.Icon.REFRESH.draw(poseStack, ((x + entryWidth) - REFRESH_BUTTON_WIDTH) + 2, y + 1);

			if (refreshButtonHovered) {
				ShaderPackScreen.TOP_LAYER_RENDER_QUEUE.add(() ->
						GuiUtil.drawTextPanel(Minecraft.getInstance().font, poseStack, REFRESH_SHADER_PACKS_LABEL,
								(mouseX - 8) - Minecraft.getInstance().font.width(REFRESH_SHADER_PACKS_LABEL), mouseY - 16));
			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				if (mouseX < this.cachedButtonDivisionX) {
					if (this.showEnableShadersButton) {
						// Enable/Disable button pressed
						this.shadersEnabled = !this.shadersEnabled;

						GuiUtil.playButtonClickSound();
						return true;
					}
				} else {
					// Refresh button pressed
					this.list.refresh();

					GuiUtil.playButtonClickSound();
					return true;
				}
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
