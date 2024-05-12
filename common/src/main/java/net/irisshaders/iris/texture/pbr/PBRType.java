package net.irisshaders.iris.texture.pbr;

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

	@Nullable
	public static String removeSuffix(String path) {
		int extensionIndex = FilenameUtils.indexOfExtension(path);
		String pathNoExtension = path.substring(0, extensionIndex);
		PBRType type = fromFileLocation(pathNoExtension);
		if (type != null) {
			String suffix = type.getSuffix();
			String basePathNoExtension = pathNoExtension.substring(0, pathNoExtension.length() - suffix.length());
			return basePathNoExtension + path.substring(extensionIndex);
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

	public String getSuffix() {
		return suffix;
	}

	public int getDefaultValue() {
		return defaultValue;
	}

	public String appendSuffix(String path) {
		int extensionIndex = FilenameUtils.indexOfExtension(path);
		if (extensionIndex != -1) {
			return path.substring(0, extensionIndex) + suffix + path.substring(extensionIndex);
		} else {
			return path + suffix;
		}
	}
}
