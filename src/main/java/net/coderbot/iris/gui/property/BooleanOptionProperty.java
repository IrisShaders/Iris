package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Properties;

public class BooleanOptionProperty extends OptionProperty<Boolean> {
    public BooleanOptionProperty(PropertyDocumentWidget document, boolean defaultValue, String key, Text label, boolean isSlider) {
        super(new Boolean[] {true, false}, defaultValue ? 0 : 1, document, key, label, isSlider);
    }

    @Override
    public Text createValueText(int width) {
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, getValue() ? "property.iris.boolean.true" : "property.iris.boolean.false", width, true, true, isDefault() ? Formatting.RESET : getValue() ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void read(Properties properties) {
        if(properties.containsKey(key)) {
            String s = properties.getProperty(key);
            if(s.equals("true") || s.equals("false")) {
                this.setValue(Boolean.parseBoolean(s));
                return;
            }
        }
        this.index = defaultIndex;
    }
}
