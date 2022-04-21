package net.coderbot.iris.texture.format;

import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.pbr.PBRType;
import org.jetbrains.annotations.Nullable;

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
