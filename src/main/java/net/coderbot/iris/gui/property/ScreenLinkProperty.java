package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Function;

public class ScreenLinkProperty extends LinkProperty {
    protected final Function<Screen, Screen> screen;
    protected final Screen parent;

    public ScreenLinkProperty(PropertyDocumentWidget document, Function<Screen, Screen> screen, Screen parent, Text label, Align align) {
        super(document, "", label, align);
        this.screen = screen;
        this.parent = parent;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            GuiUtil.playClickSound(1.0f);
            MinecraftClient.getInstance().openScreen(screen.apply(parent));
            return true;
        }
        return false;
    }
}
