package net.coderbot.iris.pipeline;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.gl.texture.GlTexture;
import net.coderbot.iris.gl.texture.TextureAccess;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.gl.texture.TextureWrapper;
import net.coderbot.iris.mixin.LightTextureAccessor;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.texture.format.TextureFormat;
import net.coderbot.iris.texture.format.TextureFormatLoader;
import net.coderbot.iris.texture.pbr.PBRTextureHolder;
import net.coderbot.iris.texture.pbr.PBRTextureManager;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

public class CustomTextureManager {
	private final EnumMap<TextureStage, Object2ObjectMap<String, TextureAccess>> customTextureIdMap = new EnumMap<>(TextureStage.class);
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures = new Object2ObjectOpenHashMap<>();
	private final TextureAccess noise;

	/**
	 * List of all OpenGL texture objects owned by this CustomTextureManager that need to be deleted in order to avoid
	 * leaks.
	 * Make sure any textures added to this list call releaseId from the close method.
	 */
	private final List<AbstractTexture> ownedTextures = new ArrayList<>();
	private final List<GlTexture> ownedRawTextures = new ArrayList<>();

	public CustomTextureManager(PackDirectives packDirectives,
								EnumMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap,
								Object2ObjectMap<String, CustomTextureData> irisCustomTextureDataMap, Optional<CustomTextureData> customNoiseTextureData) {
		customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
			Object2ObjectMap<String, TextureAccess> customTextureIds = new Object2ObjectOpenHashMap<>();

			customTextureStageDataMap.forEach((samplerName, textureData) -> {
				try {
					customTextureIds.put(samplerName, createCustomTexture(textureData));
				} catch (IOException | ResourceLocationException e) {
					Iris.logger.error("Unable to parse the image data for the custom texture on stage "
							+ textureStage + ", sampler " + samplerName, e);
				}
			});

			customTextureIdMap.put(textureStage, customTextureIds);
		});

		irisCustomTextureDataMap.forEach((name, texture) -> {
			try {
				irisCustomTextures.put(name, createCustomTexture(texture));
			} catch (IOException e) {
				Iris.logger.error("Unable to parse the image data for the custom texture on sampler " + name, e);
			}
		});

		noise = customNoiseTextureData.flatMap(textureData -> {
			try {
				return Optional.of(createCustomTexture(textureData));
			} catch (IOException | ResourceLocationException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);

				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			NativeImageBackedNoiseTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
			ownedTextures.add(texture);

			return texture;
		});
	}

	private TextureAccess createCustomTexture(CustomTextureData textureData) throws IOException, ResourceLocationException {
		if (textureData instanceof CustomTextureData.PngData) {
			NativeImageBackedCustomTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.LightmapMarker) {
			// Special code path for the light texture. While shader packs hardcode the primary light texture, it's
			// possible that a mod will create a different light texture, so this code path is robust to that.
			return new TextureWrapper(((LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture())
				.getLightTexture()::getId, TextureType.TEXTURE_2D);
		} else if (textureData instanceof CustomTextureData.RawData1D) {
			CustomTextureData.RawData1D rawData1D = (CustomTextureData.RawData1D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_1D, rawData1D.getSizeX(), 0, 0, rawData1D.getInternalFormat().getGlFormat(), rawData1D.getPixelFormat().getGlFormat(), rawData1D.getPixelType().getGlFormat(), rawData1D.getContent(), rawData1D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawDataRect rawDataRect) {
			GlTexture texture = new GlTexture(TextureType.TEXTURE_RECTANGLE, rawDataRect.getSizeX(), rawDataRect.getSizeY(), 0, rawDataRect.getInternalFormat().getGlFormat(), rawDataRect.getPixelFormat().getGlFormat(), rawDataRect.getPixelType().getGlFormat(), rawDataRect.getContent(), rawDataRect.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData2D) {
			CustomTextureData.RawData2D rawData2D = (CustomTextureData.RawData2D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_2D, rawData2D.getSizeX(), rawData2D.getSizeY(), 0, rawData2D.getInternalFormat().getGlFormat(), rawData2D.getPixelFormat().getGlFormat(), rawData2D.getPixelType().getGlFormat(), rawData2D.getContent(), rawData2D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData3D) {
			CustomTextureData.RawData3D rawData3D = (CustomTextureData.RawData3D) textureData;
			GlTexture texture = new GlTexture(TextureType.TEXTURE_3D, rawData3D.getSizeX(), rawData3D.getSizeY(), rawData3D.getSizeZ(), rawData3D.getInternalFormat().getGlFormat(), rawData3D.getPixelFormat().getGlFormat(), rawData3D.getPixelType().getGlFormat(), rawData3D.getContent(), rawData3D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.ResourceData) {
			CustomTextureData.ResourceData resourceData = (CustomTextureData.ResourceData) textureData;
			String namespace = resourceData.getNamespace();
			String location = resourceData.getLocation();

			String withoutExtension;
			int extensionIndex = FilenameUtils.indexOfExtension(location);
			if (extensionIndex != -1) {
				withoutExtension = location.substring(0, extensionIndex);
			} else {
				withoutExtension = location;
			}
			PBRType pbrType = PBRType.fromFileLocation(withoutExtension);

			TextureManager textureManager = Minecraft.getInstance().getTextureManager();

			if (pbrType == null) {
				ResourceLocation textureLocation = new ResourceLocation(namespace, location);

				// NB: We have to re-query the TextureManager for the texture object every time. This is because the
				//     AbstractTexture object could be removed / deleted from the TextureManager on resource reloads,
				//     and we could end up holding on to a deleted texture unless we added special code to handle resource
				//     reloads. Re-fetching the texture from the TextureManager every time is the most robust approach for
				//     now.
				return new TextureWrapper(() -> {
					AbstractTexture texture = textureManager.getTexture(textureLocation);
					return texture != null ? texture.getId() : MissingTextureAtlasSprite.getTexture().getId();
				}, TextureType.TEXTURE_2D);
			} else {
				location = location.substring(0, extensionIndex - pbrType.getSuffix().length()) + location.substring(extensionIndex);
				ResourceLocation textureLocation = new ResourceLocation(namespace, location);

				return new TextureWrapper(() -> {
					AbstractTexture texture = textureManager.getTexture(textureLocation);

					if (texture != null) {
						int id = texture.getId();
						PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(id);
						AbstractTexture pbrTexture;
						switch (pbrType) {
							case NORMAL:
								pbrTexture = pbrHolder.getNormalTexture();
								break;
							case SPECULAR:
								pbrTexture = pbrHolder.getSpecularTexture();
								break;
							default:
								throw new Error("Unknown PBRType '" + pbrType + "'");
						}

						TextureFormat textureFormat = TextureFormatLoader.getFormat();
						if (textureFormat != null) {
							int previousBinding = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
							GlStateManager._bindTexture(pbrTexture.getId());
							textureFormat.setupTextureParameters(pbrType, pbrTexture);
							GlStateManager._bindTexture(previousBinding);
						}

						return pbrTexture.getId();
					}

					return MissingTextureAtlasSprite.getTexture().getId();
				}, TextureType.TEXTURE_2D);
			}
		}
		throw new IllegalArgumentException("Don't know texture type!");
	}

	public EnumMap<TextureStage, Object2ObjectMap<String, TextureAccess>> getCustomTextureIdMap() {
		return customTextureIdMap;
	}

	public Object2ObjectMap<String, TextureAccess> getCustomTextureIdMap(TextureStage stage) {
		return customTextureIdMap.getOrDefault(stage, Object2ObjectMaps.emptyMap());
	}

	public Object2ObjectMap<String, TextureAccess> getIrisCustomTextures() {
		return irisCustomTextures;
	}

	public TextureAccess getNoiseTexture() {
		return noise;
	}

	public void destroy() {
		ownedTextures.forEach(AbstractTexture::close);
		ownedRawTextures.forEach(GlTexture::destroy);
	}
}
