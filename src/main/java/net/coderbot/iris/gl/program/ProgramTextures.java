package net.coderbot.iris.gl.program;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.sampler.SamplerBinding;
import net.coderbot.iris.gl.image.ImageBinding;
import net.coderbot.iris.gl.sampler.SamplerLimits;
import org.lwjgl.opengl.GL20C;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.IntSupplier;

/*
 * Manages all texture resources used by a single shader program.
 */
public class ProgramTextures {
	private final ImmutableList<SamplerBinding> samplerBindings;
	private final ImmutableList<ImageBinding> imageBindings;
	private List<GlUniform1iCall> initializer;

	private ProgramTextures(ImmutableList<SamplerBinding> samplerBindings, ImmutableList<ImageBinding> imageBindings, List<GlUniform1iCall> initializer) {
		this.samplerBindings = samplerBindings;
		this.imageBindings = imageBindings;
		this.initializer = initializer;
	}

	// Bind samplers and image units so a program can run.
	// Sets uniforms to correct sampler / image units.
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

		for (ImageBinding imageBinding : imageBindings) {
			imageBinding.update();
		}

		RenderSystem.activeTexture(GL20C.GL_TEXTURE0);
	}

	public static Builder builder(int program, Set<Integer> reservedTextureUnits) {
		return new Builder(program, reservedTextureUnits);
	}

	public static final class Builder {
		private final int program;
		private final ImmutableSet<Integer> reservedTextureUnits;
		private final ImmutableList.Builder<SamplerBinding> samplers;
		private final ImmutableList.Builder<ImageBinding> images;
		private final List<GlUniform1iCall> calls;
		private int remainingTextureUnits;
		private int nextTextureUnit;

		private Builder(int program, Set<Integer> reservedTextureUnits) {
			this.program = program;
			this.reservedTextureUnits = ImmutableSet.copyOf(reservedTextureUnits);
			this.samplers = ImmutableList.builder();
			this.images = ImmutableList.builder();
			this.calls = new ArrayList<>();

			int maxTextureUnits = SamplerLimits.get().getMaxTextureUnits();

			for (int unit : reservedTextureUnits) {
				if (unit >= maxTextureUnits) {
					throw new IllegalStateException("Cannot mark texture unit " + unit + " as reserved because that " +
							"texture unit isn't available on this system! Only " + maxTextureUnits +
							" texture units are available.");
				}
			}

			this.remainingTextureUnits = maxTextureUnits - reservedTextureUnits.size();
			this.nextTextureUnit = 0;

			while (reservedTextureUnits.contains(nextTextureUnit)) {
				nextTextureUnit += 1;
			}

			//System.out.println("Begin building samplers. Reserved texture units are " + reservedTextureUnits +
			//		", next texture unit is " + nextTextureUnit + ", there are " + remainingTextureUnits + " units remaining.");
		}

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

		public boolean hasSampler(String name) {
			return GL20C.glGetUniformLocation(program, name) != -1;
		}

		public boolean addDefaultSampler(IntSupplier textureID, String... names) {
			if (nextTextureUnit != 0) {
				// TODO: Relax this restriction!
				throw new IllegalStateException("Texture unit 0 is already used.");
			}

			return addDynamicSampler(textureID, true, names);
		}

		/**
		 * Adds a sampler
		 * @return false if this sampler is not active, true if at least one of the names referred to an active sampler
		 */
		public boolean addDynamicSampler(IntSupplier textureID, String... names) {
			return addDynamicSampler(textureID, false, names);
		}

		public void addTextureImage(IntSupplier textureID, int internalFormat, String name) {
			int location = GL20C.glGetUniformLocation(program, name);

			if (location == -1) {
				return;
			}

			if (name.startsWith("colorimg") && name.length() == 9) {
				int imageIndex = Character.getNumericValue(name.charAt(8));

				if (imageIndex >= 0 && imageIndex <= 5) {
					images.add(new ImageBinding(imageIndex, internalFormat, textureID));

					calls.add(new GlUniform1iCall(location, imageIndex));
				}
			}
		}

		private boolean addDynamicSampler(IntSupplier textureID, boolean used, String... names) {
			for (String name : names) {
				int location = GL20C.glGetUniformLocation(program, name);

				if (location == -1) {
					// There's no active sampler with this particular name in the program.
					continue;
				}

				// Make sure that we aren't out of texture units.
				if (remainingTextureUnits <= 0) {
					throw new IllegalStateException("No more available texture units while activating sampler " + name);
				}

				//System.out.println("Binding dynamic sampler " + name + " to texture unit " + nextTextureUnit);

				// Set up this sampler uniform to use this particular texture unit.
				calls.add(new GlUniform1iCall(location, nextTextureUnit));

				// And mark this texture unit as used.
				used = true;
			}

			if (!used) {
				return false;
			}

			samplers.add(new SamplerBinding(nextTextureUnit, textureID));

			remainingTextureUnits -= 1;
			nextTextureUnit += 1;

			while (remainingTextureUnits > 0 && reservedTextureUnits.contains(nextTextureUnit)) {
				nextTextureUnit += 1;
			}

			//System.out.println("The next unit is " + nextTextureUnit + ", there are " + remainingTextureUnits + " units remaining.");

			return true;
		}

		public ProgramTextures build() {
			return new ProgramTextures(samplers.build(), images.build(), calls);
		}
	}
}
