package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;

import java.util.Arrays;
import java.util.OptionalInt;

public class PackDirectives {
	private InternalTextureFormat[] requestedTextureFormats;
	private IntList buffersToBeCleared;

	PackDirectives() {
		requestedTextureFormats = new InternalTextureFormat[8];
		Arrays.fill(requestedTextureFormats, InternalTextureFormat.RGBA);

		// TODO: Don't assume that there are only 8 buffers
		buffersToBeCleared = new IntArrayList(new int[]{0, 1, 2, 3, 4, 5, 6, 7});
	}

	// TODO: These are currently hardcoded to work with Sildur's. They will need to be properly parsed from shaders.properties.
	// Some of these values also come from individual shader files, such as the requested buffer formats.

	public IntList getBuffersToBeCleared() {
		return buffersToBeCleared;
	}

	public InternalTextureFormat[] getRequestedBufferFormats() {
		// TODO: If gdepth is directly referenced and no format override is provided, use RGBA32F

		return requestedTextureFormats;
	}

	void accept(ConstDirectiveParser.ConstDirective directive) {
		final ConstDirectiveParser.Type type = directive.getType();
		final String key = directive.getKey();
		final String value = directive.getValue();

		Iris.logger.info("Found potential directive: " + type + " " + key + " = " + value);

		if (type == ConstDirectiveParser.Type.INT && key.endsWith("Format")) {
			String bufferName = key.substring(0, key.length() - "Format".length());

			bufferNameToIndex(bufferName).ifPresent(index -> {
				requestedTextureFormats[index] = InternalTextureFormat.valueOf(value);
			});
		} else if (type == ConstDirectiveParser.Type.BOOL && key.endsWith("Clear") && value.equals("false")) {
			String bufferName = key.substring(0, key.length() - "Clear".length());

			bufferNameToIndex(bufferName).ifPresent(buffersToBeCleared::removeInt);
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
