package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import org.anarres.cpp.*;

import java.util.ArrayList;
import java.util.List;

public class GlslPreprocessor {
	// Derived from GlShader from Canvas, licenced under LGPL
	public static String glslPreprocessSource(String source) {
		// NB: This doesn't work when ran in the above methods, only directly when creating the pass for some reason.
		// The C preprocessor won't understand the #version token, so we must remove it and readd it later.

		source = source.substring(source.indexOf("#version"));
		int versionStringEnd = source.indexOf('\n');

		String versionLine = source.substring(0, versionStringEnd);
		source = source.substring(versionStringEnd + 1);

		List<String> extensionLines = new ArrayList<>();

		while (source.contains("#extension")) {
			int extensionLineStart = source.indexOf("#extension");
			int extensionLineEnd = source.indexOf("\n", extensionLineStart);

			String extensionLine = source.substring(extensionLineStart, extensionLineEnd);
			source = source.replaceFirst(extensionLine, "");

			extensionLines.add(extensionLine);
		}

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

		// restore extensions
		for (String line : extensionLines) {
			builder.insert(0, line + "\n");
		}

		// restore GLSL version
		builder.insert(0, versionLine + "\n");

		source = builder.toString();

		return source;
	}
}
