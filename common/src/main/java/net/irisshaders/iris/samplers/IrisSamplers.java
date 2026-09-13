package net.irisshaders.iris.samplers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.IrisBindings;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.targets.CustomImage;
import net.irisshaders.iris.targets.RenderTarget;
import net.irisshaders.iris.targets.RenderTargets;
import net.minecraft.client.renderer.texture.AbstractTexture;

import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Supplier;

public class IrisSamplers {
	public static final int ALBEDO_TEXTURE_UNIT = 0;
	public static final int OVERLAY_TEXTURE_UNIT = 1;
	public static final int LIGHTMAP_TEXTURE_UNIT = 2;

	public static final ImmutableSet<Integer> WORLD_RESERVED_TEXTURE_UNITS = ImmutableSet.of(0, 1, 2, IrisBindings.AUX_TEXTURE);
	public static final ImmutableSet<Integer> SODIUM_RESERVED_TEXTURE_UNITS = ImmutableSet.of(0, 2);

	public static final ImmutableSet<Integer> COMPOSITE_RESERVED_TEXTURE_UNITS = ImmutableSet.of();

	private static final GpuSampler[] terrain = new GpuSampler[16];

	private IrisSamplers() {
		// no construction allowed
	}

	public static void initRenderer() {
	}

	public static void addRenderTargetSamplers(SamplerHolder samplers, Supplier<ImmutableSet<Integer>> flipped,
		RenderTargets renderTargets, boolean isFullscreenPass, WorldRenderingPipeline pipeline) {
		// colortex0,1,2,3 are only able to be sampled from fullscreen passes.
		// Iris could lift this restriction, though I'm not sure if it could cause issues.
		int startIndex = isFullscreenPass ? 0 : 4;

		for (int i = startIndex; i < renderTargets.getRenderTargetCount(); i++) {
			final int index = i;

			SamplerHolder.TextureBinding texture = new SamplerHolder.TextureBinding(
				"colortex" + index + (flipped.get().contains(index) ? "/alt" : "/main"), () -> {
				boolean alt = flipped.get().contains(index);
				RenderTarget target = renderTargets.getOrCreate(index);
				GlSampler sampler = alt ? target.getAltSampler() : target.getMainSampler();

				return target.getTextureView(alt, sampler.description().mipmapped());
			}, () -> flipped.get().contains(index) ? renderTargets.getOrCreate(index).getAltSampler() :
				renderTargets.getOrCreate(index).getMainSampler());

			final String name = "colortex" + i;

			// TODO: How do custom textures interact with aliases?

			if (i < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				String legacyName = PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(i);

				if (samplers.hasSampler(legacyName) || samplers.hasSampler(name)) {
					renderTargets.createIfUnsure(index);
				}

				// colortex0 is the default sampler in fullscreen passes
				if (i == 0 && isFullscreenPass) {
					samplers.addTextureSampler(texture, true, name, legacyName);
				} else {
					samplers.addTextureSampler(texture, false, name, legacyName);
				}
			} else {
				if (samplers.hasSampler(name)) {
					renderTargets.createIfUnsure(index);
				}

				samplers.addTextureSampler(texture, false, name);
			}
		}

		// Add the DH texture here, to make sure it's always visible.
		samplers.addDynamicSampler(TextureType.TEXTURE_2D, () -> pipeline.getDHCompat().getDepthTex(), null, "dhDepthTex", "dhDepthTex0");
		samplers.addDynamicSampler(TextureType.TEXTURE_2D, () -> pipeline.getDHCompat().getDepthTexNoTranslucent(), null, "dhDepthTex1");
	}

