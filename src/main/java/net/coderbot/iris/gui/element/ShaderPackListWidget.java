package net.coderbot.iris.gui.element;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ShaderPackListWidget extends IrisScreenEntryListWidget<ShaderPackListWidget.ShaderPackEntry> {
	public static final List<String> BUILTIN_PACKS = ImmutableList.of("(internal)");

	public ShaderPackListWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right) {
		super(client, width, height, top, bottom, left, right, 20);
		refresh();
	}

	@Override
	public int getRowWidth() {
		return width - 50;
	}

	@Override
	protected int getRowTop(int index) {
		return super.getRowTop(index) + 2;
	}

	public void refresh() {
		this.clearEntries();
		try {
			Path path = Iris.shaderpacksDirectory;
			int index = -1;

			for (String pack : BUILTIN_PACKS) {
				index++;
				addEntry(index, pack);
			}

			Collection<Path> folders = Files.walk(path, 1).filter(p -> {
				if (Files.isDirectory(p)) {
					return Files.exists(p.resolve("shaders"));
				}
				if (p.toString().endsWith(".zip")) {
					try {
						FileSystem zipSystem = FileSystems.newFileSystem(p, Iris.class.getClassLoader());
						return Files.exists(zipSystem.getPath("shaders"));
					} catch (IOException ignored) {
					}
				}
				return false;
			}).collect(Collectors.toList());

			for (Path folder : folders) {
				String name = folder.getFileName().toString();
				if (!BUILTIN_PACKS.contains(name)) {
					index++;
					addEntry(index, name);
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void addEntry(int index, String name) {
		ShaderPackEntry entry = new ShaderPackEntry(index, this, name);
		if (Iris.getIrisConfig().getShaderPackName().equals(name)) {
			this.setSelected(entry);
		}
		this.addEntry(entry);
	}

	public static class ShaderPackEntry extends AlwaysSelectedEntryListWidget.Entry<ShaderPackEntry> {
		private final String packName;
		private final ShaderPackListWidget list;
		private final int index;

		public ShaderPackEntry(int index, ShaderPackListWidget list, String packName) {
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

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
			int color = 0xFFFFFF;
			String name = packName;
			if (textRenderer.getWidth(new LiteralText(name).formatted(Formatting.BOLD)) > this.list.getRowWidth() - 3) {
				name = textRenderer.trimToWidth(name, this.list.getRowWidth() - 8) + "...";
			}
			MutableText text = new LiteralText(name);
			if (this.isMouseOver(mouseX, mouseY)) {
				text = text.formatted(Formatting.BOLD);
			}
			if (this.isSelected()) {
				color = 0xFFF263;
			}
			drawCenteredText(matrices, textRenderer, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (!this.isSelected() && button == 0) {
				this.list.select(this.index);
				return true;
			}
			return false;
		}
	}
}
