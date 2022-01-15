package net.coderbot.iris.texture.atlas;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.TextureUtil;

import net.coderbot.iris.texture.PBRType;
import net.coderbot.iris.mixin.pbr.TextureAtlasPreparationsAccessor;
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
	}

	@Nullable
	public TextureAtlasSprite getSprite(ResourceLocation id) {
		return sprites.get(id);
	}

	public void reload(TextureAtlas.Preparations preparations) {
		int glId = getId();
		TextureAtlasPreparationsAccessor dataAccessor = (TextureAtlasPreparationsAccessor) preparations;
		int maxLevel = dataAccessor.getMipLevel();
		TextureUtil.prepareImage(glId, maxLevel, dataAccessor.getWidth(), dataAccessor.getHeight());

		int defaultValue = type.getDefaultValue();
		if (defaultValue != 0) {
			TextureColorUtil.fillWithColor(glId, maxLevel, defaultValue);
		}

		for (TextureAtlasSprite sprite : sprites.values()) {
			try {
				sprite.uploadFirstFrame();
			} catch (Throwable var7) {
				CrashReport crashReport = CrashReport.forThrowable(var7, "Stitching texture atlas");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Texture being stitched together");
				crashReportCategory.setDetail("Atlas path", id);
				crashReportCategory.setDetail("Sprite", sprite);
				throw new ReportedException(crashReport);
			}
		}

		TextureSaveUtil.saveTextures("atlases", id.getPath().replaceAll("/", "_"), getId(), dataAccessor.getMipLevel(), dataAccessor.getWidth(), dataAccessor.getHeight());
	}

	@Override
	public void load(ResourceManager manager) {
	}
}
