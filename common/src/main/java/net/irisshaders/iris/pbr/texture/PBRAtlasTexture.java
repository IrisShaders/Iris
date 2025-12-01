package net.irisshaders.iris.pbr.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixin.texture.SpriteContentsAnimatedTextureAccessor;
import net.irisshaders.iris.mixin.texture.SpriteContentsFrameInfoAccessor;
import net.irisshaders.iris.mixin.texture.SpriteContentsTickerAccessor;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.loader.AtlasPBRLoader.PBRTextureAtlasSprite;
import net.irisshaders.iris.pbr.util.TextureManipulationUtil;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.FrameInfo;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PBRAtlasTexture extends AbstractTexture implements PBRDumpable {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final ResourceLocation id;
	protected final Map<ResourceLocation, PBRTextureAtlasSprite> texturesByName = new HashMap<>();
	protected final List<TextureAtlasSprite.Ticker> animatedTextures = new ArrayList<>();
	protected int width;
	protected int height;
	protected int mipLevel;

	public PBRAtlasTexture(TextureAtlas atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		id = ResourceLocation.fromNamespaceAndPath(atlasTexture.location().getNamespace(), atlasTexture.location().getPath().replace(".png", "") + type.getSuffix() + ".png");
	}

	public static void syncAnimation(SpriteContents.Ticker source, SpriteContents.Ticker target) {
		SpriteContentsTickerAccessor sourceAccessor = (SpriteContentsTickerAccessor) source;
		List<FrameInfo> sourceFrames = ((SpriteContentsAnimatedTextureAccessor) sourceAccessor.getAnimationInfo()).getFrames();

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += ((SpriteContentsFrameInfoAccessor) (Object) sourceFrames.get(f)).getTime();
		}

		SpriteContentsTickerAccessor targetAccessor = (SpriteContentsTickerAccessor) target;
		List<FrameInfo> targetFrames = ((SpriteContentsAnimatedTextureAccessor) targetAccessor.getAnimationInfo()).getFrames();

		int cycleTime = 0;
		int frameCount = targetFrames.size();
		for (FrameInfo frame : targetFrames) {
			cycleTime += ((SpriteContentsFrameInfoAccessor) (Object) frame).getTime();
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = ((SpriteContentsFrameInfoAccessor) (Object) targetFrames.get(targetFrame)).getTime();
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

	protected static void dumpSpriteNames(Path dir, String fileName, Map<ResourceLocation, PBRTextureAtlasSprite> sprites) {
		Path path = dir.resolve(fileName + ".txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			for (Map.Entry<ResourceLocation, PBRTextureAtlasSprite> entry : sprites.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				PBRTextureAtlasSprite sprite = entry.getValue();
				writer.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", entry.getKey(), sprite.getX(), sprite.getY(), sprite.contents().width(), sprite.contents().height()));
			}
		} catch (IOException e) {
			Iris.logger.warn("Failed to write file {}", path, e);
		}
	}

	public PBRType getType() {
		return type;
	}

	public ResourceLocation getAtlasId() {
		return id;
	}

	public void addSprite(PBRTextureAtlasSprite sprite) {
		texturesByName.put(sprite.contents().name(), sprite);
	}

	@Nullable
	public PBRTextureAtlasSprite getSprite(ResourceLocation id) {
		return texturesByName.get(id);
	}

	public void clear() {
		animatedTextures.forEach(TextureAtlasSprite.Ticker::close);
		texturesByName.clear();
		animatedTextures.clear();
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		if (this.texture != null) {
			this.texture.close();
		}

		this.texture = RenderSystem.getDevice().createTexture(getAtlasId().toString(), GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING, TextureFormat.RGBA8, atlasWidth, atlasHeight, 1, mipLevel + 1);
		if (TextureFormatLoader.getFormat() != null && !TextureFormatLoader.getFormat().canInterpolateValues(type)) {
			texture.iris$markMipmapNonLinear();
		}
		texture.setTextureFilter(FilterMode.NEAREST, mipLevel > 1);

		TextureManipulationUtil.fillWithColor(texture.iris$getGlId(), mipLevel, type.getDefaultValue());
		TextureTracker.INSTANCE.trackTexture(this.texture.iris$getGlId(), (AbstractTexture) (Object) this);
		width = atlasWidth;
		height = atlasHeight;
		this.mipLevel = mipLevel;

		for (PBRTextureAtlasSprite sprite : texturesByName.values()) {
			try {
				uploadSprite(sprite);
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", id);
				crashReportCategory.setDetail("Sprite", sprite);
				throw new ReportedException(crashReport);
			}
		}

		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getOrCreatePBRHolder();

		switch (type) {
			case NORMAL:
				pbrHolder.setNormalAtlas(this);
				break;
			case SPECULAR:
				pbrHolder.setSpecularAtlas(this);
				break;
		}
	}

	public boolean tryUpload(int atlasWidth, int atlasHeight, int mipLevel) {
		try {
			upload(atlasWidth, atlasHeight, mipLevel);
			return true;
		} catch (Throwable t) {
			if (IrisPlatformHelpers.getInstance().isDevelopmentEnvironment()) {
				t.printStackTrace();
			}
			return false;
		}
	}

	protected void uploadSprite(PBRTextureAtlasSprite sprite) {
		TextureAtlasSprite.Ticker spriteTicker = sprite.createTicker();
		if (spriteTicker != null) {
			animatedTextures.add(spriteTicker);

			SpriteContents.Ticker sourceTicker = ((net.irisshaders.iris.pbr.SpriteContentsExtension) sprite.getBaseSprite().contents()).getCreatedTicker();
			SpriteContents.Ticker targetTicker = ((net.irisshaders.iris.pbr.SpriteContentsExtension) sprite.contents()).getCreatedTicker();
			if (sourceTicker != null && targetTicker != null) {
				syncAnimation(sourceTicker, targetTicker);

				SpriteContentsTickerAccessor tickerAccessor = (SpriteContentsTickerAccessor) targetTicker;
				SpriteContentsAnimatedTextureAccessor infoAccessor = (SpriteContentsAnimatedTextureAccessor) tickerAccessor.getAnimationInfo();

				infoAccessor.invokeUploadFrame(sprite.getX(), sprite.getY(), ((SpriteContentsFrameInfoAccessor) (Object) infoAccessor.getFrames().get(tickerAccessor.getFrame())).getIndex(), texture);
				return;
			}
		}

		sprite.uploadFirstFrame(texture);
	}

	public void cycleAnimationFrames() {
		for (TextureAtlasSprite.Ticker ticker : animatedTextures) {
			ticker.tickAndUpload(texture);
		}
	}

	@Override
	public void close() {
		PBRAtlasHolder pbrHolder = ((TextureAtlasExtension) atlasTexture).getPBRHolder();
		if (pbrHolder != null) {
			switch (type) {
				case NORMAL:
					pbrHolder.setNormalAtlas(null);
					break;
				case SPECULAR:
					pbrHolder.setSpecularAtlas(null);
					break;
			}
		}
		clear();
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) {
		String fileName = id.toDebugFileName();
		TextureUtil.writeAsPNG(path, fileName, texture, mipLevel, i -> i);
		dumpSpriteNames(path, fileName, texturesByName);
	}

	@Override
	public ResourceLocation getDefaultDumpLocation() {
		return id;
	}
}
