package net.coderbot.iris.texture.atlas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.TextureUtil;

import net.coderbot.iris.mixin.pbr.TextureAtlasPreparationsAccessor;
import net.coderbot.iris.texture.PBRType;
import net.coderbot.iris.texture.util.TextureColorUtil;
import net.coderbot.iris.texture.util.TextureSaveUtil;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class PBRAtlasTexture extends AbstractTexture {
	protected final TextureAtlas atlasTexture;
	protected final PBRType type;
	protected final ResourceLocation id;
	protected final Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
	protected final Set<TextureAtlasSprite> animatedSprites = new HashSet<>();

	public PBRAtlasTexture(TextureAtlas atlasTexture, PBRType type) {
		this.atlasTexture = atlasTexture;
		this.type = type;
		id = type.appendToFileLocation(atlasTexture.location());
	}

	public PBRType getType() {
		return type;
	}

	public ResourceLocation getAtlasId() {
		return id;
	}

	public void addSprite(TextureAtlasSprite sprite) {
		sprites.put(sprite.getName(), sprite);
		if (sprite.isAnimation()) {
			animatedSprites.add(sprite);
		}
	}

	@Nullable
	public TextureAtlasSprite getSprite(ResourceLocation id) {
		return sprites.get(id);
	}

	public void clear() {
		sprites.clear();
		animatedSprites.clear();
	}

	public void reload(TextureAtlas.Preparations preparations) {
		int glId = getId();
		TextureAtlasPreparationsAccessor preparationsAccessor = (TextureAtlasPreparationsAccessor) preparations;
		int maxLevel = preparationsAccessor.getMipLevel();
		TextureUtil.prepareImage(glId, maxLevel, preparationsAccessor.getWidth(), preparationsAccessor.getHeight());

		int defaultValue = type.getDefaultValue();
		if (defaultValue != 0) {
			TextureColorUtil.fillWithColor(glId, maxLevel, defaultValue);
		}

		for (TextureAtlasSprite sprite : sprites.values()) {
			try {
				sprite.uploadFirstFrame();
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.forThrowable(throwable, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", id);
				crashReportCategory.setDetail("Sprite", sprite);
				throw new ReportedException(crashReport);
			}
		}

		if (Boolean.parseBoolean(System.getProperty("iris.pbr.debug"))) {
			TextureSaveUtil.saveTextures("atlas", id.getPath().replaceAll("/", "_"), glId, preparationsAccessor.getMipLevel(), preparationsAccessor.getWidth(), preparationsAccessor.getHeight());
		}
	}

	public void cycleAnimationFrames() {
		bind();
		for (TextureAtlasSprite sprite : animatedSprites) {
			sprite.cycleFrames();
		}
	}

	@Override
	public void load(ResourceManager manager) {
	}
}
