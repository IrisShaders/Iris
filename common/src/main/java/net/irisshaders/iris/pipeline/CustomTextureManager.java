package net.irisshaders.iris.pipeline;

import com.mojang.blaze3d.opengl.GlStateManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.texture.GlTexture;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.texture.TextureWrapper;
import net.irisshaders.iris.mixin.GlStateManagerAccessor;
import net.irisshaders.iris.mixin.LightTextureAccessor;
import net.irisshaders.iris.pbr.format.TextureFormat;
import net.irisshaders.iris.pbr.format.TextureFormatLoader;
import net.irisshaders.iris.pbr.texture.PBRAtlasTexture;
import net.irisshaders.iris.pbr.texture.PBRTextureHolder;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.texture.CustomTextureData;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.backed.NativeImageBackedCustomTexture;
import net.irisshaders.iris.targets.backed.NativeImageBackedNoiseTexture;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.GL46C;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

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
								Object2ObjectMap<String, CustomTextureData> irisCustomTextureDataMap, CustomTextureData customNoiseTextureData) {
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

		if (customNoiseTextureData == null) {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			NativeImageBackedNoiseTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
			ownedTextures.add(texture);

			noise = texture;
		} else {
			try {
				noise = createCustomTexture(customNoiseTextureData);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private TextureAccess createCustomTexture(CustomTextureData textureData) throws IOException, ResourceLocationException {
		if (textureData instanceof CustomTextureData.PngData) {
			NativeImageBackedCustomTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.LightmapMarker) {
			// Special code path for the light texture. While shader packs hardcode the primary light texture, it's
			// possible that a mod will create a different light texture, so this code path is robust to that.
			return new TextureWrapper(() -> ((LightTextureAccessor) Minecraft.getInstance().gameRenderer.lightTexture())
				.getLightTexture().iris$getGlId(), TextureType.TEXTURE_2D);
		} else if (textureData instanceof CustomTextureData.RawData1D rawData1D) {
			GlTexture texture = new GlTexture(TextureType.TEXTURE_1D, rawData1D.getSizeX(), 0, 0, rawData1D.getInternalFormat().getGlFormat(), rawData1D.getPixelFormat().getGlFormat(), rawData1D.getPixelType().getGlFormat(), rawData1D.getContent(), rawData1D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawDataRect rawDataRect) {
			GlTexture texture = new GlTexture(TextureType.TEXTURE_RECTANGLE, rawDataRect.getSizeX(), rawDataRect.getSizeY(), 0, rawDataRect.getInternalFormat().getGlFormat(), rawDataRect.getPixelFormat().getGlFormat(), rawDataRect.getPixelType().getGlFormat(), rawDataRect.getContent(), rawDataRect.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData2D rawData2D) {
			GlTexture texture = new GlTexture(TextureType.TEXTURE_2D, rawData2D.getSizeX(), rawData2D.getSizeY(), 0, rawData2D.getInternalFormat().getGlFormat(), rawData2D.getPixelFormat().getGlFormat(), rawData2D.getPixelType().getGlFormat(), rawData2D.getContent(), rawData2D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData3D rawData3D) {
			GlTexture texture = new GlTexture(TextureType.TEXTURE_3D, rawData3D.getSizeX(), rawData3D.getSizeY(), rawData3D.getSizeZ(), rawData3D.getInternalFormat().getGlFormat(), rawData3D.getPixelFormat().getGlFormat(), rawData3D.getPixelType().getGlFormat(), rawData3D.getContent(), rawData3D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.ResourceData resourceData) {
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
				ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(namespace, location);

				// NB: We have to re-query the TextureManager for the texture object every time. This is because the
				//     AbstractTexture object could be removed / deleted from the TextureManager on resource reloads,
				//     and we could end up holding on to a deleted texture unless we added special code to handle resource
				//     reloads. Re-fetching the texture from the TextureManager every time is the most robust approach for
				//     now.
				return new TextureWrapper(() -> {
					AbstractTexture texture = textureManager.getTexture(textureLocation);
					if (texture instanceof TextureAtlas || texture instanceof PBRAtlasTexture) {
						int tex = GlStateManagerAccessor.getActiveTexture();
						int binding = GlStateManagerAccessor.getTEXTURES()[tex].binding;
						texture.setFilter(false, Minecraft.getInstance().options.mipmapLevels().get() > 0);
						GlStateManager._activeTexture(GL46C.GL_TEXTURE0 + tex);
						GlStateManager._bindTexture(binding);
					}
					return texture != null ? texture.getTexture().iris$getGlId() : textureManager.getTexture(MissingTextureAtlasSprite.getLocation()).getTexture().iris$getGlId();
				}, TextureType.TEXTURE_2D);
			} else {
				location = location.substring(0, extensionIndex - pbrType.getSuffix().length()) + location.substring(extensionIndex);
				ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(namespace, location);

				return new TextureWrapper(() -> {
					AbstractTexture texture = textureManager.getTexture(textureLocation);

					if (texture != null) {
						if (texture instanceof TextureAtlas || texture instanceof PBRAtlasTexture) {
							int tex = GlStateManagerAccessor.getActiveTexture();
							int binding = GlStateManagerAccessor.getTEXTURES()[tex].binding;
							texture.setFilter(false, Minecraft.getInstance().options.mipmapLevels().get() > 0);
							GlStateManager._activeTexture(GL46C.GL_TEXTURE0 + tex);
							GlStateManager._bindTexture(binding);
						}
						int id = texture.getTexture().iris$getGlId();
						PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(id);
						AbstractTexture pbrTexture = switch (pbrType) {
							case NORMAL -> pbrHolder.normalTexture();
							case SPECULAR -> pbrHolder.specularTexture();
						};

						TextureFormat textureFormat = TextureFormatLoader.getFormat();
						if (textureFormat != null) {
							int previousBinding = GlStateManagerAccessor.getTEXTURES()[GlStateManagerAccessor.getActiveTexture()].binding;
							GlStateManager._bindTexture(pbrTexture.getTexture().iris$getGlId());
							textureFormat.setupTextureParameters(pbrType, pbrTexture);
							GlStateManager._bindTexture(previousBinding);
						}

						return pbrTexture.getTexture().iris$getGlId();
					}

					return textureManager.getTexture(MissingTextureAtlasSprite.getLocation()).getTexture().iris$getGlId();
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
