package net.irisshaders.iris.targets.backed;

import com.mojang.blaze3d.platform.NativeImage;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.shaderpack.texture.CustomTextureData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntSupplier;

public class NativeImageBackedCustomTexture extends DynamicTexture implements TextureAccess {
	public NativeImageBackedCustomTexture(CustomTextureData.PngData textureData) throws IOException {
		super(create(textureData.getContent()));

		// By default, images are unblurred and not clamped.

		if (textureData.getFilteringData().shouldBlur()) {
			IrisRenderSystem.texParameteri(getId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, GL11C.GL_LINEAR);
			IrisRenderSystem.texParameteri(getId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, GL11C.GL_LINEAR);
		}

		if (textureData.getFilteringData().shouldClamp()) {
			IrisRenderSystem.texParameteri(getId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, GL13C.GL_CLAMP_TO_EDGE);
			IrisRenderSystem.texParameteri(getId(), GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, GL13C.GL_CLAMP_TO_EDGE);
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

	@Override
	public TextureType getType() {
		return TextureType.TEXTURE_2D;
	}

	@Override
	public IntSupplier getTextureId() {
		return this::getId;
	}
}
