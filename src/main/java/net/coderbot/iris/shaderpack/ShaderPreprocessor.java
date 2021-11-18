package net.coderbot.iris.shaderpack;

import java.util.ArrayList;
import java.util.List;

public class ShaderPreprocessor {
	public static String process(String source) {
		StringBuilder processed = new StringBuilder();

		List<String> lines = processInternal(source);

		for (String line : lines) {
			processed.append(line);
			processed.append('\n');
		}

		return processed.toString();
	}

	private static List<String> processInternal(String source) {
		List<String> lines = new ArrayList<>();

		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		for (String line : source.split("\\R")) {
			String trimmedLine = line.trim();

			if (trimmedLine.startsWith("#version")) {
				// macOS cannot handle whitespace before the #version directive.
				lines.add(trimmedLine);

				// That was the first line. Add our preprocessor lines
				lines.add("#define MC_RENDER_QUALITY 1.0");
				lines.add("#define MC_SHADOW_QUALITY 1.0");
				continue;
			}

			lines.add(line);
		}

		return lines;
	}
}
