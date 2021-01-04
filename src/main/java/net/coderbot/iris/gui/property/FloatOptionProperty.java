package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FloatOptionProperty extends OptionProperty<Float> {
    public FloatOptionProperty(Float[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label, boolean isSlider) {
        super(values, defaultIndex, document, key, label, isSlider);
    }

    @Override
    public Text createValueText(int width) {
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, Float.toString(this.getValue()), width, false, true, isDefault() ? Formatting.RESET : Formatting.YELLOW);
    }

    @Override
    public void setValue(String value) {
        this.valueText = null;
        try {
            this.setValue(Float.parseFloat(value));
        } catch (NumberFormatException ignored) { return; }
        this.index = defaultIndex;
    }
}
