package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class StringOptionProperty extends OptionProperty<String> {
	protected final boolean translated;

    public StringOptionProperty(String[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label, boolean isSlider, boolean displayTranslated) {
        super(values, defaultIndex, document, key, label, isSlider);
        this.translated = displayTranslated;
    }

    @Override
    public Text createValueText(int width) {
        return GuiUtil.trimmed(MinecraftClient.getInstance().textRenderer, getValue(), width, translated, true, isDefault() ? Formatting.RESET : Formatting.YELLOW);
    }
}
