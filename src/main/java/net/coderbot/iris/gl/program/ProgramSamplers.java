package net.coderbot.iris.gl.program;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.sampler.SamplerBinding;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import net.coderbot.iris.gl.state.ValueUpdateNotifier;
import net.coderbot.iris.gl.texture.TextureAccess;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.mixin.GlStateManagerAccessor;
import net.coderbot.iris.shaderpack.PackRenderTargetDirectives;
import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public class ProgramSamplers {
	private static ProgramSamplers active;
	private final ImmutableList<SamplerBinding> samplerBindings;
	private final ImmutableList<ValueUpdateNotifier> notifiersToReset;
	private List<GlUniform1iCall> initializer;

	private ProgramSamplers(ImmutableList<SamplerBinding> samplerBindings, ImmutableList<ValueUpdateNotifier> notifiersToReset, List<GlUniform1iCall> initializer) {
		this.samplerBindings = samplerBindings;
		this.notifiersToReset = notifiersToReset;
		this.initializer = initializer;
	}

	public void update() {
		if (active != null) {
			active.removeListeners();
		}

		active = this;

		if (initializer != null) {
			for (GlUniform1iCall call : initializer) {
				RenderSystem.glUniform1i(call.getLocation(), call.getValue());
			}

			initializer = null;
		}

		// We need to keep the active texture intact, since if we mess it up
		// in the middle of RenderType setup, bad things will happen.
		int activeTexture = GlStateManagerAccessor.getActiveTexture();

		for (SamplerBinding samplerBinding : samplerBindings) {
			samplerBinding.update();
		}

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0 + activeTexture);
	}

	public void removeListeners() {
		active = null;

		for (ValueUpdateNotifier notifier : notifiersToReset) {
			notifier.setListener(null);
		}
	}

	public static void clearActiveSamplers() {
		if (active != null) {
			active.removeListeners();
		}
	}

	public static Builder builder(int program, Set<Integer> reservedTextureUnits) {
		return new Builder(program, reservedTextureUnits);
	}

	public static CustomTextureSamplerInterceptor customTextureSamplerInterceptor(SamplerHolder samplerHolder, Object2ObjectMap<String, TextureAccess> customTextureIds) {
		return customTextureSamplerInterceptor(samplerHolder, customTextureIds, ImmutableSet.of());
	}

	public static CustomTextureSamplerInterceptor customTextureSamplerInterceptor(SamplerHolder samplerHolder, Object2ObjectMap<String, TextureAccess> customTextureIds, ImmutableSet<Integer> flippedAtLeastOnceSnapshot) {
		return new CustomTextureSamplerInterceptor(samplerHolder, customTextureIds, flippedAtLeastOnceSnapshot);
	}

	public static final class Builder implements SamplerHolder {
		private final int program;
		private final ImmutableSet<Integer> reservedTextureUnits;
		private final ImmutableList.Builder<SamplerBinding> samplers;
		private final ImmutableList.Builder<ValueUpdateNotifier> notifiersToReset;
		private final List<GlUniform1iCall> calls;
		private int remainingUnits;
		private int nextUnit;

		private Builder(int program, Set<Integer> reservedTextureUnits) {
			this.program = program;
			this.reservedTextureUnits = ImmutableSet.copyOf(reservedTextureUnits);
			this.samplers = ImmutableList.builder();
			this.notifiersToReset = ImmutableList.builder();
			this.calls = new ArrayList<>();

			int maxTextureUnits = SamplerLimits.get().getMaxTextureUnits();

			for (int unit : reservedTextureUnits) {
				if (unit >= maxTextureUnits) {
					throw new IllegalStateException("Cannot mark texture unit " + unit + " as reserved because that " +
							"texture unit isn't available on this system! Only " + maxTextureUnits +
							" texture units are available.");
				}
			}

			this.remainingUnits = maxTextureUnits - reservedTextureUnits.size();

			while (reservedTextureUnits.contains(nextUnit)) {
				this.nextUnit++;
			}

			//System.out.println("Begin building samplers. Reserved texture units are " + reservedTextureUnits +
			//		", next texture unit is " + nextUnit + ", there are " + remainingUnits + " units remaining.");
		}

		@Override
		public void addExternalSampler(int textureUnit, String... names) {
			if (!reservedTextureUnits.contains(textureUnit)) {
				throw new IllegalArgumentException("Cannot add an externally-managed sampler for texture unit " +
						textureUnit + " since it isn't in the set of reserved texture units.");
			}

			for (String name : names) {
				int location = GlStateManager._glGetUniformLocation(program, name);

				if (location == -1) {
					// There's no active sampler with this particular name in the program.
					continue;
				}

				// Set up this sampler uniform to use this particular texture unit.
				//System.out.println("Binding external sampler " + name + " to texture unit " + textureUnit);
				calls.add(new GlUniform1iCall(location, textureUnit));
			}
		}

		@Override
		public boolean hasSampler(String name) {
			return GlStateManager._glGetUniformLocation(program, name) != -1;
		}

		@Override
		public boolean addDefaultSampler(IntSupplier sampler, String... names) {
			if (nextUnit != 0) {
				// TODO: Relax this restriction!
				throw new IllegalStateException("Texture unit 0 is already used.");
			}

			return addDynamicSampler(TextureType.TEXTURE_2D, sampler, true, null, names);
		}

		/**
		 * Adds a sampler
		 * @return false if this sampler is not active, true if at least one of the names referred to an active sampler
		 */
		@Override
		public boolean addDynamicSampler(TextureType type, IntSupplier sampler, String... names) {
			return addDynamicSampler(type, sampler, false, null, names);
		}

		@Override
		public boolean addDynamicSampler(TextureType type, IntSupplier sampler, ValueUpdateNotifier notifier, String... names) {
			return addDynamicSampler(type, sampler, false, notifier, names);
		}

		/**
		 * Adds a sampler
		 * @return false if this sampler is not active, true if at least one of the names referred to an active sampler
		 */
		private boolean addDynamicSampler(TextureType type, IntSupplier sampler, boolean used, ValueUpdateNotifier notifier, String... names) {
			if (notifier != null) {
				notifiersToReset.add(notifier);
			}

			for (String name : names) {
				int location = GlStateManager._glGetUniformLocation(program, name);

				if (location == -1) {
					// There's no active sampler with this particular name in the program.
					continue;
				}

				// Make sure that we aren't out of texture units.
				if (remainingUnits <= 0) {
					throw new IllegalStateException("No more available texture units while activating sampler " + name);
				}

				//System.out.println("Binding dynamic sampler " + name + " with type " + type.name() + " to texture unit " + nextUnit);

				// Set up this sampler uniform to use this particular texture unit.
				calls.add(new GlUniform1iCall(location, nextUnit));

				// And mark this texture unit as used.
				used = true;
			}

			if (!used) {
				return false;
			}

			samplers.add(new SamplerBinding(type, nextUnit, sampler, notifier));

			remainingUnits--;
			nextUnit++;

			while (remainingUnits > 0 && reservedTextureUnits.contains(nextUnit)) {
				nextUnit += 1;
			}

			//System.out.println("The next unit is " + nextUnit + ", there are " + remainingUnits + " units remaining.");

			return true;
		}

		public ProgramSamplers build() {
			return new ProgramSamplers(samplers.build(), notifiersToReset.build(), calls);
		}
	}

	public static final class CustomTextureSamplerInterceptor implements SamplerHolder {
		private final SamplerHolder samplerHolder;
		private final Object2ObjectMap<String, TextureAccess> customTextureIds;
		private final ImmutableSet<String> deactivatedOverrides;

		private CustomTextureSamplerInterceptor(SamplerHolder samplerHolder, Object2ObjectMap<String, TextureAccess> customTextureIds, ImmutableSet<Integer> flippedAtLeastOnceSnapshot) {
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

		private IntSupplier getOverride(IntSupplier existing, String... names) {
			for (String name : names) {
				if (customTextureIds.containsKey(name) && !deactivatedOverrides.contains(name)) {
					return customTextureIds.get(name).getTextureId();
				}
			}

			return existing;
		}

		@Override
		public void addExternalSampler(int textureUnit, String... names) {
			IntSupplier override = getOverride(null, names);

			if (override != null) {
				if (textureUnit == 0) {
					samplerHolder.addDefaultSampler(override, names);
				} else {
					samplerHolder.addDynamicSampler(override, names);
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
			sampler = getOverride(sampler, names);

			return samplerHolder.addDefaultSampler(sampler, names);
		}

		@Override
		public boolean addDynamicSampler(IntSupplier sampler, String... names) {
			sampler = getOverride(sampler, names);

			return samplerHolder.addDynamicSampler(sampler, names);
		}

		@Override
		public boolean addDynamicSampler(TextureType type, IntSupplier sampler, String... names) {
			sampler = getOverride(sampler, names);

			return samplerHolder.addDynamicSampler(type, sampler, names);
		}

		@Override
		public boolean addDynamicSampler(IntSupplier sampler, ValueUpdateNotifier notifier, String... names) {
			sampler = getOverride(sampler, names);

			return samplerHolder.addDynamicSampler(sampler, notifier, names);
		}

		@Override
		public boolean addDynamicSampler(TextureType type, IntSupplier sampler, ValueUpdateNotifier notifier, String... names) {
			sampler = getOverride(sampler, names);

			return samplerHolder.addDynamicSampler(type, sampler, notifier, names);
		}
	}
}
