package net.irisshaders.iris.shaderpack.preprocessor;

import org.anarres.cpp.DefaultPreprocessorListener;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Source;

public class PropertyCollectingListener extends DefaultPreprocessorListener {
	public static final String PROPERTY_MARKER = "#warning IRIS_PASSTHROUGH ";

	private final StringBuilder builder;

	public PropertyCollectingListener() {
		this.builder = new StringBuilder();
	}

	@Override
	public void handleWarning(Source source, int line, int column, String msg) throws LexerException {
		if (msg.startsWith(PROPERTY_MARKER)) {
			builder.append(msg.replace(PROPERTY_MARKER, ""));
			builder.append('\n');
		} else {
			super.handleWarning(source, line, column, msg);
		}
	}

	@Override
	public void handleError(Source source, int line, int column, String msg) throws LexerException {
		if (msg.contains("Unknown preprocessor directive")
			|| msg.contains("Preprocessor directive not a word")) {
			// Suppress log spam since hashed lines also function as comments in preprocessed files.
			return;
		}

		super.handleError(source, line, column, msg);
	}

	public String collectLines() {
		return builder.toString();
	}
}
