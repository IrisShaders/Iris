package net.irisshaders.iris.shaderpack.preprocessor;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.helpers.StringPair;
import net.irisshaders.iris.shaderpack.option.ShaderPackOptions;
import org.anarres.cpp.Feature;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorCommand;
import org.anarres.cpp.StringLexerSource;
import org.anarres.cpp.Token;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertiesPreprocessor {

	public static final Pattern BACKSLASH_MATCHER = Pattern.compile("(?<!\\\\\\n)^(?![ \\t]*(#|block\\.\\d*|layer\\.\\d*|item\\.\\d*|entity\\.\\d*|dimension\\.\\d*)).+", Pattern.MULTILINE);

	// Derived from ShaderProcessor.glslPreprocessSource, which is derived from GlShader from Canvas, licenced under LGPL
	public static String preprocessSource(String source, ShaderPackOptions shaderPackOptions, Iterable<StringPair> environmentDefines) {
		if (source.contains(PropertyCollectingListener.PROPERTY_MARKER) || source.contains("IRIS_PASSTHROUGHBACKSLASH")) {
			throw new RuntimeException("Some shader author is trying to exploit internal Iris implementation details, stop!");
		}

		List<String> booleanValues = getBooleanValues(shaderPackOptions);
		Map<String, String> stringValues = getStringValues(shaderPackOptions);

		try (Preprocessor pp = new Preprocessor()) {
			for (String value : booleanValues) {
				pp.addMacro(value);
			}

			for (StringPair envDefine : environmentDefines) {
				if (envDefine.value().isEmpty()) {
					pp.addMacro(envDefine.key());
				} else {
					pp.addMacro(envDefine.key(), envDefine.value());
				}
			}

			stringValues.forEach((name, value) -> {
				try {
					pp.addMacro(name, value);
				} catch (LexerException e) {
					Iris.logger.fatal("Failed to preprocess property file!", e);
				}
			});

			return process(pp, source);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected IOException while processing macros", e);
		} catch (LexerException e) {
			throw new RuntimeException("Unexpected LexerException processing macros", e);
		}
	}

	public static String preprocessSource(String source, Iterable<StringPair> environmentDefines) {
		if (source.contains(PropertyCollectingListener.PROPERTY_MARKER)) {
			throw new RuntimeException("Some shader author is trying to exploit internal Iris implementation details, stop!");
		}

		Preprocessor preprocessor = new Preprocessor();

		try {
			for (StringPair envDefine : environmentDefines) {
				preprocessor.addMacro(envDefine.key(), envDefine.value());
			}
		} catch (LexerException e) {
			Iris.logger.fatal("Failed to preprocess property file!", e);
		}

		return process(preprocessor, source);
	}

	private static String process(Preprocessor preprocessor, String source) {
		preprocessor.setListener(new PropertiesCommentListener());
		PropertyCollectingListener listener = new PropertyCollectingListener();
		preprocessor.setListener(listener);

		// Not super efficient, but this removes trailing whitespace on lines, fixing an issue with whitespace after
		// line continuations (see PreprocessorTest#testWeirdPropertiesLineContinuation)
		// Required for Voyager Shader
		source = Arrays.stream(source.split("\\R")).map(String::trim).filter(s -> !s.isBlank())
			.map(line -> {
				if (line.startsWith("#")) {
					for (PreprocessorCommand command : PreprocessorCommand.values()) {
						if (line.startsWith("#" + (command.name().replace("PP_", "").toLowerCase(Locale.ROOT)))) {
							return line;
						}
					}
					return "";
				}
				// In PropertyCollectingListener we suppress "unknown preprocessor directive errors" and
				// assume the line to be a comment, since in .properties files `#` also functions as a comment
				// marker.
				return line.replace("#", "");
			}).collect(Collectors.joining("\n")) + "\n";
		// TODO: This is a horrible fix to trick the preprocessor into not seeing the backslashes during processing. We need a better way to do this.
		source = source.replace("\\", "IRIS_PASSTHROUGHBACKSLASH");

		preprocessor.addInput(new StringLexerSource(source, true));
		preprocessor.addFeature(Feature.KEEPCOMMENTS);

		final StringBuilder builder = new StringBuilder();

		try {
			for (; ; ) {
				final Token tok = preprocessor.token();
				if (tok == null) break;
				if (tok.getType() == Token.EOF) break;
				builder.append(tok.getText());
			}
		} catch (final Exception e) {
			Iris.logger.error("Properties pre-processing failed", e);
		}

		source = builder.toString();

		return (listener.collectLines() + source).replace("IRIS_PASSTHROUGHBACKSLASH", "\\");
	}

	private static List<String> getBooleanValues(ShaderPackOptions shaderPackOptions) {
		List<String> booleanValues = new ArrayList<>();

		shaderPackOptions.getOptionSet().getBooleanOptions().forEach((string, value) -> {
			boolean trueValue = shaderPackOptions.getOptionValues().getBooleanValueOrDefault(string);

			if (trueValue) {
				booleanValues.add(string);
			}
		});

		return booleanValues;
	}

	private static Map<String, String> getStringValues(ShaderPackOptions shaderPackOptions) {
		Map<String, String> stringValues = new HashMap<>();

		shaderPackOptions.getOptionSet().getStringOptions().forEach(
			(optionName, value) -> stringValues.put(optionName, shaderPackOptions.getOptionValues().getStringValueOrDefault(optionName)));

		return stringValues;
	}
}
