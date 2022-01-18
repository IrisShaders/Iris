package net.coderbot.iris.pipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedNoiseTexture;
import net.coderbot.iris.rendertarget.NativeImageBackedSingleColorTexture;
import net.coderbot.iris.shaderpack.PackDirectives;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.shaderpack.texture.TextureStage;
import net.coderbot.iris.texture.pbr.PBRAtlasHolder;
import net.coderbot.iris.texture.pbr.PBRSimpleTextureHolder;
import net.coderbot.iris.texture.pbr.PBRType;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class CustomTextureManager {
	private final Object2ObjectMap<TextureStage, Object2ObjectMap<String, IntSupplier>> customTextureIdMap = new Object2ObjectOpenHashMap<>();
	private final IntSupplier noise;
	private PBRAtlasHolder atlasHolder;
	private PBRSimpleTextureHolder simpleTextureHolder;
	private final NativeImageBackedSingleColorTexture defaultNormalMap;
	private final NativeImageBackedSingleColorTexture defaultSpecularMap;

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
				} catch (IOException | ResourceLocationException e) {
					Iris.logger.error("Unable to parse the image data for the custom texture on stage "
							+ textureStage + ", sampler " + samplerName, e);
				}
			});

			customTextureIdMap.put(textureStage, customTextureIds);
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

			AbstractTexture texture = new NativeImageBackedNoiseTexture(noiseTextureResolution);
			ownedTextures.add(texture);

			return texture::getId;
		});

		// Create some placeholder PBR textures if some of the PBR textures are missing
		defaultNormalMap = new NativeImageBackedSingleColorTexture(PBRType.NORMAL.getDefaultValue());
		defaultSpecularMap = new NativeImageBackedSingleColorTexture(PBRType.SPECULAR.getDefaultValue());

		ownedTextures.add(defaultNormalMap);
		ownedTextures.add(defaultSpecularMap);
	}

	private IntSupplier createCustomTexture(CustomTextureData textureData) throws IOException, ResourceLocationException {
		if (textureData instanceof CustomTextureData.PngData) {
			AbstractTexture texture = new NativeImageBackedCustomTexture((CustomTextureData.PngData) textureData);
			ownedTextures.add(texture);

			return texture::getId;
		} else if (textureData instanceof CustomTextureData.ResourceData) {
			CustomTextureData.ResourceData resourceData = ((CustomTextureData.ResourceData) textureData);
			String namespace = resourceData.getNamespace();
			String location = resourceData.getLocation();

			ResourceLocation textureLocation = new ResourceLocation(namespace, location);
			TextureManager textureManager = Minecraft.getInstance().getTextureManager();

			// NB: We have to re-query the TextureManager for the texture object every time. This is because the
			//     AbstractTexture object could be removed / deleted from the TextureManager on resource reloads,
			//     and we could end up holding on to a deleted texture unless we added special code to handle resource
			//     reloads. Re-fetching the texture from the TextureManager every time is the most robust approach for
			//     now.
			return () -> {
				AbstractTexture texture = textureManager.getTexture(textureLocation);

				// TODO: Should we give something else if the texture isn't there? This will need some thought
				return texture != null ? texture.getId() : MissingTextureAtlasSprite.getTexture().getId();
			};
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

	public void setAtlas(PBRAtlasHolder holder) {
		this.atlasHolder = holder;
	}

	public void setSimpleTexture(PBRSimpleTextureHolder holder) {
		this.simpleTextureHolder = holder;
	}

	public AbstractTexture getAtlasNormals() {
		return atlasHolder != null && atlasHolder.hasNormalAtlas() ? atlasHolder.getNormalAtlas() : defaultNormalMap;
	}

	public AbstractTexture getSimpleNormals() {
		if (simpleTextureHolder != null && simpleTextureHolder.hasNormalTexture()) {
			return simpleTextureHolder.getNormalTexture();
		}
		return defaultNormalMap;
	}

	public AbstractTexture getAtlasSpecular() {
		return atlasHolder != null && atlasHolder.hasSpecularAtlas() ? atlasHolder.getSpecularAtlas() : defaultSpecularMap;
	}

	public AbstractTexture getSimpleSpecular() {
		if (simpleTextureHolder != null && simpleTextureHolder.hasSpecularTexture()) {
			return simpleTextureHolder.getSpecularTexture();
		}
		return defaultSpecularMap;
	}

	public void destroy() {
		ownedTextures.forEach(AbstractTexture::close);
	}
}
