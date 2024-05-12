package net.irisshaders.iris.texture.pbr.loader;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PBRTextureLoaderRegistry {
	public static final PBRTextureLoaderRegistry INSTANCE = new PBRTextureLoaderRegistry();

	static {
		INSTANCE.register(SimpleTexture.class, new SimplePBRLoader());
		INSTANCE.register(TextureAtlas.class, new AtlasPBRLoader());
	}

	private final Map<Class<?>, PBRTextureLoader<?>> loaderMap = new HashMap<>();

	public <T extends AbstractTexture> void register(Class<? extends T> clazz, PBRTextureLoader<T> loader) {
		loaderMap.put(clazz, loader);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends AbstractTexture> PBRTextureLoader<T> getLoader(Class<? extends T> clazz) {
		return (PBRTextureLoader<T>) loaderMap.get(clazz);
	}
}
