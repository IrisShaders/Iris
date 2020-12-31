package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class ValueProperty<T> extends Property {
    protected final String key;
    protected final PropertyDocumentWidget document;
    protected Text valueText;

    public ValueProperty(PropertyDocumentWidget document, String key, Text label) {
        super(label);
        this.key = key;
        this.document = document;
    }

    public String getKey() {
        return key;
    }

    public abstract T getValue();

    public abstract Text createValueText(int width);

    public final Text getValueText() {
        if(valueText == null) {
            valueText = createValueText((int)(this.document.getRowWidth() * 0.4) - 6);
        }
        return valueText;
    }

    public abstract boolean isDefault();

    public void setValue(T value) {
        this.save();
    }

    public void save() {
        this.valueText = null;
        // TODO: Saving to file (will add Properties or similar as an arg)
    }

    public void read() {
        // TODO: Read value from file (will add Properties or similar as an arg), and call setValue()
    }
}
