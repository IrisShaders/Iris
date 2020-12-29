package net.coderbot.iris.gui.element;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.property.Property;
import net.coderbot.iris.gui.property.PropertyList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class PropertyDocumentWidget extends ShaderScreenEntryListWidget<PropertyDocumentWidget.PropertyEntry> {
    protected Map<String, PropertyList> pages = new HashMap<>();
    protected String currentPage = "";

    public PropertyDocumentWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
        super(client, width, height, top, bottom, left, right, itemHeight);
    }

    public void addPage(String page, PropertyList properties) {
        this.pages.put(page, properties);
    }

    public void goTo(String page) {
        this.clearEntries();
        for(Property p : getPage(page)) {
            this.addEntry(new PropertyEntry(p));
        }
        this.currentPage = page;
    }

    public PropertyList getPage(String name) {
        return pages.getOrDefault(name, new PropertyList(new Property(new TranslatableText("page.iris.notFound").formatted(Formatting.DARK_RED))));
    }

    public void saveProperties() {
        this.getPage(currentPage).save();
    }

    @Override
    protected int getScrollbarXOffset() {
        return -2;
    }

    public static class PropertyEntry extends AlwaysSelectedEntryListWidget.Entry<PropertyEntry> {
        private final Property property;

        public PropertyEntry(Property property) {
            this.property = property;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.property.onClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.property.render(matrices, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }
    }
}
