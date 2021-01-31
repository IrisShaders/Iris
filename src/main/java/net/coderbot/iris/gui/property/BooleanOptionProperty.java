package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BooleanOptionProperty extends OptionProperty<Boolean> {
    public BooleanOptionProperty(PropertyDocumentWidget document, boolean defaultValue, String key, Text label, boolean isSlider) {
        super(new Boolean[] {true, false}, defaultValue ? 0 : 1, document, key, label, isSlider);
    }

    @Override
    public Text createValueText(int width) {
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, getValue() ? "property.iris.boolean.true" : "property.iris.boolean.false", width, true, true, isDefault() ? Formatting.RESET : getValue() ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void setValue(String value) {
        this.valueText = null;
        if(value.equals("true") || value.equals("false")) {
            this.setValue(Boolean.parseBoolean(value));
            return;
        }
        this.index = defaultIndex;
    }
}
