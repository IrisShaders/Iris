package net.irisshaders.iris.shaderpack.preprocessor;

import net.irisshaders.iris.helpers.StringPair;
import org.anarres.cpp.Feature;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.StringLexerSource;
import org.anarres.cpp.Token;

public class JcppProcessor {
	// Derived from GlShader from Canvas, licenced under LGPL
	public static String glslPreprocessSource(String source, Iterable<StringPair> environmentDefines) {
		if (source.contains(GlslCollectingListener.VERSION_MARKER)
			|| source.contains(GlslCollectingListener.EXTENSION_MARKER)) {
			throw new RuntimeException("Some shader author is trying to exploit internal Iris implementation details, stop!");
		}

		// Note: This is an absolutely awful hack. But JCPP's lack of extensibility leaves me with no choice...
		//       We should write our own preprocessor at some point to avoid this.
		//
		// Why are we doing this awful hack instead of just using the preprocessor like a normal person? Because it lets
		// us only hoist #extension directives if they're actually used. This is needed for shader packs written on
		// lenient drivers that allow #extension directives to be placed anywhere to work on strict drivers like Mesa
		// that require #extension directives to occur at the top.
		//
		// TODO: This allows #version to not appear as the first non-comment non-whitespace thing in the file.
		//       That's not the behavior we want. If you're reading this, don't rely on this behavior.
		source = source.replace("#version", GlslCollectingListener.VERSION_MARKER);
		source = source.replace("#extension", GlslCollectingListener.EXTENSION_MARKER);

		// Remove null characters. Some packs, such as Chocapic High Performance, have random null characters that trip up JCPP.
		source = source.replace("\u0000", "");

		GlslCollectingListener listener = new GlslCollectingListener();

		@SuppressWarnings("resource") final Preprocessor pp = new Preprocessor();

		// Add the values of the environment defines without actually modifying the source code
		// of the shader program, one step down the road of having accurate line number reporting
		// in errors...
		try {
			for (StringPair envDefine : environmentDefines) {
				pp.addMacro(envDefine.key(), envDefine.value());
			}
		} catch (LexerException e) {
			throw new RuntimeException("Unexpected LexerException processing macros", e);
		}

		pp.setListener(listener);
		pp.addInput(new StringLexerSource(source, true));
		pp.addFeature(Feature.KEEPCOMMENTS);

		final StringBuilder builder = new StringBuilder();

		try {
			for (; ; ) {
				final Token tok = pp.token();
				if (tok == null) break;
				if (tok.getType() == Token.EOF) break;
				builder.append(tok.getText());
			}
		} catch (final Exception e) {
			throw new RuntimeException("GLSL source pre-processing failed", e);
		}

		builder.append("\n");

		source = listener.collectLines() + builder;

		return source;
	}
}
