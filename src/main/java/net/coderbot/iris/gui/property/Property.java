package net.coderbot.iris.gui.property;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class Property {
    protected final Text label;

    public Property(Text label) {
        this.label = label;
    }

    public boolean onClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.drawText(MinecraftClient.getInstance(), label, matrices, x + 10, y + (height / 2), 0xFFFFFF, false, true, true);
    }

    public boolean charTyped(char c, int keyCode) {
        return false;
    }

    public int getHeight() {
        return 20;
    }

    protected final void drawText(MinecraftClient client, Text text, MatrixStack matrices, int x, int y, int color, boolean centerX, boolean centerY, boolean shadow) {
        TextRenderer t = client.textRenderer;
        if(shadow) t.drawWithShadow(matrices, text, centerX ? x - (int)((float)t.getWidth(text) / 2) : x, centerY ? y - 4 : y, color);
        else t.draw(matrices, text, centerX ? x - (int)((float)t.getWidth(text) / 2) : x, centerY ? y - 4 : y, color);
    }
}
