package net.coderbot.iris.gui.element;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderPackSelectionList extends IrisObjectSelectionList<ShaderPackSelectionList.BaseEntry> {
	public static final List<String> BUILTIN_PACKS = ImmutableList.of();

	private static final Component PACK_LIST_LABEL = new TranslatableComponent("pack.iris.list.label").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY);
	private static final Component SHADERS_DISABLED_LABEL = new TranslatableComponent("options.iris.shaders.disabled");
	private static final Component SHADERS_ENABLED_LABEL = new TranslatableComponent("options.iris.shaders.enabled");

	private final EnableShadersButtonEntry enableShadersButton = new EnableShadersButtonEntry(Iris.getIrisConfig().areShadersEnabled());

	public ShaderPackSelectionList(Minecraft client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20);

		refresh();
	}

	@Override
	public int getRowWidth() {
		// Temporarily set to only reach a width of up to 312 in order to fit in with
		// the width of the array of buttons at the bottom of the GUI. May be changed
		// in the future if this widget is made to occupy half the screen.
		return Math.min(308, width - 50);
	}

	@Override
	protected int getRowTop(int index) {
		return super.getRowTop(index) + 2;
	}

	public void refresh() {
		this.clearEntries();

		try {
			this.addEntry(enableShadersButton);

			Path path = Iris.getShaderpacksDirectory();
			int index = 0;

			for (String pack : BUILTIN_PACKS) {
				index++;
				addEntry(index, pack);
			}

			Collection<Path> folders = Files.list(path).filter(Iris::isValidShaderpack).collect(Collectors.toList());

			for (Path folder : folders) {
				String name = folder.getFileName().toString();

				if (BUILTIN_PACKS.contains(name)) {
					continue;
				}

				index++;
				addEntry(index, name);
			}

			this.addEntry(new LabelEntry(PACK_LIST_LABEL));
		} catch (Throwable e) {
			Iris.logger.error("Error reading files while constructing selection UI");
			Iris.logger.catching(e);
		}
	}

	public void addEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);

		Iris.getIrisConfig().getShaderPackName().ifPresent(currentPackName -> {
			if (name.equals(currentPackName)) {
				setSelected(entry);
			}
		});

		this.addEntry(entry);
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

	public EnableShadersButtonEntry getEnableShadersButton() {
		return enableShadersButton;
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

		public boolean isSelected() {
			return list.getSelected() == this;
		}

		public String getPackName() {
			return packName;
		}

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return new TranslatableComponent("narrator.select", packName);
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			Font font = Minecraft.getInstance().font;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getEnableShadersButton().enabled;

			if (font.width(new TextComponent(name).withStyle(ChatFormatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = font.plainSubstrByWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			MutableComponent text = new TextComponent(name);

			if (shadersEnabled && this.isMouseOver(mouseX, mouseY)) {
				text = text.withStyle(ChatFormatting.BOLD);
			}

			if (this.isSelected()) {
				color = 0xFFF263;
			}

			if (!shadersEnabled) {
				color = 0xA2A2A2;
			}

			drawCenteredString(poseStack, font, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (list.getEnableShadersButton().enabled && !this.isSelected() && button == 0) {
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

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return label;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xC2C2C2);
		}
	}

	public static class EnableShadersButtonEntry extends BaseEntry {
		public boolean enabled;

		public EnableShadersButtonEntry(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void render(PoseStack poseStack, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			GuiUtil.bindIrisWidgetsTexture();

			GuiUtil.drawButton(poseStack, x - 2, y - 3, entryWidth, 18, hovered, false);

			Component label = this.enabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL;

			drawCenteredString(poseStack, Minecraft.getInstance().font, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFFFFF);
		}

		// Appears to be some accessibility thing
		@Override
		public Component getNarration() {
			return new TranslatableComponent("narration.button", this.enabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				this.enabled = !this.enabled;
				GuiUtil.playButtonClickSound();

				return true;
			}

			return super.mouseClicked(mouseX, mouseY, button);
		}
	}
}
