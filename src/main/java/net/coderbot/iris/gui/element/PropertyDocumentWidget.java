package net.coderbot.iris.gui.element;

import net.coderbot.iris.gui.property.Property;
import net.coderbot.iris.gui.property.PropertyList;
import net.coderbot.iris.gui.property.ValueProperty;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyDocumentWidget extends ShaderScreenEntryListWidget<PropertyDocumentWidget.PropertyEntry> {
    protected Map<String, PropertyList> document = new HashMap<>();
    protected String currentPage = "";

    public PropertyDocumentWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
        super(client, width, height, top, bottom, left, right, itemHeight);
    }

    public void addPage(String page, PropertyList properties) {
        this.document.put(page, properties);
    }

    public void goTo(String page) {
        this.clearEntries();
        for(Property p : getPage(page)) {
            this.addEntry(new PropertyEntry(this, p));
        }
        this.currentPage = page;
        this.setScrollAmount(0.0);
    }

    public PropertyList getPage(String name) {
        return document.getOrDefault(name, new PropertyList(new Property(new TranslatableText("page.iris.notFound").formatted(Formatting.DARK_RED))));
    }

    public void saveProperties() {
        this.getPage(currentPage).save();
    }

    @Override
    protected int getScrollbarXOffset() {
        return -2;
    }

    public void setDocument(Map<String, PropertyList> document, String homePage) {
        this.document = document;
        // TODO: Make this the actual shader properties txt file plus the default values in the shader's config
        Properties properties = new Properties();
        for(String page : document.keySet()) {
            for(Property p : document.get(page)) {
                if(p instanceof ValueProperty) {
                    ((ValueProperty<?>)p).read(properties);
                }
            }
        }
        this.goTo(homePage);
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public static class PropertyEntry extends AlwaysSelectedEntryListWidget.Entry<PropertyEntry> {
        private final Property property;
        private final PropertyDocumentWidget parent;

        public PropertyEntry(PropertyDocumentWidget parent, Property property) {
            this.property = property;
            this.parent = parent;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.property.onClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            return this.property.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean charTyped(char chr, int keyCode) {
            return this.property.charTyped(chr, keyCode);
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.property.render(matrices, x, y, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
        }
    }
}
