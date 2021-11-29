package net.coderbot.iris.shaderpack.transform.line;

/**
 * Removes leading whitespace on the line containing the #version directive, to
 * work around an issue with Apple's drivers on macOS. Credit to julian for the fix.
 */
public class VersionDirectiveNormalizer implements LineTransform {
	public static final VersionDirectiveNormalizer INSTANCE = new VersionDirectiveNormalizer();

	private VersionDirectiveNormalizer() {
		// no-op
	}

	@Override
	public String transform(int index, String line) {
		String trimmed = line.trim();

		if (trimmed.startsWith("#version")) {
			return trimmed;
		}

		return line;
	}
}
