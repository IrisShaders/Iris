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

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class CustomTextureManager {
	private final Object2ObjectMap<TextureStage, Object2ObjectMap<String, Supplier<AbstractTexture>>> customTexturesMap = new Object2ObjectOpenHashMap<>();
	private final AbstractTexture noise;
	private final NativeImageBackedSingleColorTexture normals;
	private final NativeImageBackedSingleColorTexture specular;

	public CustomTextureManager(PackDirectives packDirectives, Object2ObjectMap<TextureStage, Object2ObjectMap<String, CustomTextureData>> customTextureDataMap, Optional<CustomTextureData> customNoiseTextureData) {
		customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
			Object2ObjectMap<String, Supplier<AbstractTexture>> abstractCustomTextures = new Object2ObjectOpenHashMap<>();
			customTextureStageDataMap.forEach((samplerName, textureData) -> {
				if (textureData instanceof CustomTextureData.PngData) {
					try {
						AbstractTexture abstractTexture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
						abstractCustomTextures.put(samplerName, () -> abstractTexture);
					} catch (IOException e) {
						Iris.logger.error("Unable to parse the image data for the custom texture on stage " + textureStage + ", sampler " + samplerName, e);
					}
				} else if (textureData instanceof CustomTextureData.ResourceData) {
					abstractCustomTextures.put(samplerName, () -> Minecraft.getInstance().getTextureManager().getTexture(((CustomTextureData.ResourceData) textureData).getResourceLocation()));
				}
			});

			customTexturesMap.put(textureStage, abstractCustomTextures);
		});

		noise = customNoiseTextureData.flatMap(textureData -> {
			try {
				// TODO: Support CustomTextureData types other than PngData
				AbstractTexture customNoiseTexture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);

				return Optional.of(customNoiseTexture);
			} catch (IOException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);
				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			return new NativeImageBackedNoiseTexture(noiseTextureResolution);
		});

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);
	}

	public Object2ObjectMap<TextureStage, Object2ObjectMap<String, Supplier<AbstractTexture>>> getCustomTexturesMap() {
		return customTexturesMap;
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
		customTexturesMap.forEach((textureStage, customTextureStageIdMap) -> customTextureStageIdMap.forEach((samplerName, textureData) -> textureData.get().close()));
		normals.close();
		specular.close();
		noise.close();
	}
}
