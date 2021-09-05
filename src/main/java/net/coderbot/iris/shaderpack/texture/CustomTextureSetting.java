package net.coderbot.iris.shaderpack.texture;

public class CustomTextureSetting {
	private final String sampler;
	private final String textureLocation;

	public CustomTextureSetting(String sampler, String textureLocation) {
		this.sampler = sampler;
		this.textureLocation = textureLocation;
	}

	public String getSampler() {
		return sampler;
	}

	public String getTextureLocation() {
		return textureLocation;
	}
}
