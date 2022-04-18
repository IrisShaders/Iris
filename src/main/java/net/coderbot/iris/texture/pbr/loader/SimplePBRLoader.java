package net.coderbot.iris.texture.pbr.loader;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.coderbot.iris.mixin.texture.SimpleTextureAccessor;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class SimplePBRLoader implements PBRTextureLoader<SimpleTexture> {
	@Override
	public void load(SimpleTexture texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		ResourceLocation location = ((SimpleTextureAccessor) texture).getLocation();

		AbstractTexture normalTexture = createPBRTexture(location, resourceManager, PBRType.NORMAL);
		AbstractTexture specularTexture = createPBRTexture(location, resourceManager, PBRType.SPECULAR);

		pbrTextureConsumer.acceptNormalTexture(normalTexture);
		pbrTextureConsumer.acceptSpecularTexture(specularTexture);
	}

	@Nullable
	protected AbstractTexture createPBRTexture(ResourceLocation imageLocation, ResourceManager resourceManager, PBRType pbrType) {
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		try {
			pbrTexture.load(resourceManager);
			return pbrTexture;
		} catch (IOException e) {
			//
		}
		return null;
	}
}
