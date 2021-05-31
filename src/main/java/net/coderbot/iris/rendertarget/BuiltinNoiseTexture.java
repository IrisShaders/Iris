package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL20C;

public class BuiltinNoiseTexture {
	private static final Identifier NOISE = new Identifier("iris", "textures/noise.png");

	public static void bind() {
		MinecraftClient.getInstance().getTextureManager().bindTexture(NOISE);
		int id = MinecraftClient.getInstance().getTextureManager().getTexture(NOISE).getGlId();

		GlStateManager._activeTexture(GL20C.GL_TEXTURE15);
		GlStateManager._bindTexture(id);
	}
}
