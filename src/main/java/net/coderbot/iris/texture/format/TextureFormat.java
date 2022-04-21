package net.coderbot.iris.texture.format;

import org.jetbrains.annotations.Nullable;

import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.pbr.PBRType;

public interface TextureFormat {
	String getName();

	@Nullable
	String getVersion();

	@Nullable
	CustomMipmapGenerator getMipmapGenerator(PBRType pbrType);

	public interface Factory {
		TextureFormat createFormat(String name, @Nullable String version);
	}
}
