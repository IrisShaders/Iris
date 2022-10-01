package net.coderbot.iris.texture.pbr;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.NotNull;

public interface PBRTextureHolder {
	@NotNull
	AbstractTexture getNormalTexture();

	@NotNull
	AbstractTexture getSpecularTexture();
}
