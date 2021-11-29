package net.coderbot.iris.pipeline;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

public class CustomTextureManager {
	private final Object2ObjectMap<TextureStage, Object2ObjectMap<String, IntSupplier>> customTextureIdMap = new Object2ObjectOpenHashMap<>();
	private final AbstractTexture noise;
	private final NativeImageBackedSingleColorTexture normals;
	private final NativeImageBackedSingleColorTexture specular;
	private final List<AbstractTexture> pngBackedTextures = new ArrayList<>();

	public CustomTextureManager(PackDirectives packDirectives, Object2ObjectMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap, Optional<CustomTextureData> customNoiseTextureData) {
		customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
			Object2ObjectMap<String, IntSupplier> customTextureIds = new Object2ObjectOpenHashMap<>();
			customTextureStageDataMap.forEach((samplerName, textureData) -> {
				if (textureData instanceof CustomTextureData.PngData) {
					try {
						AbstractTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
						pngBackedTextures.add(texture);
						customTextureIds.put(samplerName, texture::getId);
					} catch (IOException e) {
						Iris.logger.error("Unable to parse the image data for the custom texture on stage " + textureStage + ", sampler " + samplerName, e);
					}
				} else if (textureData instanceof CustomTextureData.ResourceData) {
					AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(((CustomTextureData.ResourceData) textureData).getResourceLocation());

					// TODO: Should we give something else if the texture isn't there? This will need some thought
					IntSupplier glId = texture != null ? texture::getId : MissingTextureAtlasSprite.getTexture()::getId;
					customTextureIds.put(samplerName, glId);
				}
			});

			customTextureIdMap.put(textureStage, customTextureIds);
		});

		noise = customNoiseTextureData.flatMap(textureData -> {
			if (textureData instanceof CustomTextureData.PngData) {
				try {
					AbstractTexture customNoiseTexture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
					pngBackedTextures.add(customNoiseTexture);

					return Optional.of(customNoiseTexture);
				} catch (IOException e) {
					Iris.logger.error("Unable to parse the image data for the custom noise texture", e);
					return Optional.empty();
				}
			} else if (textureData instanceof CustomTextureData.ResourceData) {
				AbstractTexture customNoiseTexture = Minecraft.getInstance().getTextureManager().getTexture(((CustomTextureData.ResourceData) textureData).getResourceLocation());

				// TODO: Should we give something else if the texture isn't there? This will need some thought
				return Optional.of(customNoiseTexture != null ? customNoiseTexture : MissingTextureAtlasSprite.getTexture());
			}
			return Optional.empty();
		}).orElseGet(() -> {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			return new NativeImageBackedNoiseTexture(noiseTextureResolution);
		});

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);
	}

	public Object2ObjectMap<TextureStage, Object2ObjectMap<String, IntSupplier>> getCustomTextureIdMap() {
		return customTextureIdMap;
	}

	public AbstractTexture getNoiseTexture() {
		return noise;
	}

	public NativeImageBackedSingleColorTexture getNormals() {
		return normals;
	}

	public NativeImageBackedSingleColorTexture getSpecular() {
		return specular;
	}

	public void destroy() {
		pngBackedTextures.forEach(AbstractTexture::close);
		normals.close();
		specular.close();
	}
}
