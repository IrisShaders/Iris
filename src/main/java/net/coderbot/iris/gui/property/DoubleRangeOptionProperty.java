package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DoubleRangeOptionProperty extends OptionProperty<Double> {
    public DoubleRangeOptionProperty(Double[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label) {
        super(values, defaultIndex, document, key, label);
    }

    @Override
    public Text getValueText() {
        return isDefault() ? new LiteralText(Double.toString(this.getValue())) : new LiteralText(Double.toString(this.getValue())).formatted(Formatting.YELLOW);
    }
}
