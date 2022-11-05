package net.coderbot.iris.texture.format;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public interface TextureFormat {
	String getName();

	@Nullable
	String getVersion();

	default List<String> getDefines() {
		List<String> defines = new ArrayList<>();

		String defineName = getName().toUpperCase(Locale.ROOT).replaceAll("-", "_");
		String define = "MC_TEXTURE_FORMAT_" + defineName;
		defines.add(define);

		String version = getVersion();
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
			int minFilter = IrisRenderSystem.getTexParameteri(texture.getId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
			// Non-mipped filters begin at 0x2600 whereas mipped filters begin at 0x2700,
			// so this bit mask can be used to check if the filter is mipped or not
			boolean mipmap = (minFilter & 1 << 8) == 1;
			IrisRenderSystem.texParameteri(texture.getId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mipmap ? GL11.GL_NEAREST_MIPMAP_NEAREST : GL11.GL_NEAREST);
			IrisRenderSystem.texParameteri(texture.getId(), GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
	}

	@Nullable
	CustomMipmapGenerator getMipmapGenerator(PBRType pbrType);

	public interface Factory {
		TextureFormat createFormat(String name, @Nullable String version);
	}
}
