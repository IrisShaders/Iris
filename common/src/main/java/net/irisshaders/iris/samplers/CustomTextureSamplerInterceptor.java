package net.irisshaders.iris.samplers;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.texture.TextureAccess;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class CustomTextureSamplerInterceptor implements SamplerHolder {
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
		TextureAccess override = getOverride(names);

		if (override != null) {
			binding = override.samplerBinding();
		}

		return samplerHolder.addTextureSampler(binding, defaultSampler, names);
	}

	private TextureAccess getOverride(String... names) {
		for (String name : names) {
			if (customTextureIds.containsKey(name) && !deactivatedOverrides.contains(name)) {
				return customTextureIds.get(name);
			}
		}

		return null;
	}

	@Override
	public void addExternalSampler(int textureUnit, String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			if (textureUnit == 0) {
				samplerHolder.addDefaultSampler(override.getType(), override.getTextureId(), null, () -> override.getSampling(), names);
			} else {
				samplerHolder.addDynamicSampler(override.getType(), override.getTextureId(), null, () -> override.getSampling(), names);
			}
		} else {
			samplerHolder.addExternalSampler(textureUnit, names);
		}
	}

	@Override
	public boolean hasSampler(String name) {
		return samplerHolder.hasSampler(name);
	}

	@Override
	public boolean addDefaultSampler(IntSupplier sampler, String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			return samplerHolder.addDefaultSampler(override.getType(), override.getTextureId(), null, override::getSampling, names);
		} else {
			return samplerHolder.addDefaultSampler(sampler, names);
		}
	}

	@Override
	public boolean addDefaultSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			return samplerHolder.addDefaultSampler(override.getType(), override.getTextureId(), null, override::getSampling, names);
		} else {
			return samplerHolder.addDefaultSampler(type, texture, notifier, sampler, names);
		}
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, GlSampler glSampler, String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			return samplerHolder.addDynamicSampler(override.getType(), override.getTextureId(), null, override::getSampling, names);
		} else {
			return samplerHolder.addDynamicSampler(sampler, glSampler, names);
		}
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, Supplier<GlSampler> sampler, String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			return samplerHolder.addDynamicSampler(override.getType(), override.getTextureId(), null, override::getSampling, names);
		} else {
			return samplerHolder.addDynamicSampler(type, texture, sampler, names);
		}
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, GlSampler glSampler, ValueUpdateNotifier notifier, String... names) {
		TextureAccess override = getOverride(names);

		if (override != null) {
			return samplerHolder.addDynamicSampler(override.getType(), override.getTextureId(), null, override::getSampling, names);
		} else {
			return samplerHolder.addDynamicSampler(sampler, glSampler, notifier, names);
		}
	}

	@Override
	public boolean addDynamicSampler(TextureType type, IntSupplier texture, ValueUpdateNotifier notifier, Supplier<GlSampler> sampler,
		String... names) {
		return false;
	}
}
