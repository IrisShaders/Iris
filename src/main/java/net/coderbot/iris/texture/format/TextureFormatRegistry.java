package net.coderbot.iris.texture.format;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

public class TextureFormatRegistry {
	public static final TextureFormatRegistry INSTANCE = new TextureFormatRegistry();

	static {
		INSTANCE.register("lab-pbr", LabPBRTextureFormat::new);
	}

	private final Map<String, TextureFormat.Factory> factoryMap = new HashMap<>();

	public void register(String name, TextureFormat.Factory factory) {
		factoryMap.put(name, factory);
	}

	@Nullable
	public TextureFormat.Factory getFactory(String name) {
		return factoryMap.get(name);
	}
}
