package net.coderbot.iris.texture.pbr.loader;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.texture.AnimatedTextureAccessor;
import net.coderbot.iris.mixin.texture.AnimationMetadataSectionAccessor;
import net.coderbot.iris.mixin.texture.FrameInfoAccessor;
import net.coderbot.iris.mixin.texture.TextureAtlasAccessor;
import net.coderbot.iris.mixin.texture.TextureAtlasSpriteAccessor;
import net.coderbot.iris.texture.TextureInfoCache;
import net.coderbot.iris.texture.TextureInfoCache.TextureInfo;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.mipmap.ChannelMipmapGenerator;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.mipmap.LinearBlendFunction;
import net.coderbot.iris.texture.pbr.PBRAtlasTexture;
import net.coderbot.iris.texture.pbr.PBRSpriteHolder;
import net.coderbot.iris.texture.pbr.PBRType;
import net.coderbot.iris.texture.pbr.TextureAtlasSpriteExtension;
import net.coderbot.iris.texture.util.ImageManipulationUtil;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class AtlasPBRLoader implements PBRTextureLoader<TextureAtlas> {
	public static final ChannelMipmapGenerator LINEAR_MIPMAP_GENERATOR = new ChannelMipmapGenerator(
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE,
			LinearBlendFunction.INSTANCE
	);

	@Override
	public void load(TextureAtlas atlas, ResourceManager resourceManager, PBRTextureConsumer pbrTextureConsumer) {
		TextureInfo textureInfo = TextureInfoCache.INSTANCE.getInfo(atlas.getId());
		int atlasWidth = textureInfo.getWidth();
		int atlasHeight = textureInfo.getHeight();
		int mipLevel = fetchAtlasMipLevel(atlas);

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

	protected static int fetchAtlasMipLevel(TextureAtlas atlas) {
		TextureAtlasSprite missingSprite = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
		return ((TextureAtlasSpriteAccessor) missingSprite).getMainImage().length - 1;
	}

	@Nullable
	protected TextureAtlasSprite createPBRSprite(TextureAtlasSprite sprite, ResourceManager resourceManager, TextureAtlas atlas, int atlasWidth, int atlasHeight, int mipLevel, PBRType pbrType) {
		ResourceLocation spriteName = sprite.getName();
		ResourceLocation imageLocation = ((TextureAtlasAccessor) atlas).callGetResourceLocation(spriteName);
		ResourceLocation pbrImageLocation = pbrType.appendToFileLocation(imageLocation);

		TextureAtlasSprite pbrSprite = null;
		try (Resource resource = resourceManager.getResource(pbrImageLocation)) {
			NativeImage nativeImage = NativeImage.read(resource.getInputStream());
			AnimationMetadataSection animationMetadata = resource.getMetadata(AnimationMetadataSection.SERIALIZER);
			if (animationMetadata == null) {
				animationMetadata = AnimationMetadataSection.EMPTY;
			}

			Pair<Integer, Integer> frameSize = animationMetadata.getFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
			int frameWidth = frameSize.getFirst();
			int frameHeight = frameSize.getSecond();
			int targetFrameWidth = sprite.getWidth();
			int targetFrameHeight = sprite.getHeight();
			if (frameWidth != targetFrameWidth || frameHeight != targetFrameHeight) {
				int imageWidth = nativeImage.getWidth();
				int imageHeight = nativeImage.getHeight();

				// We can assume the following is always true as a result of getFrameSize's check:
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

				if (animationMetadata != AnimationMetadataSection.EMPTY) {
					AnimationMetadataSectionAccessor animationAccessor = (AnimationMetadataSectionAccessor) animationMetadata;
					int internalFrameWidth = animationAccessor.getFrameWidth();
					int internalFrameHeight = animationAccessor.getFrameHeight();
					if (internalFrameWidth != -1) {
						animationAccessor.setFrameWidth(frameWidth);
					}
					if (internalFrameHeight != -1) {
						animationAccessor.setFrameHeight(frameHeight);
					}
				}
			}

			ResourceLocation pbrSpriteName = new ResourceLocation(spriteName.getNamespace(), spriteName.getPath() + pbrType.getSuffix());
			TextureAtlasSprite.Info pbrSpriteInfo = new PBRTextureAtlasSpriteInfo(pbrSpriteName, frameWidth, frameHeight, animationMetadata, pbrType);

			int x = ((TextureAtlasSpriteAccessor) sprite).getX();
			int y = ((TextureAtlasSpriteAccessor) sprite).getY();
			pbrSprite = new PBRTextureAtlasSprite(atlas, pbrSpriteInfo, mipLevel, atlasWidth, atlasHeight, x, y, nativeImage);
			syncAnimation(sprite, pbrSprite);
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
		Tickable sourceTicker = source.getAnimationTicker();
		Tickable targetTicker = target.getAnimationTicker();
		if (!(sourceTicker instanceof AnimatedTextureAccessor) || !(targetTicker instanceof AnimatedTextureAccessor)) {
			return;
		}

		AnimatedTextureAccessor sourceAccessor = (AnimatedTextureAccessor) sourceTicker;

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += ((FrameInfoAccessor) sourceAccessor.getFrames().get(f)).getTime();
		}

		AnimatedTextureAccessor targetAccessor = (AnimatedTextureAccessor) targetTicker;
		List<Object> targetFrames = targetAccessor.getFrames();

		int cycleTime = 0;
		int frameCount = targetFrames.size();
		for (int f = 0; f < frameCount; f++) {
			cycleTime += ((FrameInfoAccessor) targetFrames.get(f)).getTime();
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = ((FrameInfoAccessor) targetFrames.get(targetFrame)).getTime();
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

	protected static class PBRTextureAtlasSpriteInfo extends TextureAtlasSprite.Info {
		protected final PBRType pbrType;

		public PBRTextureAtlasSpriteInfo(ResourceLocation name, int width, int height, AnimationMetadataSection metadata, PBRType pbrType) {
			super(name, width, height, metadata);
			this.pbrType = pbrType;
		}
	}

	public static class PBRTextureAtlasSprite extends TextureAtlasSprite implements CustomMipmapGenerator.Provider {
		protected PBRTextureAtlasSprite(TextureAtlas atlas, TextureAtlasSprite.Info info, int mipLevel, int atlasWidth, int atlasHeight, int x, int y, NativeImage nativeImage) {
			super(atlas, info, mipLevel, atlasWidth, atlasHeight, x, y, nativeImage);
		}

		@Override
		public CustomMipmapGenerator getMipmapGenerator(Info info, int atlasWidth, int atlasHeight) {
			if (info instanceof PBRTextureAtlasSpriteInfo) {
				PBRType pbrType = ((PBRTextureAtlasSpriteInfo) info).pbrType;
				TextureFormat format = TextureFormatLoader.getFormat();
				if (format != null) {
					CustomMipmapGenerator generator = format.getMipmapGenerator(pbrType);
					if (generator != null) {
						return generator;
					}
				}
			}
			return LINEAR_MIPMAP_GENERATOR;
		}
	}
}