	public static void addNoiseSampler(SamplerHolder samplers, TextureAccess sampler) {
		var binding = sampler.samplerBinding();
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding(List.of(sampler, "noise"), binding.texture(), () -> GlSampler.LINEAR_REPEAT,
				binding.nativeSampler(), binding.image()), false, "noisetex");
	}

	public static boolean hasShadowSamplers(SamplerHolder samplers) {
		// TODO: Keep this up to date with the actual definitions.
		// TODO: Don't query image presence using the sampler interface even though the current underlying implementation
		//       is the same.
		ImmutableList.Builder<String> shadowSamplers = ImmutableList.<String>builder()
			.add("shadowtex0", "shadowtex0DH", "shadowtex0HW", "shadowtex1", "shadowtex1HW", "shadowtex1DH", "shadow", "watershadow",
				"shadowcolor");

		for (int i = 0; i < PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS; i++) {
			shadowSamplers.add("shadowcolor" + i);
			shadowSamplers.add("shadowcolorimg" + i);
		}

		for (String samplerName : shadowSamplers.build()) {
			if (samplers.hasSampler(samplerName)) {
				return true;
			}
		}

		return false;
	}

	public static boolean addShadowSamplers(SamplerHolder samplers, ShadowRenderTargets shadowRenderTargets, ImmutableSet<Integer> flipped,
		boolean separateHardwareSamplers) {
		boolean usesShadows;

		// TODO: figure this out from parsing the shader source code to be 100% compatible with the legacy
		// shader packs that rely on this behavior.
		boolean waterShadowEnabled = samplers.hasSampler("watershadow");

		if (waterShadowEnabled) {
			usesShadows = true;
			samplers.addTextureSampler(new SamplerHolder.TextureBinding("shadowtex0", () -> shadowRenderTargets.getDepthTextureView(0),
					() -> separateHardwareSamplers ? GlSampler.NEAREST : shadowRenderTargets.getSamplerFor(0)), false, "shadowtex0",
				"watershadow");
			samplers.addTextureSampler(new SamplerHolder.TextureBinding("shadowtex1", () -> shadowRenderTargets.getDepthTextureView(1),
					() -> separateHardwareSamplers ? GlSampler.NEAREST : shadowRenderTargets.getSamplerFor(1)), false,
				"shadowtex1", "shadow");
		} else {
			usesShadows = samplers.addTextureSampler(
				new SamplerHolder.TextureBinding("shadowtex0", () -> shadowRenderTargets.getDepthTextureView(0),
					() -> separateHardwareSamplers ? GlSampler.NEAREST : shadowRenderTargets.getSamplerFor(0)), false, "shadowtex0",
				"shadow");
			usesShadows |= samplers.addTextureSampler(
				new SamplerHolder.TextureBinding("shadowtex1", () -> shadowRenderTargets.getDepthTextureView(1),
					() -> separateHardwareSamplers ? GlSampler.NEAREST : shadowRenderTargets.getSamplerFor(1)), false, "shadowtex1");
		}

		for (int i = 0; i < shadowRenderTargets.getRenderTargetCount(); i++) {
			int index = i;
			String[] names = i == 0 ? new String[]{"shadowcolor", "shadowcolor0"} : new String[]{"shadowcolor" + i};
			Object key = "shadowcolor" + i + (flipped == null ? "/current" : flipped.contains(i) ? "/alt" : "/main");

			if (samplers.addTextureSampler(new SamplerHolder.TextureBinding(key,
				() -> shadowRenderTargets.get(index)
					.getTextureView(flipped == null ? shadowRenderTargets.isFlipped(index) : flipped.contains(index), false),
				() -> GlSampler.LINEAR), false, names)) {
				shadowRenderTargets.createIfEmpty(index);
			}
		}

		if (shadowRenderTargets.isHardwareFiltered(0) && separateHardwareSamplers) {
			samplers.addTextureSampler(new SamplerHolder.TextureBinding("shadowtex0HW", () -> shadowRenderTargets.getDepthTextureView(0),
				() -> shadowRenderTargets.getSamplerFor(0)), false, "shadowtex0HW");
		}

		if (shadowRenderTargets.isHardwareFiltered(1) && separateHardwareSamplers) {
			samplers.addTextureSampler(new SamplerHolder.TextureBinding("shadowtex1HW", () -> shadowRenderTargets.getDepthTextureView(1),
				() -> shadowRenderTargets.getSamplerFor(1)), false, "shadowtex1HW");
		}

		return usesShadows;
	}

	public static boolean hasPBRSamplers(SamplerHolder samplers) {
		return samplers.hasSampler("normals") || samplers.hasSampler("specular");
	}

	public static void addLevelSamplers(SamplerHolder samplers, WorldRenderingPipeline pipeline, AbstractTexture whitePixel,
		boolean hasTexture, boolean hasLightmap, boolean hasOverlay) {
		if (hasTexture) {
			samplers.addExternalSampler(ALBEDO_TEXTURE_UNIT, "tex", "texture", "gtexture", "u_MainSampler", "Sampler0");
		} else {
			// TODO: Rebind unbound sampler IDs instead of hardcoding a list...
			samplers.addTextureSampler(new SamplerHolder.TextureBinding(whitePixel, whitePixel::getTextureView, () -> GlSampler.NEAREST),
				false, "tex", "texture", "gtexture", "u_MainSampler", "Sampler0",
				"gcolor", "colortex0");
		}

		if (hasLightmap) {
			samplers.addExternalSampler(LIGHTMAP_TEXTURE_UNIT, "lightmap", "Sampler2");
		} else {
			samplers.addTextureSampler(new SamplerHolder.TextureBinding(whitePixel, whitePixel::getTextureView, () -> GlSampler.NEAREST),
				false, "lightmap", "Sampler2");
		}

		if (hasOverlay) {
			samplers.addExternalSampler(OVERLAY_TEXTURE_UNIT, "iris_overlay", "Sampler1");
		} else {
			samplers.addTextureSampler(new SamplerHolder.TextureBinding(whitePixel, whitePixel::getTextureView, () -> GlSampler.NEAREST),
				false, "iris_overlay", "Sampler1");
		}

		if (pipeline instanceof IrisRenderingPipeline irp) {
			samplers.addTextureSampler(
				new SamplerHolder.TextureBinding("normals", irp::normalTextureView, irp::getNormalSampler, irp::normalNativeSampler),
				false,
				"normals");
			samplers.addTextureSampler(
				new SamplerHolder.TextureBinding("specular", irp::specularTextureView, irp::getSpecularSampler,
					irp::specularNativeSampler),
				false, "specular");
		}
	}

	public static void addWorldDepthSamplers(SamplerHolder samplers, RenderTargets renderTargets) {
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex0", () -> renderTargets.getDepthTextureView(0), () -> GlSampler.NEAREST), false,
			"depthtex0");
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex1", () -> renderTargets.getDepthTextureView(1), () -> GlSampler.NEAREST), false,
			"depthtex1");
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex2", () -> renderTargets.getDepthTextureView(2), () -> GlSampler.NEAREST), false,
			"depthtex2");
	}

	public static void addCompositeSamplers(SamplerHolder samplers, RenderTargets renderTargets) {
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex0", () -> renderTargets.getDepthTextureView(0), () -> GlSampler.NEAREST), false,
			"gdepthtex", "depthtex0");
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex1", () -> renderTargets.getDepthTextureView(1), () -> GlSampler.NEAREST), false,
			"depthtex1");
		samplers.addTextureSampler(
			new SamplerHolder.TextureBinding("depthtex2", () -> renderTargets.getDepthTextureView(2), () -> GlSampler.NEAREST), false,
			"depthtex2");
	}

	public static void addCustomTextures(SamplerHolder samplers, Object2ObjectMap<String, TextureAccess> irisCustomTextures) {
		irisCustomTextures.forEach((name, texture) -> samplers.addTextureSampler(texture.samplerBinding(), false, name));
	}

	public static void addCustomImages(SamplerHolder images, Set<CustomImage> customImages) {
		customImages.forEach(image -> {
			if (image.getSamplerName() != null) {
				images.addTextureSampler(new SamplerHolder.TextureBinding(image, null,
					() -> image.getInternalFormat().getPixelFormat().isInteger() ? GlSampler.NEAREST : GlSampler.LINEAR, null,
					image::image), false, image.getSamplerName());
			}
		});
	}

	public static GpuSampler getTerrainCache(int i) {
		if (WorldRenderingSettings.INSTANCE.breaksAnisotropy()) {
			i = 1;
		}

		if (terrain[i] == null) {
			terrain[i] = RenderSystem.getDevice()
				.createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, i,
					OptionalDouble.empty());
		}

		return terrain[i];
	}

	public static void closeTerrainSamplers() {
		for (int i = 0; i < terrain.length; i++) {
			if (terrain[i] != null) {
				terrain[i].close();
			}

			terrain[i] = null;
		}
	}
}
