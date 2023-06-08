package net.coderbot.iris.texture.pbr;

import com.mojang.blaze3d.platform.TextureUtil;
import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.texture.SpriteContentsAnimatedTextureAccessor;
import net.coderbot.iris.mixin.texture.SpriteContentsFrameInfoAccessor;
import net.coderbot.iris.mixin.texture.SpriteContentsTickerAccessor;
import net.coderbot.iris.texture.pbr.loader.AtlasPBRLoader.PBRTextureAtlasSprite;
import net.coderbot.iris.texture.util.TextureManipulationUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.FrameInfo;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
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
		id = new ResourceLocation(atlasTexture.location().getNamespace(), atlasTexture.location().getPath().replace(".png", "") + type.getSuffix() + ".png");
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
		int glId = getId();
		TextureUtil.prepareImage(glId, mipLevel, atlasWidth, atlasHeight);
		TextureManipulationUtil.fillWithColor(glId, mipLevel, type.getDefaultValue());
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
			return false;
		}
	}

	protected void uploadSprite(PBRTextureAtlasSprite sprite) {
		TextureAtlasSprite.Ticker spriteTicker = sprite.createTicker();
		if (spriteTicker != null) {
			animatedTextures.add(spriteTicker);

			SpriteContents.Ticker sourceTicker = ((net.coderbot.iris.texture.SpriteContentsExtension) sprite.getBaseSprite().contents()).getCreatedTicker();
			SpriteContents.Ticker targetTicker = ((net.coderbot.iris.texture.SpriteContentsExtension) sprite.contents()).getCreatedTicker();
			if (sourceTicker != null && targetTicker != null) {
				syncAnimation(sourceTicker, targetTicker);

				SpriteContentsTickerAccessor tickerAccessor = (SpriteContentsTickerAccessor) targetTicker;
				SpriteContentsAnimatedTextureAccessor infoAccessor = (SpriteContentsAnimatedTextureAccessor) tickerAccessor.getAnimationInfo();

				infoAccessor.invokeUploadFrame(sprite.getX(), sprite.getY(), ((SpriteContentsFrameInfoAccessor) infoAccessor.getFrames().get(tickerAccessor.getFrame())).getIndex());
				return;
			}
		}

		sprite.uploadFirstFrame();
	}

	public static void syncAnimation(SpriteContents.Ticker source, SpriteContents.Ticker target) {
		SpriteContentsTickerAccessor sourceAccessor = (SpriteContentsTickerAccessor) source;
		List<FrameInfo> sourceFrames = ((SpriteContentsAnimatedTextureAccessor) sourceAccessor.getAnimationInfo()).getFrames();

		int ticks = 0;
		for (int f = 0; f < sourceAccessor.getFrame(); f++) {
			ticks += ((SpriteContentsFrameInfoAccessor) sourceFrames.get(f)).getTime();
		}

		SpriteContentsTickerAccessor targetAccessor = (SpriteContentsTickerAccessor) target;
		List<FrameInfo> targetFrames = ((SpriteContentsAnimatedTextureAccessor) targetAccessor.getAnimationInfo()).getFrames();

		int cycleTime = 0;
		int frameCount = targetFrames.size();
		for (int f = 0; f < frameCount; f++) {
			cycleTime += ((SpriteContentsFrameInfoAccessor) targetFrames.get(f)).getTime();
		}
		ticks %= cycleTime;

		int targetFrame = 0;
		while (true) {
			int time = ((SpriteContentsFrameInfoAccessor) targetFrames.get(targetFrame)).getTime();
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

	public void cycleAnimationFrames() {
		bind();
		for (TextureAtlasSprite.Ticker ticker : animatedTextures) {
			ticker.tickAndUpload();
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
	public void load(ResourceManager manager) {
	}

	@Override
	public void dumpContents(ResourceLocation id, Path path) throws IOException {
		String fileName = id.toDebugFileName();
		TextureUtil.writeAsPNG(path, fileName, getId(), mipLevel, width, height);
		dumpSpriteNames(path, fileName, texturesByName);
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

	@Override
	public ResourceLocation getDefaultDumpLocation() {
		return id;
	}
}
