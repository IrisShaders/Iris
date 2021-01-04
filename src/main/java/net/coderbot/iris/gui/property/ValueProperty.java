package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ValueProperty<T> extends Property {
    protected final String key;
    protected final PropertyDocumentWidget document;
    protected Text valueText;

    protected int cachedWidth = 0;
    protected int cachedX = 0;

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
            valueText = createValueText((int)(cachedWidth * 0.4) - 6);
        }
        return valueText;
    }

    public abstract boolean isDefault();

    public abstract void setValue(T value);

    public abstract void setValue(String value);

    public void resetValueText() {
        this.valueText = null;
    }

    public void updateCaches(int width, int x) {
        this.cachedWidth = width;
        this.cachedX = x;
    }
}
