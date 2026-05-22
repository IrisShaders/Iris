package net.irisshaders.iris.pbr.format;

import net.irisshaders.iris.pbr.mipmap.CustomMipmapGenerator;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface TextureFormat {
	String name();

	@Nullable
	String version();

	default List<String> getDefines() {
		List<String> defines = new ArrayList<>();

		String defineName = name().toUpperCase(Locale.ROOT).replaceAll("-", "_");
		String define = "MC_TEXTURE_FORMAT_" + defineName;
		defines.add(define);

		String version = version();
		if (version != null) {
			String defineVersion = version.replaceAll("[.-]", "_");
			String versionDefine = define + "_" + defineVersion;
			defines.add(versionDefine);
		}

		return defines;
	}

	/**
	 * Dictates whether textures of the given PBR type can have their color values interpolated or not.
	 * Usually, this controls the texture minification and magification filters -
	 * a return value of false would signify that the linear filters cannot be used.
	 *
	 * @param pbrType The type of PBR texture
	 * @return If texture values can be interpolated or not
	 */
	boolean canInterpolateValues(PBRType pbrType);

	default void setupTextureParameters(PBRType pbrType, AbstractTexture texture) {
		if (!canInterpolateValues(pbrType)) {
			texture.getTexture().iris$markMipmapNonLinear();
		}
	}

	@Nullable
	CustomMipmapGenerator getMipmapGenerator(PBRType pbrType);

	interface Factory {
		TextureFormat createFormat(String name, @Nullable String version);
	}
}
