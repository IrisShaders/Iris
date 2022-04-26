package net.coderbot.iris.texture.pbr.loader;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

public interface PBRTextureLoader<T extends AbstractTexture> {
	void load(T texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer);

	interface PBRTextureConsumer {
		void acceptNormalTexture(@Nullable AbstractTexture texture);

		void acceptSpecularTexture(@Nullable AbstractTexture texture);
	}
}
