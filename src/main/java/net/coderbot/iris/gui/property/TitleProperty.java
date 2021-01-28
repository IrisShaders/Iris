package net.coderbot.iris.gui.property;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.UiTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class TitleProperty extends Property {
    protected int[] underlineColors;

    public TitleProperty(Text label, int... underlineColorsARGB) {
        super(label);
        this.underlineColors = underlineColorsARGB;
        if(underlineColors.length == 0) underlineColors = new int[] { 0xFFFFFFFF };
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        if(Iris.getIrisConfig().getUITheme() == UiTheme.SODIUM) GuiUtil.fill(x + 4, y + 3, width - 12, height - 6, 0x90000000);
        this.drawText(MinecraftClient.getInstance(), label, matrices, x + (width / 2), y + (height / 2), 0xFFFFFF, true, true, true);
        float sectionWid = (float)(width - 8) / underlineColors.length;
        int drawn = 0;
        for(int i = 0; i < underlineColors.length; i++) {
            int w = (int)(sectionWid * (i + 1)) - drawn;
            GuiUtil.fill(x + 2 + drawn, y + height, w, 1, underlineColors[i]);
            drawn += w;
        }
    }
}
