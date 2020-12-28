package net.coderbot.iris.gui.element;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import javax.naming.directory.BasicAttributes;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Collectors;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ShaderPackListWidget extends AlwaysSelectedEntryListWidget<ShaderPackListWidget.ShaderPackEntry> {
    private ShaderPackEntry selected;

    public ShaderPackListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int entryHeight) {
        super(minecraftClient, width, height, top, bottom, entryHeight);
        refresh();
    }

    @Override
    public int getRowWidth() {
        return width - 4;
    }

    public void refresh() {
        this.clearEntries();
        try {
            Path path = Iris.getShaderPackDir();
            int index = 0;
            addEntry(index, "(internal)");
            for(Path folder : Files.walk(path, 1).filter(p -> {
                if(Files.isDirectory(p)) {
                    return Files.exists(p.resolve("shaders"));
                } else if(p.toString().endsWith(".zip")) {
                    try {
                        FileSystem zipSystem = FileSystems.newFileSystem(p, Iris.class.getClassLoader());
                        return Files.exists(zipSystem.getPath("shaders"));
                    } catch (IOException ignored) {}
                }
                return false;
            }).collect(Collectors.toList())) {
                String name = folder.getFileName().toString();
                if(!name.equals("(internal)")) {
                    index++;
                    addEntry(index, name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEntry(int index, String name) {
        ShaderPackEntry entry = new ShaderPackEntry(index, this, name);
        if(Iris.getIrisConfig().getShaderPackName().equals(name)) this.selected = entry;
        this.addEntry(entry);
    }

    public ShaderPackEntry getSelected() {
        return selected;
    }

    public void select(int entry) {
        this.selected = this.getEntry(entry);
    }

    public static class ShaderPackEntry extends AlwaysSelectedEntryListWidget.Entry<ShaderPackEntry> {
        private final String packName;
        private final ShaderPackListWidget list;
        private int index;

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
            MutableText text = new LiteralText(packName);
            if(this.isMouseOver(mouseX, mouseY)) text = text.formatted(Formatting.BOLD);
            if(this.isSelected()) color = 0xFFF263;
            drawCenteredText(matrices, textRenderer, text, x + entryWidth / 2, y + (entryHeight - 8) / 2, color);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(!this.isSelected() && button == 0) {
                this.list.select(this.index);
                return true;
            }
            return false;
        }
    }
}
