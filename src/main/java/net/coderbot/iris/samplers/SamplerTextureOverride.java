package net.coderbot.iris.samplers;

import net.coderbot.iris.shaderpack.CustomTexture;

public class SamplerTextureOverride {
	private final String sampler;
	private final CustomTexture texture;

	public SamplerTextureOverride(String sampler, CustomTexture texture) {
		this.sampler = sampler;
		this.texture = texture;
	}

	public String getSampler() {
		return sampler;
	}

	public CustomTexture getTexture() {
		return texture;
	}
}
