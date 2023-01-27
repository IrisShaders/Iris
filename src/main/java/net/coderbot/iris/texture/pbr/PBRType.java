package net.coderbot.iris.texture.pbr;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

public enum PBRType {
	NORMAL("_n", 0x7F7FFFFF),
	SPECULAR("_s", 0x00000000);

	private static final PBRType[] VALUES = values();

	private final String suffix;
	private final int defaultValue;

	PBRType(String suffix, int defaultValue) {
		this.suffix = suffix;
		this.defaultValue = defaultValue;
	}

	public String getSuffix() {
		return suffix;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public ResourceLocation appendToFileLocation(ResourceLocation location, boolean isAtlas) {
		String path = location.getPath();
		String newPath;
		int extensionIndex = FilenameUtils.indexOfExtension(path);
		if (extensionIndex != -1) {
			newPath = path.substring(0, extensionIndex) + suffix + path.substring(extensionIndex);
		} else {
			newPath = path + suffix;
		}
		return new ResourceLocation(location.getNamespace(), isAtlas ? "textures/" + newPath + ".png" : newPath);
	}

	@Nullable
	public static ResourceLocation removeSuffix(ResourceLocation location) {
		String path = location.getPath();
		int extensionIndex = FilenameUtils.indexOfExtension(path);
		String pathNoExtension = path.substring(0, extensionIndex);
		String extension = path.substring(extensionIndex);
		PBRType type = fromFileLocation(pathNoExtension);
		if (type != null) {
			String suffix = type.getSuffix();
			String basePathNoExtension = pathNoExtension.substring(0, pathNoExtension.length() - suffix.length());
			return new ResourceLocation(location.getNamespace(), basePathNoExtension + extension);
		}
		return null;
	}

	/**
	 * Returns the PBR type corresponding to the suffix of the given file location.
	 *
	 * @param location The file location without an extension
	 * @return the PBR type
	 */
	@Nullable
	public static PBRType fromFileLocation(String location) {
		for (PBRType type : VALUES) {
			if (location.endsWith(type.getSuffix())) {
				return type;
			}
		}
		return null;
	}
}
