package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import org.anarres.cpp.DefaultPreprocessorListener;
import org.anarres.cpp.Feature;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;
import org.anarres.cpp.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ShaderPreprocessor {
	public static String process(Path rootPath, Path shaderPath, String source) throws IOException {
		StringBuilder processed = new StringBuilder();

		List<String> lines = processInternal(rootPath, shaderPath, source);

		for (String line : lines) {
			processed.append(line);
			processed.append('\n');
		}

		return glslPreprocessSource(processed.toString());
	}

	private static List<String> processInternal(Path rootPath, Path shaderPath, String source) throws IOException {
		List<String> lines = new ArrayList<>();

		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		for (String line : source.split("\\R")) {
			String trimmedLine = line.trim();

			if (trimmedLine.startsWith("#include ")) {
				try {
					lines.addAll(include(rootPath, shaderPath, trimmedLine));
				} catch (IOException e) {
					throw new IOException("Failed to read file from #include directive", e);
				}

				continue;
			}

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

	private static List<String> include(Path rootPath, Path shaderPath, String directive) throws IOException {
		// Remove the "#include " part so that we just have the file path
		String target = directive.substring("#include ".length()).trim();

		// Remove quotes if they're present
		// All include directives should have quotes, but I
		if (target.startsWith("\"")) {
			target = target.substring(1);
		}

		if (target.endsWith("\"")) {
			target = target.substring(0, target.length() - 1);
		}

		Path included;

		if (target.startsWith("/")) {
			target = target.substring(1);

			included = rootPath.resolve(target);
		} else {
			included = shaderPath.getParent().resolve(target);
		}

		String source = readFile(included);

		return processInternal(rootPath, included, source);
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}


	// Derived from GlShader from Canvas, licenced under LGPL
	public static String glslPreprocessSource(String source) {
		// NB: This doesn't work when ran in the above methods, only directly when creating the pass for some reason.
		// The C preprocessor won't understand the #version token, so we must remove it and readd it later.

		source = source.substring(source.indexOf("#version"));
		int versionStringEnd = source.indexOf('\n');

		String versionLine = source.substring(0, versionStringEnd);
		source = source.substring(versionStringEnd + 1);

		@SuppressWarnings("resource")
		final Preprocessor pp = new Preprocessor();
		pp.setListener(new DefaultPreprocessorListener());
		pp.addInput(new StringLexerSource(source, true));
		pp.addFeature(Feature.KEEPCOMMENTS);

		final StringBuilder builder = new StringBuilder();

		try {
			for (;;) {
				final Token tok = pp.token();
				if (tok == null) break;
				if (tok.getType() == Token.EOF) break;
				builder.append(tok.getText());
			}
		} catch (final Exception e) {
			Iris.logger.error("GLSL source pre-processing failed", e);
		}

		builder.append("\n");

		source = builder.toString();

		// restore GLSL version
		source = versionLine + "\n" + source;

		// strip leading whitepsace before newline, makes next change more reliable
		source = source.replaceAll("[ \t]*[\r\n]", "\n");
		// consolidate newlines
		source = source.replaceAll("\n{2,}", "\n\n");
		// inefficient way to remove multiple orhpaned comment blocks
		source = source.replaceAll("\\/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*\\/[\\s]+\\/\\*", "/*");
		source = source.replaceAll("\\/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*\\/[\\s]+\\/\\*", "/*");
		source = source.replaceAll("\\/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*\\/[\\s]+\\/\\*", "/*");

		return source;
	}
}
