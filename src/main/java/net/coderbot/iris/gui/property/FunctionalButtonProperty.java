package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.text.Text;

public class FunctionalButtonProperty extends LinkProperty {
    protected final Runnable onClick;

    public FunctionalButtonProperty(PropertyDocumentWidget document, Runnable onClick, Text label, Align align) {
        super(document, "", label, align);
        this.onClick = onClick;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            GuiUtil.playClickSound(1.0f);
            onClick.run();
            return true;
        }
        return false;
    }
}
