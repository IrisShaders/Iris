package net.coderbot.iris.gui.element;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.property.*;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.*;

public class PropertyDocumentWidget extends ShaderScreenEntryListWidget<PropertyDocumentWidget.PropertyEntry> {
    protected Map<String, PropertyList> document = new HashMap<>();
    protected String currentPage = "";
    protected int rowWidth = 0;
    protected boolean resizedRows = false;

    protected Runnable save = () -> {};
    protected Runnable load = () -> {};

    public PropertyDocumentWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight) {
        super(client, width, height, top, bottom, left, right, itemHeight);
    }

    public PropertyDocumentWidget(MinecraftClient client, int width, int height, int top, int bottom, int left, int right, int itemHeight, int rowWidth) {
        this(client, width, height, top, bottom, left, right, itemHeight);
        this.resizedRows = true;
        this.rowWidth = rowWidth;
    }

    @Override
    public int getRowWidth() {
        return resizedRows ? rowWidth : super.getRowWidth();
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

    public Set<String> getPages() {
        return this.document.keySet();
    }

    public PropertyList getPage(String name) {
    	if(!currentPage.isEmpty()) return document.getOrDefault(name, new PropertyList(new TitleProperty(new TranslatableText("page.iris.notFound").formatted(Formatting.DARK_RED, Formatting.BOLD)), new LinkProperty(this, currentPage, new TranslatableText("option.iris.back"), LinkProperty.Align.CENTER_RIGHT), new Property(new TranslatableText("page.iris.invalid", name).formatted(Formatting.GRAY))));
        return document.getOrDefault(name, new PropertyList(new TitleProperty(new TranslatableText("page.iris.notFound").formatted(Formatting.DARK_RED))));
    }

    public void saveProperties() {
        save.run();
    }

    public void loadProperties() {
        load.run();
    }

    public void onSave(Runnable procedure) {
        this.save = procedure;
    }

    public void onLoad(Runnable procedure) {
        this.load = procedure;
    }

    @Override
    protected int getScrollbarXOffset() {
        return -2;
    }

    public void setDocument(Map<String, PropertyList> document, String homePage) {
        this.document = document;
        this.goTo(homePage);
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public static Map<String, PropertyList> createShaderpackConfigDocument(TextRenderer tr, int width, String shaderName, ShaderPack pack, PropertyDocumentWidget widget) {
        Properties shaderProperties = pack.getShaderProperties();
        Map<String, PropertyList> document = new HashMap<>();
        Map<String, String> child2Parent = new HashMap<>();
        int tw = (int)(width * 0.6) - 21;
        int bw = width - 20;
        if(shaderProperties.isEmpty() || !shaderProperties.containsKey("screen")) {
            document.put("screen", new PropertyList(
                    new TitleProperty(new LiteralText(shaderName).formatted(Formatting.BOLD), 0xAAFFFFFF),
                    new Property(GuiUtil.trimmed(tr, "page.iris.noConfig", bw, true, true, Formatting.ITALIC))
            ));
            return document;
        }
        List<String> sliderOptions = new ArrayList<>();
		List<String> profiles = new ArrayList<>();
        for(String s : shaderProperties.stringPropertyNames()) {
            if(s.equals("sliders")) {
                sliderOptions.add(shaderProperties.getProperty(s));
            } else if(s.startsWith("profile.")) {
            	profiles.add(s);
			}
        }
        for(String s : shaderProperties.stringPropertyNames()) {
            if(s.startsWith("screen.") || s.equals("screen")) {
                PropertyList page = new PropertyList();
                boolean subScreen = s.startsWith("screen.");
                page.add(new TitleProperty(GuiUtil.trimmed(tr, subScreen ? s : shaderName, width - 60, subScreen, true, Formatting.BOLD), 0xAAFFFFFF));
                String[] screenOptions = shaderProperties.getProperty(s).split(" ");
                for(String p : screenOptions) {
                    if(p.equals("<profile>")) {
                        page.add(new StringOptionProperty(profiles.toArray(new String[0]), 1, widget, p, GuiUtil.trimmed(tr, "option.iris.profile", tw, true, true), sliderOptions.contains(p), true));
                    } else if(p.equals("<empty>")) {
                        if(!Iris.getIrisConfig().getIfCondensedShaderConfig()) page.add(Property.EMPTY);
                    } else if(p.startsWith("[") && p.endsWith("]")) {
                        String a = "screen."+String.copyValueOf(Arrays.copyOfRange(p.toCharArray(), 1, p.length() - 1));
                        page.add(new LinkProperty(widget, a, GuiUtil.trimmed(tr, a, bw, true, true), LinkProperty.Align.LEFT));
                        child2Parent.put(a, s);
                    } else {
                        page.add(new StringOptionProperty(new String[] {"This", "Is", "Not", "Functional"}, 0, widget, p, GuiUtil.trimmed(tr, "option."+p, tw, true, true), sliderOptions.contains(p), false));
                    }
                }
                document.put(s, page);
            }
        }
        for(String child : child2Parent.keySet()) {
            if(document.containsKey(child)) document.get(child).add(1, new LinkProperty(widget, child2Parent.get(child), new TranslatableText("option.iris.back"), LinkProperty.Align.CENTER_RIGHT));
        }
        return document;
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
