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
	private final IntSupplier noise;
	private final NativeImageBackedSingleColorTexture normals;
	private final NativeImageBackedSingleColorTexture specular;

	/**
	 * List of all OpenGL texture objects owned by this CustomTextureManager that need to be deleted in order to avoid
	 * leaks.
	 */
	private final List<AbstractTexture> ownedTextures = new ArrayList<>();

	public CustomTextureManager(PackDirectives packDirectives,
								Object2ObjectMap<TextureStage,Object2ObjectMap<String, CustomTextureData>> customTextureDataMap,
								Optional<CustomTextureData> customNoiseTextureData) {
		customTextureDataMap.forEach((textureStage, customTextureStageDataMap) -> {
			Object2ObjectMap<String, IntSupplier> customTextureIds = new Object2ObjectOpenHashMap<>();

			customTextureStageDataMap.forEach((samplerName, textureData) -> {
				try {
					customTextureIds.put(samplerName, createCustomTexture(textureData));
				} catch (IOException e) {
					Iris.logger.error("Unable to parse the image data for the custom texture on stage "
							+ textureStage + ", sampler " + samplerName, e);
				}
			});

			customTextureIdMap.put(textureStage, customTextureIds);
		});

		noise = customNoiseTextureData.flatMap(textureData -> {
			try {
				return Optional.of(createCustomTexture(textureData));
			} catch (IOException e) {
				Iris.logger.error("Unable to parse the image data for the custom noise texture", e);

				return Optional.empty();
			}
		}).orElseGet(() -> {
			final int noiseTextureResolution = packDirectives.getNoiseTextureResolution();

			AbstractTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
			ownedTextures.add(texture);

			return texture::getId;
		});

		// Create some placeholder PBR textures for now
		normals = new NativeImageBackedSingleColorTexture(127, 127, 255, 255);
		specular = new NativeImageBackedSingleColorTexture(0, 0, 0, 0);

		ownedTextures.add(normals);
		ownedTextures.add(specular);
	}

	private IntSupplier createCustomTexture(CustomTextureData textureData) throws IOException {
		if (textureData instanceof CustomTextureData.PngData) {
			AbstractTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.ResourceData) {
			AbstractTexture texture = Minecraft.getInstance().getTextureManager()
					.getTexture(((CustomTextureData.ResourceData) textureData).getResourceLocation());

			// TODO: Should we give something else if the texture isn't there? This will need some thought
			return texture != null ? texture::getId : MissingTextureAtlasSprite.getTexture()::getId;
		} else {
			throw new IllegalArgumentException("Unable to handle custom texture data " + textureData);
		}
	}

	public Object2ObjectMap<TextureStage, Object2ObjectMap<String, IntSupplier>> getCustomTextureIdMap() {
		return customTextureIdMap;
	}

	public IntSupplier getNoiseTexture() {
		return noise;
	}

	public NativeImageBackedSingleColorTexture getNormals() {
		return normals;
	}

	public NativeImageBackedSingleColorTexture getSpecular() {
		return specular;
	}

	public void destroy() {
		ownedTextures.forEach(AbstractTexture::close);
	}
}
