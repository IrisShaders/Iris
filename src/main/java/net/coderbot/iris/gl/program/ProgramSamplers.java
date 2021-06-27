package net.coderbot.iris.gl.program;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.sampler.SamplerBinding;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

public class ProgramSamplers {
	private final ImmutableList<SamplerBinding> samplerBindings;
	private List<GlUniform1iCall> initializer;

	private ProgramSamplers(ImmutableList<SamplerBinding> samplerBindings, List<GlUniform1iCall> initializer) {
		this.samplerBindings = samplerBindings;
		this.initializer = initializer;
	}

	public void update() {
		if (initializer != null) {
			for (GlUniform1iCall call : initializer) {
				GL20C.glUniform1i(call.getLocation(), call.getValue());
			}

			initializer = null;
		}

		for (SamplerBinding samplerBinding : samplerBindings) {
			samplerBinding.update();
		}

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
	}

	public static Builder builder(int program, Set<Integer> reservedTextureUnits) {
		return new Builder(program, reservedTextureUnits);
	}

	public static final class Builder implements SamplerHolder {
		private final int program;
		private final ImmutableSet<Integer> reservedTextureUnits;
		private final ImmutableList.Builder<SamplerBinding> samplers;
		private final List<GlUniform1iCall> calls;
		private int remainingUnits;
		private int nextUnit;

		private Builder(int program, Set<Integer> reservedTextureUnits) {
			this.program = program;
			this.reservedTextureUnits = ImmutableSet.copyOf(reservedTextureUnits);
			this.samplers = ImmutableList.builder();
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
			this.nextUnit = 0;

			while (reservedTextureUnits.contains(nextUnit)) {
				nextUnit += 1;
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
				int location = GL20C.glGetUniformLocation(program, name);

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
			return GL20C.glGetUniformLocation(program, name) != -1;
		}

		@Override
		public boolean addDefaultSampler(IntSupplier sampler, Runnable postBind, String... names) {
			if (nextUnit != 0) {
				// TODO: Relax this restriction!
				throw new IllegalStateException("Texture unit 0 is already used.");
			}

			return addDynamicSampler(sampler, postBind, true, names);
		}

		/**
		 * Adds a sampler
		 * @return false if this sampler is not active, true if at least one of the names referred to an active sampler
		 */
		@Override
		public boolean addDynamicSampler(IntSupplier sampler, Runnable postBind, String... names) {
			return addDynamicSampler(sampler, postBind, false, names);
		}

		private boolean addDynamicSampler(IntSupplier sampler, Runnable postBind, boolean used, String... names) {
			for (String name : names) {
				int location = GL20C.glGetUniformLocation(program, name);

				if (location == -1) {
					// There's no active sampler with this particular name in the program.
					continue;
				}

				// Make sure that we aren't out of texture units.
				if (remainingUnits <= 0) {
					throw new IllegalStateException("No more available texture units while activating sampler " + name);
				}

				//System.out.println("Binding dynamic sampler " + name + " to texture unit " + nextUnit);

				// Set up this sampler uniform to use this particular texture unit.
				calls.add(new GlUniform1iCall(location, nextUnit));

				// And mark this texture unit as used.
				used = true;
			}

			if (!used) {
				return false;
			}

			samplers.add(new SamplerBinding(nextUnit, sampler, postBind));

			remainingUnits -= 1;
			nextUnit += 1;

			while (remainingUnits > 0 && reservedTextureUnits.contains(nextUnit)) {
				nextUnit += 1;
			}

			//System.out.println("The next unit is " + nextUnit + ", there are " + remainingUnits + " units remaining.");

			return true;
		}

		public ProgramSamplers build() {
			return new ProgramSamplers(samplers.build(), calls);
		}
	}
}
