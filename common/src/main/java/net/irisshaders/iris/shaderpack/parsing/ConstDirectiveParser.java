package net.irisshaders.iris.shaderpack.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstDirectiveParser {
	public static List<ConstDirective> findDirectives(String source) {
		List<ConstDirective> directives = new ArrayList<>();

		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		for (String line : source.split("\\R")) {
			findDirectiveInLine(line).ifPresent(directives::add);
		}

		return directives;
	}

	public static Optional<ConstDirective> findDirectiveInLine(String line) {
		// Valid const directives contain the following elements:
		// * Zero or more whitespace characters
		// * A "const" literal
		// * At least one whitespace character
		// * A type literal (int, float, vec4, or bool)
		// * At least one whitespace character
		// * The name / key of the const directive (alphanumeric & underscore characters)
		// * Zero or more whitespace characters
		// * An equals sign
		// * Zero or more whitespace characters
		// * The value of the const directive (alphanumeric & underscore characters)
		// * A semicolon
		// * (any content)

		// Bail-out early without doing any processing if required components are not found
		// A const directive must contain at the very least a const keyword, then an equals
		// sign, then a semicolon.
		if (!line.contains("const") || !line.contains("=") || !line.contains(";")) {
			return Optional.empty();
		}

		// Trim any surrounding whitespace (such as indentation) from the line before processing it.
		line = line.trim();

		// A valid declaration must have a trimmed line starting with const
		if (!line.startsWith("const")) {
			return Optional.empty();
		}

		// Remove the const part from the string
		line = line.substring("const".length());

		// There must be at least one whitespace character between the "const" keyword and the type keyword
		if (!startsWithWhitespace(line)) {
			return Optional.empty();
		}

		// Trim all whitespace between the const keyword and the type keyword
		line = line.trim();

		// Valid const declarations have a type that is either an int, a float, a vec4, or a bool.
		Type type;

		if (line.startsWith("int")) {
			type = Type.INT;
			line = line.substring("int".length());
		} else if (line.startsWith("float")) {
			type = Type.FLOAT;
			line = line.substring("float".length());
		} else if (line.startsWith("vec2")) {
			type = Type.VEC2;
			line = line.substring("vec2".length());
		} else if (line.startsWith("ivec3")) {
			type = Type.IVEC3;
			line = line.substring("ivec3".length());
		} else if (line.startsWith("vec4")) {
			type = Type.VEC4;
			line = line.substring("vec4".length());
		} else if (line.startsWith("bool")) {
			type = Type.BOOL;
			line = line.substring("bool".length());
		} else {
			return Optional.empty();
		}

		// There must be at least one whitespace character between the type keyword and the key of the const declaration
		if (!startsWithWhitespace(line)) {
			return Optional.empty();
		}

		// Split the declaration at the equals sign
		int equalsIndex = line.indexOf('=');

		if (equalsIndex == -1) {
			// No equals sign found, not a valid const declaration
			return Optional.empty();
		}

		// The key comes before the equals sign
		String key = line.substring(0, equalsIndex).trim();

		// The key must be a "word" (alphanumeric & underscore characters)
		if (!isWord(key)) {
			return Optional.empty();
		}

		// Everything after the equals sign but before the semicolon is the value
		String remaining = line.substring(equalsIndex + 1);

		int semicolonIndex = remaining.indexOf(';');

		if (semicolonIndex == -1) {
			// No semicolon found, not a valid const declaration
			return Optional.empty();
		}

		String value = remaining.substring(0, semicolonIndex).trim();

		// We make no attempt to properly parse / verify the value here, that responsibility lies with whatever code
		// is working with the directives.
		return Optional.of(new ConstDirective(type, key, value));
	}

	private static boolean startsWithWhitespace(String text) {
		return !text.isEmpty() && Character.isWhitespace(text.charAt(0));
	}

	private static boolean isWord(String text) {
		if (text.isEmpty()) {
			return false;
		}

		for (char character : text.toCharArray()) {
			if (!Character.isDigit(character) && !Character.isAlphabetic(character) && character != '_') {
				return false;
			}
		}

		return true;
	}

	public enum Type {
		INT,
		FLOAT,
		VEC2,
		IVEC3,
		VEC4,
		BOOL
	}

	public static class ConstDirective {
		private final Type type;
		private final String key;
		private final String value;

		ConstDirective(Type type, String key, String value) {
			this.type = type;
			this.key = key;
			this.value = value;
		}

		public Type getType() {
			return type;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}

		public String toString() {
			return "ConstDirective { " + type + " " + key + " = " + value + "; }";
		}
	}
}
