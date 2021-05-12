package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.rendertarget.RenderTargets;

import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalInt;

public class PackDirectives {
	private final InternalTextureFormat[] requestedTextureFormats;
	private final boolean[] clearBuffers;
	private int noiseTextureResolution;
	private float sunPathRotation;

	PackDirectives() {
		requestedTextureFormats = new InternalTextureFormat[RenderTargets.MAX_RENDER_TARGETS];
		Arrays.fill(requestedTextureFormats, InternalTextureFormat.RGBA);

		clearBuffers = new boolean[RenderTargets.MAX_RENDER_TARGETS];
		Arrays.fill(clearBuffers, true);

		noiseTextureResolution = 256;
		sunPathRotation = 0.0F;
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

	public int getNoiseTextureResolution() {
		return noiseTextureResolution;
	}

	public float getSunPathRotation() {
		return sunPathRotation;
	}

	void accept(ConstDirectiveParser.ConstDirective directive) {
		final ConstDirectiveParser.Type type = directive.getType();
		final String key = directive.getKey();
		final String value = directive.getValue();

		Iris.logger.info("Found potential directive: " + type + " " + key + " = " + value);

		if (type == ConstDirectiveParser.Type.INT && key.endsWith("Format")) {
			String bufferName = key.substring(0, key.length() - "Format".length());

			bufferNameToIndex(bufferName).ifPresent(index -> {
				Optional<InternalTextureFormat> internalFormat = InternalTextureFormat.fromString(value);

				if (internalFormat.isPresent()) {
					requestedTextureFormats[index] = internalFormat.get();
				} else {
					Iris.logger.warn("Unrecognized internal texture format " + value + " specified for " + key + ", ignoring.");
				}
			});
		} else if (type == ConstDirectiveParser.Type.BOOL && key.endsWith("Clear") && value.equals("false")) {
			String bufferName = key.substring(0, key.length() - "Clear".length());

			bufferNameToIndex(bufferName).ifPresent(index -> {
				clearBuffers[index] = false;
			});
		} else if (type == ConstDirectiveParser.Type.INT && key.equals("noiseTextureResolution")) {
			noiseTextureResolution = Integer.parseInt(value);
		} else if (type == ConstDirectiveParser.Type.FLOAT && key.equals("sunPathRotation")) {
			sunPathRotation = Float.parseFloat(value);
		}
	}

	private OptionalInt bufferNameToIndex(String bufferName) {
		if (bufferName.startsWith("colortex")) {
			String index = bufferName.substring("colortex".length());

			try {
				return OptionalInt.of(Integer.parseInt(index));
			} catch (NumberFormatException e) {
				return OptionalInt.empty();
			}
		}

		switch (bufferName) {
			case "gcolor":
				return OptionalInt.of(0);
			case "gdepth":
				return OptionalInt.of(1);
			case "gnormal":
				return OptionalInt.of(2);
			case "composite":
				return OptionalInt.of(3);
			case "gaux1":
				return OptionalInt.of(4);
			case "gaux2":
				return OptionalInt.of(5);
			case "gaux3":
				return OptionalInt.of(6);
			case "gaux4":
				return OptionalInt.of(7);
		}

		return OptionalInt.empty();
	}
}
