package net.coderbot.iris.gui.property;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class PairProperty extends TupleProperty {
    protected final Property left;
    protected final Property right;

    protected int cachedWidth = 0;
    protected int cachedX = 0;

    public PairProperty(Property left, Property right) {
        super(new LiteralText(""));
        this.left = left;
        this.right = right;
    }

    public Property getLeft() {
        return left;
    }

    public Property getRight() {
        return right;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        return mouseX > ((float)cachedWidth / 2) + cachedX ? right.onClicked(mouseX, mouseY, button) : left.onClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return mouseX > ((float)cachedWidth / 2) + cachedX ? right.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : left.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean charTyped(char c, int keyCode) {
        return left.charTyped(c, keyCode) || right.charTyped(c, keyCode);
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.cachedWidth = width;
        this.cachedX = x;
        int w = (width / 2) - 2;
        boolean mouseLeft = mouseX < ((float)width / 2) + x;
        left.render(matrices, x, y, w, height, mouseX, mouseY, isHovered && mouseLeft, delta);
        right.render(matrices, x + w + 4, y, w, height, mouseX, mouseY, isHovered && !mouseLeft, delta);
    }

    @Override
    public Property[] getContainedProperties() {
        return new Property[] {this.left, this.right};
    }
}
