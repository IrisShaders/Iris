package net.coderbot.iris.gui.property;

import com.google.common.collect.Lists;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class OptionProperty<T> extends ValueProperty<T> {
    protected T[] values;
    protected int index;
    protected final int defaultIndex;
    protected final boolean isSlider;

    private int cachedWidth = 0;
    private int cachedX = 0;

    public OptionProperty(T[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label, boolean isSlider) {
        super(document, key, label);
        this.values = values;
        this.index = defaultIndex;
        this.defaultIndex = defaultIndex;
        this.isSlider = isSlider;
    }

    public void cycle() {
        this.index++;
        if(index >= values.length) index = 0;
        this.documentSave();
    }

    @Override
    public T getValue() {
        return values[index];
    }

    @Override
    public void setValue(T value) {
        List<T> vList = Lists.newArrayList(values);
        if(vList.contains(value)) {
            this.index = vList.indexOf(value);
        } else {
            Iris.logger.warn("Unable to set value of {} to {} - Invalid value!", key, value);
            this.index = defaultIndex;
        }
        super.setValue(value);
    }

    protected boolean isButtonHovered(double mouseX, boolean entryHovered) {
        return entryHovered && mouseX > cachedX + (cachedWidth * 0.6) - 7;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        if(isButtonHovered(mouseX, true) && button == 0) {
            GuiUtil.playClickSound(1.025f);
            if(!isSlider) {
                cycle();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if(isSlider && isButtonHovered(mouseX, true)) {
            float pos = (float)((mouseX - (cachedX + (cachedWidth * 0.6) - 7)) / ((cachedWidth * 0.4)));
            this.index = Math.min((int)(pos * this.values.length), this.values.length - 1);
            this.documentSave();
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.cachedWidth = width;
        this.cachedX = x;
        MinecraftClient mc = MinecraftClient.getInstance();
        this.drawText(mc, label, matrices, x + 10, y + (height / 2), 0xFFFFFF, false, true, true);
        if(isSlider) this.renderSlider(mc, matrices, x, y, width, height, mouseX, mouseY, isHovered);
        else this.renderButton(mc, matrices, x, y, width, height, mouseX, isHovered);
    }

    private void renderButton(MinecraftClient mc, MatrixStack matrices, int x, int y, int width, int height, int mouseX, boolean isHovered) {
        int color = 0x8AE0E0E0;

        int bx = (int)(x + (width * 0.6)) - 7;
        int bw = (int)(width * 0.4);

        if(this.isButtonHovered(mouseX, isHovered)) {
            GuiUtil.fill(bx, y, bw, height, color);
        } else {
            GuiUtil.borderedRect(bx, y, -100, bw, height, color);
        }

        Text vt = this.getValueText();
        this.drawText(mc, vt, matrices, (int)(x + (width * 0.8)) - (mc.textRenderer.getWidth(vt) / 2) - 7, y + (height / 2), 0xFFFFFF, false, true, true);
    }

    private void renderSlider(MinecraftClient mc, MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered) {
        int color = 0x8AE0E0E0;

        int bx = (int)(x + (width * 0.6)) - 7;
        int bw = (int)(width * 0.4);

        GuiUtil.borderedRect(bx, y + 2, -100, bw, height - 4, color);
        GuiUtil.fill(bx + 1, y + 3, bw - 2, height - 6, 0x73000000);

        int sx = (bx + 2) + Math.round((((float)this.index / (this.values.length - 1))) * (bw - 10));

        if(this.isButtonHovered(mouseX, isHovered)) {
            GuiUtil.fill(sx, y + 4, 6, height - 8, color);
        } else {
            GuiUtil.borderedRect(sx, y + 4, -100, 6, height - 8, color);
        }

        Text vt = this.getValueText();
        this.drawText(mc, vt, matrices, (int)(x + (width * 0.8)) - (mc.textRenderer.getWidth(vt) / 2) - 7, y + (height / 2), 0xFFFFFF, false, true, true);
    }

    @Override
    public boolean isDefault() {
        return index == defaultIndex;
    }
}
