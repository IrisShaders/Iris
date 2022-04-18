package net.coderbot.iris.texture.pbr.loader;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;

import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.texture.TextureAtlasAccessor;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.coderbot.iris.texture.AtlasInfoGatherer;
import net.coderbot.iris.texture.pbr.PBRAtlasTexture;
import net.coderbot.iris.texture.pbr.PBRSpriteHolder;
import net.coderbot.iris.texture.pbr.PBRType;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.coderbot.iris.texture.util.ImageScalingUtil;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class AtlasPBRLoader implements PBRTextureLoader<TextureAtlas> {
	@Override
	public void load(TextureAtlas atlas, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		int atlasWidth = AtlasInfoGatherer.getWidth(atlas);
		int atlasHeight = AtlasInfoGatherer.getHeight(atlas);
		int mipLevel = AtlasInfoGatherer.getMipLevel(atlas);

		PBRAtlasTexture normalAtlas = null;
		PBRAtlasTexture specularAtlas = null;
		for (TextureAtlasSprite sprite : ((TextureAtlasAccessor) atlas).getTexturesByName().values()) {
			if (!(sprite instanceof MissingTextureAtlasSprite)) {
				TextureAtlasSprite normalSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.NORMAL);
				TextureAtlasSprite specularSprite = createPBRSprite(sprite, resourceManager, atlas, atlasWidth, atlasHeight, mipLevel, PBRType.SPECULAR);
				if (normalSprite != null) {
					if (normalAtlas == null) {
						normalAtlas = new PBRAtlasTexture(atlas, PBRType.NORMAL);
					}
					normalAtlas.addSprite(normalSprite);
					PBRSpriteHolder pbrSpriteHolder = ((TextureAtlasSpriteExtension) sprite).getOrCreatePBRHolder();
					pbrSpriteHolder.setNormalSprite(normalSprite);
				}
				if (specularSprite != null) {
					if (specularAtlas == null) {
						specularAtlas = new PBRAtlasTexture(atlas, PBRType.SPECULAR);
					}
					specularAtlas.addSprite(specularSprite);
					PBRSpriteHolder pbrSpriteHolder = ((TextureAtlasSpriteExtension) sprite).getOrCreatePBRHolder();
					pbrSpriteHolder.setSpecularSprite(specularSprite);
				}
			}
		}

		if (normalAtlas != null) {
			try {
				normalAtlas.upload(atlasWidth, atlasHeight, mipLevel);
				pbrTextureConsumer.acceptNormalTexture(normalAtlas);
			} catch (Exception e) {
				//
			}
		}
		if (specularAtlas != null) {
			try {
				specularAtlas.upload(atlasWidth, atlasHeight, mipLevel);
				pbrTextureConsumer.acceptSpecularTexture(specularAtlas);
			} catch (Exception e) {
				//
			}
		}
	}

	@Nullable
	protected TextureAtlasSprite createPBRSprite(TextureAtlasSprite sprite, ResourceManager resourceManager, TextureAtlas atlas, int atlasWidth, int atlasHeight, int mipLevel, PBRType pbrType) {
		ResourceLocation spriteName = sprite.getName();
		ResourceLocation imageLocation = ((TextureAtlasAccessor) atlas).callGetResourceLocation(spriteName);
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		TextureAtlasSprite pbrSprite = null;
		try {
			Resource resource = resourceManager.getResource(pbrImageLocation);

			try {
				NativeImage nativeImage = NativeImage.read(resource.getInputStream());
				if (nativeImage.getWidth() != sprite.getWidth()) {
					int newWidth = sprite.getWidth();
					int newHeight = nativeImage.getHeight() * newWidth / nativeImage.getWidth();
					NativeImage scaledImage;
					if (newWidth < nativeImage.getWidth() || newWidth % nativeImage.getWidth() != 0) {
						scaledImage = ImageScalingUtil.scaleBilinear(nativeImage, newWidth, newHeight);
					} else {
						scaledImage = ImageScalingUtil.scaleNearestNeighbor(nativeImage, newWidth, newHeight);
					}
					nativeImage.close();
					nativeImage = scaledImage;
				}

				AnimationMetadataSection animationMetadata = resource.getMetadata(AnimationMetadataSection.SERIALIZER);
				if (animationMetadata == null) {
					animationMetadata = AnimationMetadataSection.EMPTY;
				}

				Pair<Integer, Integer> size = animationMetadata.getFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
				ResourceLocation pbrSpriteName = new ResourceLocation(spriteName.getNamespace(), spriteName.getPath() + pbrType.getSuffix());
				TextureAtlasSprite.Info pbrSpriteInfo = new TextureAtlasSprite.Info(pbrSpriteName, size.getFirst(), size.getSecond(), animationMetadata);

				int x = ((TextureAtlasSpriteAccessor) sprite).getX();
				int y = ((TextureAtlasSpriteAccessor) sprite).getY();
				pbrSprite = TextureAtlasSpriteAccessor.callInit(atlas, pbrSpriteInfo, mipLevel, atlasWidth, atlasHeight, x, y, nativeImage);
				syncAnimation(sprite, pbrSprite);
			} catch (Throwable t) {
				if (resource != null) {
					try {
						resource.close();
					} catch (Throwable t1) {
						t.addSuppressed(t1);
					}
				}

				throw t;
			}

			if (resource != null) {
				resource.close();
			}
		} catch (FileNotFoundException e) {
			//
		} catch (RuntimeException e) {
			Iris.logger.error("Unable to parse metadata from {} : {}", pbrImageLocation, e);
		} catch (IOException e) {
			Iris.logger.error("Unable to load {} : {}", pbrImageLocation, e);
		}

		return pbrSprite;
	}

	protected void syncAnimation(TextureAtlasSprite source, TextureAtlasSprite target) {
		if (!source.isAnimation() || !target.isAnimation()) {
			return;
		}

		TextureAtlasSpriteAccessor sourceAccessor = ((TextureAtlasSpriteAccessor) source);
		AnimationMetadataSection sourceMetadata = sourceAccessor.getMetadata();

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += sourceMetadata.getFrameTime(f);
		}

		TextureAtlasSpriteAccessor targetAccessor = ((TextureAtlasSpriteAccessor) target);
		AnimationMetadataSection targetMetadata = targetAccessor.getMetadata();

		int cycleTime = 0;
		int frameCount = targetMetadata.getFrameCount();
		for (int f = 0; f < frameCount; f++) {
			cycleTime += targetMetadata.getFrameTime(f);
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = targetMetadata.getFrameTime(targetFrame);
			if (ticks >= time) {
				targetFrame++;
				ticks -= time;
			} else {
				break;
			}
		}

		targetAccessor.setFrame(targetFrame);
		targetAccessor.setSubFrame(ticks + sourceAccessor.getSubFrame());
	}
}
