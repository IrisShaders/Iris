package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TitleProperty extends Property {
    protected final int underlineColor;

    public TitleProperty(Text label, int underlineColorARGB) {
        super(label);
        this.underlineColor = underlineColorARGB;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.drawText(MinecraftClient.getInstance(), label, matrices, x + (width / 2), y + (height / 2), 0xFFFFFF, true, true, true);
        GuiUtil.fill(x + 2, y + height, width - 8, 1, underlineColor);
    }
}
