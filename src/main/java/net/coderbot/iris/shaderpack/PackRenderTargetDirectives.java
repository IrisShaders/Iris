package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.rendertarget.RenderTargets;

import java.util.Arrays;
import java.util.Optional;

public class PackRenderTargetDirectives {
	private static final int MAX_RENDER_TARGETS = RenderTargets.MAX_RENDER_TARGETS;
	private static final String[] LEGACY_RENDER_TARGETS = new String[] {
		"gcolor",
		"gdepth",
		"gnormal",
		"gcomposite",
		"gaux1",
		"gaux2",
		"gaux3",
		"gaux4"
	};

	private final InternalTextureFormat[] requestedTextureFormats;
	private final boolean[] clearBuffers;
	private final boolean[] mipmapBuffers;

	PackRenderTargetDirectives() {
		requestedTextureFormats = new InternalTextureFormat[MAX_RENDER_TARGETS];
		Arrays.fill(requestedTextureFormats, InternalTextureFormat.RGBA);

		clearBuffers = new boolean[RenderTargets.MAX_RENDER_TARGETS];
		Arrays.fill(clearBuffers, true);

		mipmapBuffers = new boolean[RenderTargets.MAX_RENDER_TARGETS];
	}

	public IntList getBuffersToBeCleared() {
		IntList buffersToBeCleared = new IntArrayList();

		for (int i = 0; i < clearBuffers.length; i++) {
			if (clearBuffers[i]) {
				buffersToBeCleared.add(i);
			}
		}

		return buffersToBeCleared;
	}

	public InternalTextureFormat[] getRequestedBufferFormats() {
		// TODO: If gdepth is directly referenced and no format override is provided, use RGBA32F

		return requestedTextureFormats;
	}

	// TODO: getter for mipmap directives, and actually handle them!

	public void acceptDirectives(DirectiveHolder directives) {
		if (MAX_RENDER_TARGETS > 7) {
			// Handle legacy GAUX4FORMAT directives

			directives.acceptCommentStringDirective("GAUX4FORMAT", format -> {
				if ("RGBA32F".equals(format)) {
					requestedTextureFormats[7] = InternalTextureFormat.RGBA32F;
				} else if ("RGB32F".equals(format)) {
					requestedTextureFormats[7] = InternalTextureFormat.RGB32F;
				} else if ("RGB16".equals(format)) {
					requestedTextureFormats[7] = InternalTextureFormat.RGB16;
				} else {
					Iris.logger.warn("Ignoring GAUX4FORMAT directive /* GAUX4FORMAT:" + format + "*/ because " + format
							+ " must be RGBA32F, RGB32F, or RGB16. Use `const int colortex7Format = " + format + ";` + instead.");
				}
			});
		}

		for (int i = 0; i < MAX_RENDER_TARGETS; i++) {
			acceptBufferDirectives(directives, i, "colortex" + i);

			if (i < LEGACY_RENDER_TARGETS.length) {
				acceptBufferDirectives(directives, i, LEGACY_RENDER_TARGETS[i]);
			}
		}
	}

	private void acceptBufferDirectives(DirectiveHolder directives, int bufferIndex, String bufferName) {
		directives.acceptConstStringDirective(bufferName + "Format", format -> {
			Optional<InternalTextureFormat> internalFormat = InternalTextureFormat.fromString(format);

			if (internalFormat.isPresent()) {
				requestedTextureFormats[bufferIndex] = internalFormat.get();
			} else {
				Iris.logger.warn("Unrecognized internal texture format " + format + " specified for " + bufferName + "Format, ignoring.");
			}
		});

		// TODO: Only for composite and deferred
		directives.acceptConstBooleanDirective(bufferName + "Clear",
				shouldClear -> clearBuffers[bufferIndex] = shouldClear);

		// TODO: Only for composite, deferred, and final
		// TODO: vec4 *ClearColor

		// TODO: Only for composite, deferred, and final
		directives.acceptConstBooleanDirective(bufferName + "MipmapEnabled",
				shouldMipmap -> mipmapBuffers[bufferIndex] = shouldMipmap);
	}
}
