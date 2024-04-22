package net.irisshaders.iris.texture.pbr.loader;

import com.mojang.blaze3d.platform.NativeImage;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.texture.AnimationMetadataSectionAccessor;
import net.irisshaders.iris.mixin.texture.TextureAtlasAccessor;
import net.irisshaders.iris.texture.format.TextureFormat;
import net.irisshaders.iris.texture.format.TextureFormatLoader;
import net.irisshaders.iris.texture.mipmap.ChannelMipmapGenerator;
import net.irisshaders.iris.texture.mipmap.CustomMipmapGenerator;
import net.irisshaders.iris.texture.mipmap.LinearBlendFunction;
import net.irisshaders.iris.texture.pbr.PBRAtlasTexture;
import net.irisshaders.iris.texture.pbr.PBRSpriteHolder;
import net.irisshaders.iris.texture.pbr.PBRType;
import net.irisshaders.iris.texture.pbr.SpriteContentsExtension;
import net.irisshaders.iris.texture.util.ImageManipulationUtil;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class AtlasPBRLoader implements PBRTextureLoader<TextureAtlas> {
	public static final ChannelMipmapGenerator LINEAR_MIPMAP_GENERATOR = new ChannelMipmapGenerator(
		LinearBlendFunction.INSTANCE,
		LinearBlendFunction.INSTANCE,
		LinearBlendFunction.INSTANCE,
		LinearBlendFunction.INSTANCE
	);

	@Override
	public void load(TextureAtlas atlas, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
		int atlasWidth = atlasAccessor.callGetWidth();
		int atlasHeight = atlasAccessor.callGetHeight();
		int mipLevel = atlasAccessor.getMipLevel();

		PBRAtlasTexture normalAtlas = null;
		PBRAtlasTexture specularAtlas = null;
		for (TextureAtlasSprite sprite : ((TextureAtlasAccessor) atlas).getTexturesByName().values()) {
			PBRTextureAtlasSprite normalSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.NORMAL);
			PBRTextureAtlasSprite specularSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.SPECULAR);
			if (normalSprite != null) {
				if (normalAtlas == null) {
					normalAtlas = new PBRAtlasTexture(atlas, PBRType.NORMAL);
				}
				normalAtlas.addSprite(normalSprite);
				PBRSpriteHolder pbrSpriteHolder = ((SpriteContentsExtension) sprite.contents()).getOrCreatePBRHolder();
				pbrSpriteHolder.setNormalSprite(normalSprite);
			}
			if (specularSprite != null) {
				if (specularAtlas == null) {
					specularAtlas = new PBRAtlasTexture(atlas, PBRType.SPECULAR);
				}
				specularAtlas.addSprite(specularSprite);
				PBRSpriteHolder pbrSpriteHolder = ((SpriteContentsExtension) sprite.contents()).getOrCreatePBRHolder();
				pbrSpriteHolder.setSpecularSprite(specularSprite);
			}
		}

		if (normalAtlas != null) {
			if (normalAtlas.tryUpload(atlasWidth, atlasHeight, mipLevel)) {
				pbrTextureConsumer.acceptNormalTexture(normalAtlas);
			}
		}
		if (specularAtlas != null) {
			if (specularAtlas.tryUpload(atlasWidth, atlasHeight, mipLevel)) {
				pbrTextureConsumer.acceptSpecularTexture(specularAtlas);
			}
		}
	}

	@Nullable
	protected PBRTextureAtlasSprite createPBRSprite(TextureAtlasSprite sprite, ResourceManager resourceManager, TextureAtlas atlas, int atlasWidth, int atlasHeight, int mipLevel, PBRType pbrType) {
		ResourceLocation spriteName = sprite.contents().name();
		ResourceLocation pbrImageLocation = getPBRImageLocation(spriteName, pbrType);

		Optional<Resource> optionalResource = resourceManager.getResource(pbrImageLocation);
		if (optionalResource.isEmpty()) {
			return null;
		}
		Resource resource = optionalResource.get();

		ResourceMetadata animationMetadata;
		try {
			animationMetadata = resource.metadata();
		} catch (Exception e) {
			Iris.logger.error("Unable to parse metadata from {}", pbrImageLocation, e);
			return null;
		}

		NativeImage nativeImage;
		try (InputStream stream = resource.open()) {
			nativeImage = NativeImage.read(stream);
		} catch (IOException e) {
			Iris.logger.error("Using missing texture, unable to load {}", pbrImageLocation, e);
			return null;
		}

		int imageWidth = nativeImage.getWidth();
		int imageHeight = nativeImage.getHeight();
		AnimationMetadataSection metadataSection = animationMetadata.getSection(AnimationMetadataSection.SERIALIZER).orElse(AnimationMetadataSection.EMPTY);
		FrameSize frameSize = metadataSection.calculateFrameSize(imageWidth, imageHeight);
		int frameWidth = frameSize.width();
		int frameHeight = frameSize.height();
		if (!Mth.isMultipleOf(imageWidth, frameWidth) || !Mth.isMultipleOf(imageHeight, frameHeight)) {
			Iris.logger.error("Image {} size {},{} is not multiple of frame size {},{}", pbrImageLocation, imageWidth, imageHeight, frameWidth, frameHeight);
			nativeImage.close();
			return null;
		}

		int targetFrameWidth = sprite.contents().width();
		int targetFrameHeight = sprite.contents().height();
		if (frameWidth != targetFrameWidth || frameHeight != targetFrameHeight) {
			try {
				// We can assume the following is always true:
				// imageWidth % frameWidth == 0 && imageHeight % frameHeight == 0
				int targetImageWidth = imageWidth / frameWidth * targetFrameWidth;
				int targetImageHeight = imageHeight / frameHeight * targetFrameHeight;

				NativeImage scaledImage;
				if (targetImageWidth % imageWidth == 0 && targetImageHeight % imageHeight == 0) {
					scaledImage = ImageManipulationUtil.scaleNearestNeighbor(nativeImage, targetImageWidth, targetImageHeight);
				} else {
					scaledImage = ImageManipulationUtil.scaleBilinear(nativeImage, targetImageWidth, targetImageHeight);
				}
				nativeImage.close();
				nativeImage = scaledImage;

				frameWidth = targetFrameWidth;
				frameHeight = targetFrameHeight;

				if (metadataSection != AnimationMetadataSection.EMPTY) {
					AnimationMetadataSectionAccessor animationAccessor = (AnimationMetadataSectionAccessor) metadataSection;
					int internalFrameWidth = animationAccessor.getFrameWidth();
					int internalFrameHeight = animationAccessor.getFrameHeight();
					if (internalFrameWidth != -1) {
						animationAccessor.setFrameWidth(frameWidth);
					}
					if (internalFrameHeight != -1) {
						animationAccessor.setFrameHeight(frameHeight);
					}
				}
			} catch (Exception e) {
				Iris.logger.error("Something bad happened trying to load PBR texture " + spriteName.getPath() + pbrType.getSuffix() + "!", e);
				throw e;
			}
		}

		ResourceLocation pbrSpriteName = new ResourceLocation(spriteName.getNamespace(), spriteName.getPath() + pbrType.getSuffix());
		PBRSpriteContents pbrSpriteContents = new PBRSpriteContents(pbrSpriteName, new FrameSize(frameWidth, frameHeight), nativeImage, animationMetadata, pbrType);
		pbrSpriteContents.increaseMipLevel(mipLevel);
		return new PBRTextureAtlasSprite(pbrSpriteName, pbrSpriteContents, atlasWidth, atlasHeight, sprite.getX(), sprite.getY(), sprite);
	}

	protected ResourceLocation getPBRImageLocation(ResourceLocation spriteName, PBRType pbrType) {
		String path = pbrType.appendSuffix(spriteName.getPath());
		// Temporary fix for CIT Resewn. CIT Resewn has sprites that are not in the textures/ folder, so a custom check must be used here to avoid that assumption.
		if (path.startsWith("optifine/cit/")) {
			return new ResourceLocation(spriteName.getNamespace(), path + ".png");
		}
		return new ResourceLocation(spriteName.getNamespace(), "textures/" + path + ".png");
	}

	protected static class PBRSpriteContents extends SpriteContents implements CustomMipmapGenerator.Provider {
		protected final PBRType pbrType;

		public PBRSpriteContents(ResourceLocation name, FrameSize size, NativeImage image, ResourceMetadata metadata, PBRType pbrType) {
			super(name, size, image, metadata);
			this.pbrType = pbrType;
		}

		@Override
		public CustomMipmapGenerator getMipmapGenerator() {
			TextureFormat format = TextureFormatLoader.getFormat();
			if (format != null) {
				CustomMipmapGenerator generator = format.getMipmapGenerator(pbrType);
				if (generator != null) {
					return generator;
				}
			}
			return LINEAR_MIPMAP_GENERATOR;
		}
	}

	public static class PBRTextureAtlasSprite extends TextureAtlasSprite {
		protected final TextureAtlasSprite baseSprite;

		protected PBRTextureAtlasSprite(ResourceLocation location, PBRSpriteContents contents, int atlasWidth, int atlasHeight, int x, int y, TextureAtlasSprite baseSprite) {
			super(location, contents, atlasWidth, atlasHeight, x, y);
			this.baseSprite = baseSprite;
		}

		public TextureAtlasSprite getBaseSprite() {
			return baseSprite;
		}
	}
}
