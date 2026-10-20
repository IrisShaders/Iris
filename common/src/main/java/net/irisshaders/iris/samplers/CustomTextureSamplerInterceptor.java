package net.irisshaders.iris.samplers;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;


public class CustomTextureSamplerInterceptor implements SamplerHolder {
	private final SamplerHolder samplerHolder;
	private final Object2ObjectMap<String, TextureAccess> customTextureIds;
	private final ImmutableSet<String> deactivatedOverrides;

	public CustomTextureSamplerInterceptor(SamplerHolder samplerHolder, Object2ObjectMap<String, TextureAccess> customTextureIds) {
		this(samplerHolder, customTextureIds, ImmutableSet.of());
	}

	public CustomTextureSamplerInterceptor(SamplerHolder samplerHolder, Object2ObjectMap<String, TextureAccess> customTextureIds,
		ImmutableSet<Integer> flippedAtLeastOnceSnapshot) {
		this.samplerHolder = samplerHolder;
		this.customTextureIds = customTextureIds;

		ImmutableSet.Builder<String> deactivatedOverrides = new ImmutableSet.Builder<>();

		for (int deactivatedOverride : flippedAtLeastOnceSnapshot) {
			deactivatedOverrides.add("colortex" + deactivatedOverride);

			if (deactivatedOverride < PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.size()) {
				deactivatedOverrides.add(PackRenderTargetDirectives.LEGACY_RENDER_TARGETS.get(deactivatedOverride));
			}
		}

		this.deactivatedOverrides = deactivatedOverrides.build();
	}

	@Override
	public boolean addTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		TextureAccess override = this.getOverride(names);

		if (override != null) {
			binding = override.samplerBinding();
		}

		return this.samplerHolder.addTextureSampler(binding, defaultSampler, names);
	}

	private TextureAccess getOverride(String... names) {
		for (String name : names) {
			if (this.customTextureIds.containsKey(name) && !this.deactivatedOverrides.contains(name)) {
				return this.customTextureIds.get(name);
			}
		}

		return null;
	}

	@Override
	public void addExternalSampler(int textureUnit, String... names) {
		TextureAccess override = this.getOverride(names);

		if (override != null) {
			this.samplerHolder.addTextureSampler(override.samplerBinding(), textureUnit == 0, names);
		} else {
			this.samplerHolder.addExternalSampler(textureUnit, names);
		}
	}

	@Override
	public boolean addDrawTextureSampler(TextureBinding binding, boolean defaultSampler, String... names) {
		TextureAccess override = this.getOverride(names);
		return override == null ? this.samplerHolder.addDrawTextureSampler(binding, defaultSampler,
			names) : this.samplerHolder.addTextureSampler(override.samplerBinding(), defaultSampler, names);
	}

	@Override
	public boolean hasSampler(String name) {
		return this.samplerHolder.hasSampler(name);
	}

}
