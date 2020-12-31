package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.coderbot.iris.gui.property.*;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.util.*;

public class ShaderPackPropertiesUtil {
    public static Map<String, PropertyList> createDocument(String shaderName, ShaderPack pack, PropertyDocumentWidget widget) {
        Properties shaderProperties = pack.getShaderProperties();
        Map<String, PropertyList> document = new HashMap<>();
        Map<String, String> child2Parent = new HashMap<>();
        //PropertyList mainPage = new PropertyList(new TitleProperty(new LiteralText(shaderName).formatted(Formatting.BOLD), 0xAAFFFFFF), new Property(new LiteralText("This menu is not functional.")));
        if(shaderProperties.isEmpty()) {
            document.put("screen", new PropertyList(
                    new TitleProperty(new LiteralText(shaderName).formatted(Formatting.BOLD), 0xAAFFFFFF),
                    new Property(new TranslatableText("page.iris.noConfig").formatted(Formatting.ITALIC))
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
                if(s.startsWith("screen.")) {
                    page.add(new TitleProperty(new TranslatableText(s).formatted(Formatting.BOLD), 0xAAFFFFFF));
                } else {
                    page.add(new TitleProperty(new LiteralText(shaderName).formatted(Formatting.BOLD), 0xAAFFFFFF));
                }
                String[] properties = shaderProperties.getProperty(s).split(" ");
                for(String p : properties) {
                    if(p.equals("<empty>")) {
                        page.add(new Property(new LiteralText("")));
                    } else if(p.startsWith("[") && p.endsWith("]")) {
                        String a = "screen."+String.copyValueOf(Arrays.copyOfRange(p.toCharArray(), 1, p.length() - 1));
                        page.add(new PageLinkProperty(widget, a, new TranslatableText(a), PageLinkProperty.Align.LEFT));
                        child2Parent.put(a, s);
                    } else page.add(new DoubleRangeOptionProperty(new Double[] {0.0, 0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0}, 4, widget, p, new TranslatableText("option."+p), sliderOptions.contains(p)));
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
