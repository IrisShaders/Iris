package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class BooleanOptionProperty extends OptionProperty<Boolean> {
    public BooleanOptionProperty(PropertyDocumentWidget document, boolean defaultValue, String key, Text label) {
        super(new Boolean[] {true, false}, defaultValue ? 0 : 1, document, key, label);
    }

    @Override
    public Text getValueText() {
        return isDefault() ?
                new TranslatableText(getValue() ? "property.iris.boolean.true" : "property.iris.boolean.false")
        : getValue() ?
                new TranslatableText("property.iris.boolean.true").formatted(Formatting.GREEN)
        :
                new TranslatableText("property.iris.boolean.false").formatted(Formatting.RED);
    }
}
