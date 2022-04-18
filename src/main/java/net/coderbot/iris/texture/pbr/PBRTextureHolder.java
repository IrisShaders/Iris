package net.coderbot.iris.texture.pbr;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.renderer.texture.AbstractTexture;

public interface PBRTextureHolder {
	@NotNull
	AbstractTexture normalTexture();

	@NotNull
	AbstractTexture specularTexture();
}
