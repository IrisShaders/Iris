package net.coderbot.iris.texture;

import net.minecraft.client.renderer.texture.SimpleTexture;
import org.jetbrains.annotations.Nullable;

public class PBRSimpleTextureHolder {
	protected SimpleTexture normalTexture;
	protected SimpleTexture.TextureImage normalTextureImage;
	protected SimpleTexture specularTexture;
	protected SimpleTexture.TextureImage specularTextureImage;

	public boolean hasNormalTexture() {
 		return normalTexture != null;
	}

	public boolean hasSpecularTexture() {
		return specularTexture != null;
	}

	@Nullable
	public SimpleTexture getNormalTexture() {
		return normalTexture;
	}

	@Nullable
	public SimpleTexture getSpecularTexture() {
		return specularTexture;
	}

	public void setNormalTexture(SimpleTexture texture, SimpleTexture.TextureImage textureImage) {
		normalTexture = texture;
		normalTextureImage = textureImage;
	}

	public void setSpecularTexture(SimpleTexture texture, SimpleTexture.TextureImage textureImage) {
		specularTexture = texture;
		specularTextureImage = textureImage;
	}

	public void close() {
		if (normalTextureImage != null) {
			normalTextureImage.close();
		}
		if (specularTextureImage != null) {
			specularTextureImage.close();
		}
	}
}
