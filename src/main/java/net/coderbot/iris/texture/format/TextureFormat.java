package net.coderbot.iris.texture.format;

import net.coderbot.iris.gl.shader.MacroBuilder;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.pbr.PBRType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public interface TextureFormat {
	String getName();

	@Nullable
	String getVersion();

	default void addMacros(MacroBuilder builder) {
		String macroName = getName().toUpperCase(Locale.ROOT).replaceAll("-", "_");
		String macro = "MC_TEXTURE_FORMAT_" + macroName;
		builder.define(macro);

		String version = getVersion();
		if (version != null) {
			String macroVersion = version.replaceAll("[.-]", "_");
			String versionMacro = macro + "_" + macroVersion;
			builder.define(versionMacro);
		}
	}

	@Nullable
	CustomMipmapGenerator getMipmapGenerator(PBRType pbrType);

	public interface Factory {
		TextureFormat createFormat(String name, @Nullable String version);
	}
}
