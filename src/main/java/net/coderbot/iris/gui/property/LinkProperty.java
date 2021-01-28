package net.coderbot.iris.gui.property;

import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class LinkProperty extends Property {
    protected final PropertyDocumentWidget document;
    protected final String page;
    protected final Align align;

    public LinkProperty(PropertyDocumentWidget document, String pageName, Text label, Align align) {
        super(label);
        this.document = document;
        this.page = pageName;
        this.align = align;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        if(button == 0) {
            GuiUtil.playClickSound(1.0f);
            this.document.goTo(page);
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        int color = 0x8AE0E0E0;
        int bx = x + 4;
        int bw = width - 12;
        /*if(isHovered) {
            GuiUtil.fill(bx, y, bw, height, color);
        } else {
            GuiUtil.borderedRect(bx, y, -100, bw, height, color);
        }*/
        GuiUtil.drawButton(bx, y, bw, height, isHovered, true);
        MinecraftClient mc = MinecraftClient.getInstance();
        int tx;
        int w = mc.textRenderer.getWidth(this.label);
        if(this.align.center) tx = (x + (width/2)) - (w / 2) - 2;
        else if(this.align.left) tx = x + 10;
        else tx = x + width - 10 - w;
        this.drawText(mc, label, matrices, tx, y + (height / 2), 0xFFFFFF, false, true, true);
        this.drawText(mc, new LiteralText(this.align.left ? ">" : "<"), matrices, this.align.left ? x + width - 19 : x + 11, y + (height / 2), 0xFFFFFF, false, true, true);
    }

    public enum Align {
        LEFT(true, false), CENTER_LEFT(true, true), RIGHT(false, false), CENTER_RIGHT(false, true);

        public final boolean left;
        public final boolean center;

        Align(boolean left, boolean center) {
            this.left = left;
            this.center = center;
        }
    }
}
