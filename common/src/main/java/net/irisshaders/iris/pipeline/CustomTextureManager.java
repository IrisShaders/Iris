package net.irisshaders.iris.pipeline;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.gl.texture.TextureWrapper;
import net.irisshaders.iris.mixin.texture.TextureManagerAccessor;
import net.irisshaders.iris.pbr.texture.PBRTextureHolder;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import net.irisshaders.iris.pbr.texture.PBRType;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.texture.CustomTextureData;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.backed.NativeImageBackedCustomTexture;
import net.irisshaders.iris.targets.backed.NativeImageBackedNoiseTexture;
import net.irisshaders.iris.targets.backed.RawTexture;
import net.minecraft.IdentifierException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class CustomTextureManager {
	private final EnumMap<TextureStage, Object2ObjectMap<String, TextureAccess>> customTextureIdMap = new EnumMap<>(TextureStage.class);
	private final Object2ObjectMap<String, TextureAccess> irisCustomTextures = new Object2ObjectOpenHashMap<>();
	private final TextureAccess noise;

	private final List<AbstractTexture> ownedTextures = new ArrayList<>();
	private final List<RawTexture> ownedRawTextures = new ArrayList<>();

	public CustomTextureManager(PackDirectives packDirectives,
		EnumMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap,
		Object2ObjectMap<String, CustomTextureData> irisCustomTextureDataMap, CustomTextureData customNoiseTextureData) {
		try {
			customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
				Object2ObjectMap<String, TextureAccess> customTextureIds = new Object2ObjectOpenHashMap<>();
				customTextureStageDataMap.forEach((samplerName, textureData) -> {
					try {
						customTextureIds.put(samplerName, this.createCustomTexture(textureData));
					} catch (IOException | IdentifierException e) {
						Iris.logger.error(
							"Unable to parse the image data for the custom texture on stage " + textureStage + ", sampler " + samplerName,
							e);
					}
				});

				this.customTextureIdMap.put(textureStage, customTextureIds);
			});

			irisCustomTextureDataMap.forEach((name, texture) -> {
				try {
					this.irisCustomTextures.put(name, this.createCustomTexture(texture));
				} catch (IOException e) {
					Iris.logger.error("Unable to parse the image data for the custom texture on sampler " + name, e);
				}
			});

			if (customNoiseTextureData == null) {
				final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

				NativeImageBackedNoiseTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
				this.ownedTextures.add(texture);

				this.noise = texture;
			} else {
				try {
					this.noise = this.createCustomTexture(customNoiseTextureData);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (RuntimeException | Error failure) {
			try {
				this.destroy();
			} catch (RuntimeException | Error suppressed) {
				failure.addSuppressed(suppressed);
			}

			throw failure;
		}
	}

	private TextureAccess createCustomTexture(CustomTextureData textureData) throws IOException, IdentifierException {
		if (textureData instanceof CustomTextureData.PngData) {
			NativeImageBackedCustomTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.LightmapMarker) {

			return new TextureWrapper(() -> Minecraft.getInstance().gameRenderer.levelLightmap(), TextureType.TEXTURE_2D);
		} else if (textureData instanceof CustomTextureData.RawData1D rawData1D) {
			RawTexture texture = new RawTexture(TextureType.TEXTURE_1D, rawData1D.getSizeX(), 0, 0, rawData1D.getInternalFormat(),
				rawData1D.getPixelFormat(), rawData1D.getPixelType(), rawData1D.getContent(), rawData1D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawDataRect rawDataRect) {
			RawTexture texture = new RawTexture(TextureType.TEXTURE_RECTANGLE, rawDataRect.getSizeX(), rawDataRect.getSizeY(), 0,
				rawDataRect.getInternalFormat(), rawDataRect.getPixelFormat(), rawDataRect.getPixelType(), rawDataRect.getContent(),
				rawDataRect.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData2D rawData2D) {
			RawTexture texture = new RawTexture(TextureType.TEXTURE_2D, rawData2D.getSizeX(), rawData2D.getSizeY(), 0,
				rawData2D.getInternalFormat(), rawData2D.getPixelFormat(), rawData2D.getPixelType(), rawData2D.getContent(),
				rawData2D.getFilteringData());
			ownedRawTextures.add(texture);

			return texture;
		} else if (textureData instanceof CustomTextureData.RawData3D rawData3D) {
			RawTexture texture = new RawTexture(TextureType.TEXTURE_3D, rawData3D.getSizeX(), rawData3D.getSizeY(), rawData3D.getSizeZ(),
				rawData3D.getInternalFormat(), rawData3D.getPixelFormat(), rawData3D.getPixelType(), rawData3D.getContent(),
				rawData3D.getFilteringData());
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

			if (pbrType != null) {
				location = location.substring(0, extensionIndex - pbrType.getSuffix().length()) + location.substring(extensionIndex);
			}

			Identifier textureLocation = Identifier.fromNamespaceAndPath(namespace, location);
			textureManager.getTexture(textureLocation);
			var missingTexture = textureManager.getTexture(MissingTextureAtlasSprite.getLocation());
			var textures = ((TextureManagerAccessor) textureManager).iris$getTextures();

			return new TextureWrapper(() -> {
				AbstractTexture texture = textures.get(textureLocation);

				if (texture == null) {
					return missingTexture.getTextureView();
				}

				if (pbrType == null) {
					return texture.getTextureView();
				}

				PBRTextureHolder pbrHolder = PBRTextureManager.INSTANCE.getOrLoadHolder(texture.getTexture());
				AbstractTexture pbrTexture = switch (pbrType) {
					case NORMAL -> pbrHolder.normalTexture();
					case SPECULAR -> pbrHolder.specularTexture();
				};

				return pbrTexture.getTextureView();
			}, TextureType.TEXTURE_2D);
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
		this.ownedRawTextures.forEach(RawTexture::destroy);
	}
}
