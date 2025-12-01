package net.irisshaders.iris.pbr.loader;

import net.irisshaders.iris.mixin.texture.ReloadableTextureAccessor;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SimplePBRLoader implements PBRTextureLoader<SimpleTexture> {
	@Override
	public void load(SimpleTexture texture, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		ResourceLocation location = ((ReloadableTextureAccessor) texture).getLocation();

		AbstractTexture normalTexture = createPBRTexture(location, resourceManager, PBRType.NORMAL);
		AbstractTexture specularTexture = createPBRTexture(location, resourceManager, PBRType.SPECULAR);

		if (normalTexture != null) {
			pbrTextureConsumer.acceptNormalTexture(normalTexture);
		}
		if (specularTexture != null) {
			pbrTextureConsumer.acceptSpecularTexture(specularTexture);
		}
	}

	@Nullable
	protected AbstractTexture createPBRTexture(ResourceLocation imageLocation, ResourceManager resourceManager, PBRType pbrType) {
		ResourceLocation pbrImageLocation = imageLocation.withPath(pbrType::appendSuffix);

		ImmediateState.temporarilyIgnorePass = true;
		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		TextureContents contents = loadContentsSafe(pbrTexture, resourceManager);

		if (contents == null) {
			pbrTexture.close();
			ImmediateState.temporarilyIgnorePass = false;
			return null;
		}
		pbrTexture.apply(contents);
		ImmediateState.temporarilyIgnorePass = false;

		return pbrTexture;
	}

	private TextureContents loadContentsSafe(ReloadableTexture texture, ResourceManager manager) {
		try {
			return texture.loadContents(manager);
		} catch (Exception var4) {
			return null;
		}
	}
}
