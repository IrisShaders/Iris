package net.coderbot.iris.rendertarget;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class NativeImageBackedCustomTexture extends DynamicTexture {
	public NativeImageBackedCustomTexture(CustomTextureData.PngData textureData) throws IOException {
		super(create(textureData.getContent()));

		// By default, images are unblurred and not clamped.

		if (textureData.getFilteringData().shouldBlur()) {
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		}

		if (textureData.getFilteringData().shouldClamp()) {
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
		}
	}

	private static NativeImage create(byte[] content) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
		buffer.put(content);
		buffer.flip();

		return NativeImage.read(buffer);
	}

	@Override
	public void upload() {
		NativeImage image = Objects.requireNonNull(getPixels());

		bind();
		image.upload(0, 0, 0, 0, 0, image.getWidth(), image.getHeight(), false, false, false, false);
	}
}
