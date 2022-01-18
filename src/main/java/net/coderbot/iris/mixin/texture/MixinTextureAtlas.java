package net.coderbot.iris.mixin.texture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.coderbot.iris.texture.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasInterface {
	@Unique
	private int width = -1;

	@Unique
	private int height = -1;

	@Unique
	private int mipLevel = -1;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getMipLevel() {
		return mipLevel;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setMipLevel(int mipLevel) {
		this.mipLevel = mipLevel;
	}
}
