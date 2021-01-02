package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.coderbot.iris.gui.property.*;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.*;

public final class ShaderPackPropertiesUtil {
    public static Map<String, PropertyList> createDocument(TextRenderer tr, int width, String shaderName, ShaderPack pack, PropertyDocumentWidget widget) {
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
        for(String s : shaderProperties.stringPropertyNames()) {
            if(s.equals("sliders")) {
                sliderOptions.add(shaderProperties.getProperty(s));
            }
        }
        for(String s : shaderProperties.stringPropertyNames()) {
            if(s.startsWith("screen.") || s.equals("screen")) {
                PropertyList page = new PropertyList();
                boolean subScreen = s.startsWith("screen.");
                page.add(new TitleProperty(GuiUtil.trimmed(tr, subScreen ? s : shaderName, width - 4, subScreen, true, Formatting.BOLD), 0xAAFFFFFF));
                String[] properties = shaderProperties.getProperty(s).split(" ");
                for(String p : properties) {
                    if(p.equals("<profile>")) {
                        page.add(new StringOptionProperty(new String[] {"Low", "Medium", "High", "Extreme", "This is not functional"}, 1, widget, p, GuiUtil.trimmed(tr, "option.iris.profile", tw, true, true), sliderOptions.contains(p)));
                    } else if(p.equals("<empty>")) {
                        page.add(new Property(new LiteralText("")));
                    } else if(p.startsWith("[") && p.endsWith("]")) {
                        String a = "screen."+String.copyValueOf(Arrays.copyOfRange(p.toCharArray(), 1, p.length() - 1));
                        page.add(new PageLinkProperty(widget, a, GuiUtil.trimmed(tr, a, bw, true, true), PageLinkProperty.Align.LEFT));
                        child2Parent.put(a, s);
                    } else {
                        page.add(new StringOptionProperty(new String[] {"This", "Is", "Not", "Functional"}, 0, widget, p, GuiUtil.trimmed(tr, "option."+p, tw, true, true), sliderOptions.contains(p)));
                    }
                }
                document.put(s, page);
            }
        }
        for(String child : child2Parent.keySet()) {
            if(document.containsKey(child)) document.get(child).add(1, new PageLinkProperty(widget, child2Parent.get(child), new TranslatableText("option.iris.back"), PageLinkProperty.Align.CENTER_RIGHT));
        }
        return document;
    }
}
