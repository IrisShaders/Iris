package net.coderbot.iris.gui.property;

import com.google.common.collect.Lists;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public abstract class OptionProperty<T> extends ValueProperty<T> {
    protected T[] values;
    protected int index;
    protected final int defaultIndex;

    private int cachedWidth = 0;
    private int cachedX = 0;

    public OptionProperty(T[] values, int defaultIndex, PropertyDocumentWidget document, String key, Text label) {
        super(document, key, label);
        this.values = values;
        this.index = defaultIndex;
        this.defaultIndex = defaultIndex;
    }

    public void cycle() {
        this.index++;
        if(index >= values.length) index = 0;
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
        }
        super.setValue(value);
    }

    protected boolean isButtonHovered(double mouseX, boolean entryHovered) {
        return entryHovered && mouseX > cachedX + (cachedWidth * 0.6) - 7;
    }

    @Override
    public boolean onClicked(double mouseX, double mouseY, int button) {
        if(isButtonHovered(mouseX, true) && button == 0) {
            cycle();
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int x, int y, int width, int height, int mouseX, int mouseY, boolean isHovered, float delta) {
        this.cachedWidth = width;
        this.cachedX = x;
        MinecraftClient mc = MinecraftClient.getInstance();
        int color = 0x7FA4A4A4;

        this.drawText(mc, label, matrices, x + 10, y + (height / 2), 0xFFFFFF, false, true, true);

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

    @Override
    public boolean isDefault() {
        return index == defaultIndex;
    }
}
