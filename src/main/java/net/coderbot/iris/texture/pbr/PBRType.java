package net.coderbot.iris.texture.pbr;

import org.apache.commons.io.FilenameUtils;

import net.minecraft.resources.ResourceLocation;

public enum PBRType {
	NORMAL("_n", 0x7F7FFFFF),
	SPECULAR("_s", 0x00000000);

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

	public ResourceLocation appendToFileLocation(ResourceLocation location) {
		String path = location.getPath();
		String extension = FilenameUtils.getExtension(path);
		String basePath = path.substring(0, path.length() - extension.length() - 1);
		return new ResourceLocation(location.getNamespace(), basePath + suffix + "." + extension);
	}
}
