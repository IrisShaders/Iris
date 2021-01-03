package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ShaderPackListWidget extends ShaderScreenEntryListWidget<ShaderPackListWidget.ShaderPackEntry> {
    public ShaderPackListWidget(MinecraftClient minecraftClient, int width, int height, int top, int bottom, int left, int right) {
        super(minecraftClient, width, height, top, bottom, left, right, 20);
        refresh();
    }

    @Override
    public int getRowWidth() {
        return width - 6;
    }

    @Override
    protected int getRowTop(int index) {
        return super.getRowTop(index) + 2;
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        GuiUtil.drawCompactScrollBar(this.width - 2, this.top + 2, this.bottom - 2, this.getMaxScroll(), this.getScrollAmount(), this.getMaxPosition(), Math.max(0, Math.min(3, this.scrollbarFade + (hovered ? delta : -delta))) / 3);
        //GuiUtil.drawCompactScrollBar(this.width - 2, this.top + 2, this.bottom - 2, this.getMaxScroll(), this.getScrollAmount(), this.getMaxPosition(), 1f);
        this.hovered = this.isMouseOver(mouseX, mouseY);
    }

    public void addEntry(int index, String name) {
        ShaderPackEntry entry = new ShaderPackEntry(index, this, name);
        if(Iris.getIrisConfig().getShaderPackName().equals(name)) this.selected = entry;
        this.addEntry(entry);
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
            String name = packName;
            if(textRenderer.getWidth(new LiteralText(name).formatted(Formatting.BOLD)) > this.list.width - 8) {
                char[] cs = packName.toCharArray();
                name = String.copyValueOf(Arrays.copyOfRange(cs, 0, Math.min(cs.length, (int)(((float)this.list.width - 14) / 6)) - 3))+"...";
            }
            MutableText text = new LiteralText(name);
            if(this.isMouseOver(mouseX, mouseY)) text = text.formatted(Formatting.BOLD);
            if(this.isSelected()) color = 0xFFF263;
            drawCenteredText(matrices, textRenderer, text, (x + entryWidth / 2) - 2, y + (entryHeight - 11) / 2, color);
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
