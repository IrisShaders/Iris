package net.coderbot.iris.gui.element;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderPackListWidget extends IrisScreenEntryListWidget<ShaderPackListWidget.BaseEntry> {
	public static final List<String> BUILTIN_PACKS = ImmutableList.of();

	private static final Text PACK_LIST_LABEL = new TranslatableText("pack.iris.list.label").formatted(Formatting.ITALIC, Formatting.GRAY);
	private static final Text SHADERS_DISABLED_LABEL = new TranslatableText("options.iris.shaders.disabled");
	private static final Text SHADERS_ENABLED_LABEL = new TranslatableText("options.iris.shaders.enabled");

	private final EnableShadersButtonEntry enableShadersButton = new EnableShadersButtonEntry(Iris.getIrisConfig().areShadersEnabled());

	public ShaderPackListWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right) {
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

			Path path = Iris.SHADERPACKS_DIRECTORY;
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
		for (int i = 0; i < getEntryCount(); i++) {
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

	public static abstract class BaseEntry extends AlwaysSelectedEntryListWidget.Entry<BaseEntry> {
		protected BaseEntry() {}
	}

	public static class ShaderPackEntry extends BaseEntry {
		private final String packName;
		private final ShaderPackListWidget list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackListWidget list, String packName) {
			this.packName = packName;
			this.list = list;
			this.index = index;
		}

		public boolean isSelected() {
			return list.getSelectedOrNull() == this;
		}

		public String getPackName() {
			return packName;
		}

		// Appears to be some accessibility thing
		@Override
		public Text method_37006() {
			return new TranslatableText("narrator.select", packName);
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			int color = 0xFFFFFF;
			String name = packName;

			boolean shadersEnabled = list.getEnableShadersButton().enabled;

			if (textRenderer.getWidth(new LiteralText(name).formatted(Formatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = textRenderer.trimToWidth(name, this.list.getRowWidth() - 8) + "...";
			}

			MutableText text = new LiteralText(name);

			if (shadersEnabled && this.isMouseOver(mouseX, mouseY)) {
				text = text.formatted(Formatting.BOLD);
			}

			if (this.isSelected()) {
				color = 0xFFF263;
			}

			if (!shadersEnabled) {
				color = 0xA2A2A2;
			}

			drawCenteredText(matrices, textRenderer, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
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
		private final Text label;

		public LabelEntry(Text label) {
			this.label = label;
		}

		// Appears to be some accessibility thing
		@Override
		public Text method_37006() {
			return label;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xC2C2C2);
		}
	}

	public static class EnableShadersButtonEntry extends BaseEntry {
		public boolean enabled;

		public EnableShadersButtonEntry(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			GuiUtil.bindIrisWidgetsTexture();

			GuiUtil.drawButton(matrices, x - 2, y - 3, entryWidth, 18, hovered, false);

			Text label = this.enabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL;

			drawCenteredText(matrices, MinecraftClient.getInstance().textRenderer, label, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, 0xFFFFFF);
		}

		// Appears to be some accessibility thing
		@Override
		public Text method_37006() {
			return new TranslatableText("narration.button", this.enabled ? SHADERS_ENABLED_LABEL : SHADERS_DISABLED_LABEL);
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
