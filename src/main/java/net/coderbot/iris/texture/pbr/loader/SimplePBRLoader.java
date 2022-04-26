package net.coderbot.iris.texture.pbr.loader;

import net.coderbot.iris.mixin.texture.SimpleTextureAccessor;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SimplePBRLoader implements PBRTextureLoader<SimpleTexture> {
	@Override
	public void load(SimpleTexture texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		ResourceLocation location = ((SimpleTextureAccessor) texture).getLocation();
		TextureFormat textureFormat = TextureFormatLoader.getFormat();

		AbstractTexture normalTexture = createPBRTexture(location, resourceManager, textureFormat, PBRType.NORMAL);
		AbstractTexture specularTexture = createPBRTexture(location, resourceManager, textureFormat, PBRType.SPECULAR);

		pbrTextureConsumer.acceptNormalTexture(normalTexture);
		pbrTextureConsumer.acceptSpecularTexture(specularTexture);
	}

	@Nullable
	protected AbstractTexture createPBRTexture(ResourceLocation imageLocation, ResourceManager resourceManager, @Nullable TextureFormat textureFormat, PBRType pbrType) {
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		try {
			pbrTexture.load(resourceManager);
		} catch (IOException e) {
			return null;
		}

		if (textureFormat != null) {
			textureFormat.setupTextureParameters(pbrType, pbrTexture);
		}

		return pbrTexture;
	}
}
