package net.coderbot.iris.mixin.pbr;

import net.coderbot.iris.samplers.EntityTextureTracker;
import net.coderbot.iris.texture.PBRSimpleTextureHolder;
import net.coderbot.iris.texture.PBRType;
import net.coderbot.iris.texture.SimpleTextureExtension;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;

@Mixin(SimpleTexture.class)
public abstract class SimpleTextureMixin extends AbstractTexture implements SimpleTextureExtension {
	@Shadow
	@Final
	protected ResourceLocation location;

	@Unique
	private PBRSimpleTextureHolder pbrTextureHolder;

	@Unique
	private SimpleTexture.TextureImage textureImage;

	@Override
	public int getId() {
		int id = super.getId();

		if (isEntityTexture(location) && !isPBRImageLocation(location)) {
			EntityTextureTracker.INSTANCE.trackTexture(id, (SimpleTexture) (Object) this);
		}

		return id;
	}

	@Inject(method = "getTextureImage(Lnet/minecraft/server/packs/resources/ResourceManager;)Lnet/minecraft/client/renderer/texture/SimpleTexture$TextureImage;", at = @At(value = "RETURN"))
	private void onReturnLoad(ResourceManager resourceManager, CallbackInfoReturnable<SimpleTexture.TextureImage> cir) {
		SimpleTexture.TextureImage textureImage = cir.getReturnValue();
		if (textureImage == null || !isEntityTexture(location)) {
			return;
		}

		if (isPBRImageLocation(location)) {
			this.textureImage = textureImage;
			return;
		}

		createPBRSprite(location, resourceManager, PBRType.NORMAL);
		createPBRSprite(location, resourceManager, PBRType.SPECULAR);
	}

	private void createPBRSprite(ResourceLocation imageLocation, ResourceManager resourceManager, PBRType pbrType) {
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		SimpleTexture pbrTexture = new SimpleTexture(pbrImageLocation);
		try {
			pbrTexture.load(resourceManager);
		} catch (IOException e) {
			pbrTextureHolder = null;
			return;
		}

		createPBRSpriteHolderIfNull();

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
	public boolean hasPBRSpriteHolder() {
		return pbrTextureHolder != null;
	}

	@Override
	@Nullable
	public PBRSimpleTextureHolder getPBRSpriteHolder() {
		return pbrTextureHolder;
	}

	private void createPBRSpriteHolderIfNull() {
		if (pbrTextureHolder == null) {
			pbrTextureHolder = new PBRSimpleTextureHolder();
		}
	}

	private boolean isEntityTexture(ResourceLocation location) {
		return location.getPath().startsWith("textures/entity");
	}

	private boolean isPBRImageLocation(ResourceLocation location) {
		return location.getPath().endsWith(PBRType.NORMAL.getSuffix() + ".png") || location.getPath().endsWith(PBRType.SPECULAR.getSuffix() + ".png");
	}
}
