package net.coderbot.iris.texture.pbr.loader;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.server.packs.resources.ResourceManager;

public interface PBRTextureLoader<T extends AbstractTexture> {
	void load(T texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer);

	interface PBRTextureConsumer {
		void acceptNormalTexture(AbstractTexture texture);

		void acceptSpecularTexture(AbstractTexture texture);
	}
}
