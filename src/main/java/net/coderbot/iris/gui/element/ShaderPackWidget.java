package net.coderbot.iris.gui.element;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ShaderPackWidget extends AbstractPressableButtonWidget {
    public ShaderPackWidget(int x, int y, int width, int height, String packName) {
        super(x, y, width, height, new LiteralText(packName));
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        rect(x - 1, y - 1, width, height, matrices, 0x535353);
        if(this.isHovered()) rect(x, y, width, height, matrices, 0xFFFFFF);
        else rect(x, y, width, height, matrices, 0xBDBDBD);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        drawCenteredText(matrices, textRenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, 0xFFFFFF);
    }

    @Override
    public void onPress() {

    }

    private void rect(int x, int y, int width, int height, MatrixStack matrices, int color) {
        DrawableHelper.fill(matrices, x, y, x + width, y, color);
        DrawableHelper.fill(matrices, x, y + height, x + width, y + height, color);
        DrawableHelper.fill(matrices, x, y, x, y + height, color);
        DrawableHelper.fill(matrices, x + width, y, x + width, y + height, color);
    }
}
