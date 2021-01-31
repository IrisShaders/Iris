package net.coderbot.iris.gui.property;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

/**
 * An element of a document. Used for options
 * or configuration menus. Property objects are
 * added to a PropertyList, and PropertyLists are
 * added to a PropertyDocumentWidget which is used
 * in a GUI.
 */
public class Property {
    protected final Text label;

    /**
     * A completely empty property. When used in the
     * shader pack config document, EMPTYs can be
     * hidden by enabling condensed view.
     */
    public static final Property EMPTY = new Property(LiteralText.EMPTY);

    /**
     * The only difference between this and
     * EMPTY is that it is not EMPTY, and
     * won't be included if EMPTY is ever
     * searched for.
     */
    public static final Property PLACEHOLDER = new Property(LiteralText.EMPTY);

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

    protected final void drawText(MinecraftClient client, Text text, MatrixStack matrices, int x, int y, int color, boolean centerX, boolean centerY, boolean shadow) {
        TextRenderer t = client.textRenderer;
        if(shadow) t.drawWithShadow(matrices, text, centerX ? x - (int)((float)t.getWidth(text) / 2) : x, centerY ? y - 4 : y, color);
        else t.draw(matrices, text, centerX ? x - (int)((float)t.getWidth(text) / 2) : x, centerY ? y - 4 : y, color);
    }
}
