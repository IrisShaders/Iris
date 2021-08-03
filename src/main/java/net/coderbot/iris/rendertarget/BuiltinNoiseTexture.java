package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL20C;

public class BuiltinNoiseTexture {
	private static final ResourceLocation NOISE = new ResourceLocation("iris", "textures/noise.png");

	public static void bind() {
		Minecraft.getInstance().getTextureManager().bind(NOISE);
		int id = Minecraft.getInstance().getTextureManager().getTexture(NOISE).getId();

		GlStateManager._activeTexture(GL20C.GL_TEXTURE15);
		GlStateManager._bindTexture(id);
	}
}
