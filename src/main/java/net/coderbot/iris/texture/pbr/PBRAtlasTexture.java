package net.coderbot.iris.texture.pbr;

import com.mojang.blaze3d.platform.TextureUtil;
import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.texture.AnimatedTextureAccessor;
import net.coderbot.iris.mixin.texture.FrameInfoAccessor;
import net.coderbot.iris.mixin.texture.SpriteContentsAccessor;
import net.coderbot.iris.mixin.texture.TickerAccessor;
import net.coderbot.iris.texture.pbr.loader.AtlasPBRLoader;
import net.coderbot.iris.texture.util.TextureExporter;
import net.coderbot.iris.texture.util.TextureManipulationUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PBRAtlasTexture extends AbstractTexture implements Dumpable {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final ResourceLocation id;
	protected final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
	protected final Map<TextureAtlasSprite, SpriteContents.Ticker> animationTickers = new HashMap<>();

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

	public void addSprite(TextureAtlasSprite sprite) {
		sprites.put(sprite.contents().name(), sprite);
		if (sprite.createTicker() != null) {
			animationTickers.put(sprite, (SpriteContents.Ticker) sprite.contents().createTicker());
		}
	}

	@Nullable
	public TextureAtlasSprite getSprite(ResourceLocation id) {
		return sprites.get(id);
	}

	public void clear() {
		sprites.clear();
		animationTickers.clear();
	}

	public void upload(int atlasWidth, int atlasHeight, int mipLevel) {
		int glId = getId();
		TextureUtil.prepareImage(glId, mipLevel, atlasWidth, atlasHeight);
		TextureManipulationUtil.fillWithColor(glId, mipLevel, type.getDefaultValue());

		for (TextureAtlasSprite sprite : sprites.values()) {
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
			//Iris.logger.warn("ATLAS " + id.toString() + " with widthheightmip " + atlasWidth + " " + atlasHeight + " " + mipLevel);
			upload(atlasWidth, atlasHeight, mipLevel);
			return true;
		} catch (Throwable t) {
			Iris.logger.error("Error loading PBR atlas: ", t);
			return false;
		}
	}

	protected void uploadSprite(TextureAtlasSprite sprite) {
		SpriteContents.AnimatedTexture ticker = ((SpriteContentsAccessor) sprite.contents()).getAnimatedTexture();
		if (ticker instanceof AnimatedTextureAccessor && animationTickers.containsKey(sprite) && getFrameFromSprite(sprite) != 0) {
			AnimatedTextureAccessor accessor = (AnimatedTextureAccessor) ticker;

			accessor.invokeUploadFrame(((FrameInfoAccessor) accessor.getFrames().get(getFrameFromSprite(sprite))).getIndex(), sprite.getX(), sprite.getY());
		}

		//Iris.logger.warn("Sprite " + sprite.contents().name().toString() + " with xy " + sprite.getX() + " " + sprite.getY() + " with widthheight" + sprite.contents().width() + " " + sprite.contents().height());
 		sprite.uploadFirstFrame();
	}

	public int getFrameFromSprite(TextureAtlasSprite sprite) {
		if (animationTickers.containsKey(sprite)) {
			return ((TickerAccessor) animationTickers.get(sprite)).getFrame();
		}
		return 0;
	}

	public int getSubFrameFromSprite(TextureAtlasSprite sprite) {
		if (animationTickers.containsKey(sprite)) {
			return ((TickerAccessor) animationTickers.get(sprite)).getSubFrame();
		}
		return 0;
	}

	public void setFrameOnSprite(TextureAtlasSprite sprite, int frame) {
		if (animationTickers.containsKey(sprite)) {
			((TickerAccessor) animationTickers.get(sprite)).setFrame(frame);
		}
	}

	public void setSubFrameOnSprite(TextureAtlasSprite sprite, int frame) {
		if (animationTickers.containsKey(sprite)) {
			((TickerAccessor) animationTickers.get(sprite)).setSubFrame(frame);
		}
	}

	public void cycleAnimationFrames() {
		bind();
		animationTickers.forEach((textureAtlasSprite, ticker) -> {
			ticker.tickAndUpload(textureAtlasSprite.getX(), textureAtlasSprite.getY());
		});
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
	}

	@Override
	public void load(ResourceManager manager) {
	}

	@Override
	public void dumpContents(ResourceLocation location, Path path) throws IOException {
		String lvString3 = getAtlasId().toDebugFileName();
		TextureUtil.writeAsPNG(path, lvString3, this.getId(), AtlasPBRLoader.fetchAtlasMipLevel(atlasTexture), ((TextureAtlasExtension) atlasTexture).getWidth(), ((TextureAtlasExtension) atlasTexture).getHeight());
		dumpSpriteNames(path, lvString3, this.sprites);
	}

	private static void dumpSpriteNames(Path pPath0, String pString1, Map<ResourceLocation, TextureAtlasSprite> pMap2) {
		Path lvPath3 = pPath0.resolve(pString1 + ".txt");
		try (BufferedWriter lvWriter4 = Files.newBufferedWriter(lvPath3, new OpenOption[0]);){
			for (Map.Entry lvMap$Entry6 : pMap2.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
				TextureAtlasSprite lvTextureAtlasSprite7 = (TextureAtlasSprite)lvMap$Entry6.getValue();
				lvWriter4.write(String.format(Locale.ROOT, "%s\tx=%d\ty=%d\tw=%d\th=%d%n", lvMap$Entry6.getKey(), lvTextureAtlasSprite7.getX(), lvTextureAtlasSprite7.getY(), lvTextureAtlasSprite7.contents().width(), lvTextureAtlasSprite7.contents().height()));
			}
		}
		catch (IOException lvIOException4) {
			Iris.logger.warn("Failed to write file {}", lvPath3, lvIOException4);
		}
	}
}
