package net.coderbot.iris.mixin.texture.pbr;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.coderbot.iris.texture.pbr.PBRSimpleTextureHolder;
import net.coderbot.iris.texture.pbr.PBRType;
import net.coderbot.iris.texture.pbr.SimpleTextureExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(SimpleTexture.class)
public abstract class MixinSimpleTexture extends AbstractTexture implements SimpleTextureExtension {
	@Shadow
	@Final
	protected ResourceLocation location;

	@Unique
	private PBRSimpleTextureHolder pbrTextureHolder;

	@Unique
	private SimpleTexture.TextureImage textureImage;

	@Inject(method = "getTextureImage(Lnet/minecraft/server/packs/resources/ResourceManager;)Lnet/minecraft/client/renderer/texture/SimpleTexture$TextureImage;", at = @At(value = "RETURN"))
	private void onReturnLoad(ResourceManager resourceManager, CallbackInfoReturnable<SimpleTexture.TextureImage> cir) {
		SimpleTexture.TextureImage textureImage = cir.getReturnValue();
		if (textureImage == null || !isSimpleTexture(location)) {
			return;
		}

		if (isPBRImageLocation(location)) {
			this.textureImage = textureImage;
			return;
		}

		createPBRTexture(location, resourceManager, PBRType.NORMAL);
		createPBRTexture(location, resourceManager, PBRType.SPECULAR);
	}

	private void createPBRTexture(ResourceLocation imageLocation, ResourceManager resourceManager, PBRType pbrType) {
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		try {
			pbrTexture.load(resourceManager);
		} catch (IOException e) {
			pbrTextureHolder = null;
			return;
		}

		createPBRHolderIfNull();

		switch (pbrType) {
			case NORMAL:
				pbrTextureHolder.setNormalTexture(pbrTexture, this.textureImage);
				break;
			case SPECULAR:
				pbrTextureHolder.setSpecularTexture(pbrTexture, this.textureImage);
				break;
		}
	}


	@Override
	public void close() {
		super.close();
		if (pbrTextureHolder != null) {
			pbrTextureHolder.close();
		}
	}

	@Override
	public boolean hasPBRHolder() {
		return pbrTextureHolder != null;
	}

	@Override
	@Nullable
	public PBRSimpleTextureHolder getPBRHolder() {
		return pbrTextureHolder;
	}

	private void createPBRHolderIfNull() {
		if (pbrTextureHolder == null) {
			pbrTextureHolder = new PBRSimpleTextureHolder();
		}
	}

	private boolean isSimpleTexture(ResourceLocation location) {
		return location.getPath().startsWith("textures/entity") || location.getPath().startsWith("textures/models/armor");
	}

	private boolean isPBRImageLocation(ResourceLocation location) {
		return location.getPath().endsWith(PBRType.NORMAL.getSuffix() + ".png") || location.getPath().endsWith(PBRType.SPECULAR.getSuffix() + ".png");
	}
}
