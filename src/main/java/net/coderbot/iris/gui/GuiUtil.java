package net.coderbot.iris.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public final class GuiUtil {
    public static void drawDirtTexture(MinecraftClient client, int x, int y, int z, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        RenderSystem.enableTexture();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(x, y + height, z).texture(x, y + height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x + width, y + height, 0.0D).texture(x + width / 32.0F, y + height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x + width, y, z).texture(x + width / 32.0F, y).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x, y, z).texture(x, y).color(64, 64, 64, 255).next();
        tessellator.draw();
    }

    public static void drawCompactScrollBar(int x, int top, int bottom, int maxScroll, double scrollAmount, int maxPosition, float alpha) {
        if (maxScroll > 0) {
            int barHeight = (int)((float)((bottom - top) * (bottom - top)) / (float) maxPosition);
            barHeight = MathHelper.clamp(barHeight, 32, bottom - top - 8);
            int barTop = (int)scrollAmount * (bottom - top - barHeight) / maxScroll + top;
            if (barTop < top) barTop = top;
            int c = (((byte)(0x6E * alpha) << 24) | 0x0A0A0A);
            int d = (((byte)(0x7A * alpha) << 24) | 0xEFEFEF);
            GuiUtil.fill(x, top, -100, 2, bottom - top, c);
            GuiUtil.fill(x, barTop, -100, 2, barHeight, d);
            RenderSystem.enableTexture();
        }
    }

    public static void texture(int x, int y, int z, int width, int height, float u, float v, float uw, float vh) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.enableTexture();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(x, y + height, z).texture(u, v + vh).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(x + width, y + height, z).texture(u + uw, v + vh).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(x + width, y, z).texture(u + uw, v).color(1f, 1f, 1f, 1f).next();
        bufferBuilder.vertex(x, y, z).texture(u, v).color(1f, 1f, 1f, 1f).next();
        tessellator.draw();
    }

    public static void texture(int x, int y, int z, int width, int height, int u, int v, int uw, int vh, int texWidth, int texHeight) {
        texture(x, y, z, width, height, (float)u / texWidth, (float)v / texHeight, (float)uw / texWidth, (float)vh / texHeight);
    }

    public static void texture(int x, int y, int z, int width, int height, int u, int v) {
        texture(x, y, z, width, height, u, v, width, height, 256, 256);
    }

    public static void fill(int x, int y, int z, int width, int height, int colorARGB) {
        int a = (colorARGB >> 24) & 0xFF;
        int r = (colorARGB >> 16) & 0xFF;
        int g = (colorARGB >> 8) & 0xFF;
        int b = colorARGB & 0xFF;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(x, y + height, z).texture(0f, 1f).color(r, g, b, a).next();
        bufferBuilder.vertex(x + width, y + height, z).texture(1f, 1f).color(r, g, b, a).next();
        bufferBuilder.vertex(x + width, y, z).texture(1f, 0f).color(r, g, b, a).next();
        bufferBuilder.vertex(x, y, z).texture(0f, 0f).color(r, g, b, a).next();
        tessellator.draw();
    }

    public static void fill(int x, int y, int width, int height, int colorARGB) {
        fill(x, y, -100, width, height, colorARGB);
    }

    public static void borderedRect(int x, int y, int z, int width, int height, int colorARGB) {
        fill(x, y, z, width, 1, colorARGB);
        fill(x, y + height - 1, z, width, 1, colorARGB);
        fill(x, y + 1, z, 1, height - 2, colorARGB);
        fill(x + width - 1, y + 1, z, 1, height - 2, colorARGB);
    }

    public static void playClickSound(float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, pitch));
    }

    public static Text trimmed(TextRenderer tr, String text, int lenPixels, boolean translated, boolean ellipsis, Formatting... formats) {
        String tx = translated ? I18n.translate(text) : text;
        LiteralText t = (LiteralText) new LiteralText(tx).formatted(formats);
        if(tr.getWidth(t) > lenPixels) {
            return new LiteralText(tr.trimToWidth(tx, lenPixels - (ellipsis ? 8 : 0)) + (ellipsis ? "..." : "")).formatted(formats);
        }
        return t;
    }

    public static void drawButton(int x, int y, int width, int height, boolean selected, boolean isLink) {
        UITheme theme = Iris.getIrisConfig().getUITheme();
        if(theme == UITheme.SODIUM) {
            y += 1;
            height -= 2;
        }
        if(theme == UITheme.VANILLA) {
            MinecraftClient.getInstance().getTextureManager().bindTexture(AbstractButtonWidget.WIDGETS_LOCATION);
            int v = 46 + (selected ? 40 : 20);
            int yp = y + (int)Math.ceil((float)Math.max(0, height - 20) / 2);
            texture(x, yp, -100, width / 2, 20, 0, v);
            texture(x + width / 2, yp, -100, width / 2, 20, 200 - (width / 2), v);
        } else {
            if(selected) {
                fill(x, y, width, height, theme == UITheme.IRIS ? 0x8AE0E0E0 : 0xE0000000);
                if(theme == UITheme.SODIUM && isLink) GuiUtil.fill(x, y + height, width, 1, 0xFF94E4D3);
            } else if(theme == UITheme.IRIS) {
                borderedRect(x, y, -100, width, height, 0x8AE0E0E0);
            } else {
                fill(x, y, width, height, 0x90000000);
            }
        }
    }
}
