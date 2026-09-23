package net.irisshaders.iris.targets.backed;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.shaderpack.texture.CustomTextureData;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.function.IntSupplier;

public class NativeImageBackedCustomTexture extends DynamicTexture implements TextureAccess {
	private final boolean shouldBlur;
	private final boolean shouldClamp;

	public NativeImageBackedCustomTexture(CustomTextureData.PngData textureData) throws IOException {
		super(() -> "PNG Texture", create(textureData.getContent()));

		this.shouldBlur = textureData.getFilteringData().shouldBlur();
		this.shouldClamp = textureData.getFilteringData().shouldClamp();
	}

	private static NativeImage create(byte[] content) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocateDirect(content.length);
		buffer.put(content);
		buffer.flip();

		return NativeImage.read(buffer);
	}

	private int getId() {
		return this.texture.iris$getGlId();
	}

	@Override
	public void upload() {
		NativeImage image = Objects.requireNonNull(getPixels());

		RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, image);
	}

	@Override
	public TextureType getType() {
		return TextureType.TEXTURE_2D;
	}

	@Override
	public IntSupplier getTextureId() {
		return this::getId;
	}

	@Override
	public GlSampler getSampling() {
		if (shouldClamp) {
			if (shouldBlur) {
				return GlSampler.LINEAR;
			} else {
				return GlSampler.NEAREST;
			}
		} else {
			if (shouldBlur) {
				return GlSampler.LINEAR_REPEAT;
			} else {
				return GlSampler.NEAREST_REPEAT;
			}
		}
	}
}
