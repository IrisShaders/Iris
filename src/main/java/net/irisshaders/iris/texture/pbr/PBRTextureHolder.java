package net.irisshaders.iris.texture.pbr;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;

public interface PBRTextureHolder {
	@NotNull
	AbstractTexture normalTexture();

	@NotNull
	AbstractTexture specularTexture();
}
