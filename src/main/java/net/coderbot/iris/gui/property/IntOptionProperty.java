package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class IntOptionProperty extends OptionProperty<Integer> {
    public IntOptionProperty(Integer[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label, boolean isSlider) {
        super(values, defaultIndex, document, key, label, isSlider);
    }

    @Override
    public Text createValueText(int width) {
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, Integer.toString(this.getValue()), width, false, true, isDefault() ? Formatting.RESET : Formatting.YELLOW);
    }

    @Override
    public void setValue(String value) {
        this.valueText = null;
        try {
            this.setValue(Integer.parseInt(value));
        } catch (NumberFormatException ignored) { return; }
        this.index = defaultIndex;
    }
}
