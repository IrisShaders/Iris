package net.coderbot.iris.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

public final class GuiUtil {
    public static void drawDirtTexture(MinecraftClient client, int x, int y, int z, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(519);
        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(x, y + height, z).texture(x, y + height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x + width, y + height, 0.0D).texture(x + width / 32.0F, y + height / 32.0F).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x + width, y, z).texture(x + width / 32.0F, y).color(64, 64, 64, 255).next();
        bufferBuilder.vertex(x, y, z).texture(x, y).color(64, 64, 64, 255).next();
        tessellator.draw();
    }
}
