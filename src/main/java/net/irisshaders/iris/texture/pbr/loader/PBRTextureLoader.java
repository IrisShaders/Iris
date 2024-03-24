package net.irisshaders.iris.texture.pbr.loader;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public interface PBRTextureLoader<T extends AbstractTexture> {
	/**
	 * This method must not modify global GL state except the texture binding for {@code GL_TEXTURE_2D}.
	 *
	 * @param texture            The base texture.
	 * @param resourceManager    The resource manager.
	 * @param pbrTextureConsumer The consumer that accepts resulting PBR textures.
	 */
	void load(T texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer);

	interface PBRTextureConsumer {
		void acceptNormalTexture(@NotNull AbstractTexture texture);

		void acceptSpecularTexture(@NotNull AbstractTexture texture);
	}
}
