package net.coderbot.iris.shaderpack;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Parses comment-based directives found in shader source files of the form:
 *
 * <pre>/* KEY:VALUE *<i></i>/</pre>
 *
 * A common example is draw buffer directives:
 *
 * <pre>/* DRAWBUFFERS:157 *<i></i>/</pre>
 *
 * A given directive should only occur once in a shader file. If there are multiple occurrences of a directive with a
 * given key, the last occurrence is used.
 */
public class DirectiveParser {
	// Directives take the following form:
	// /* KEY:VALUE */

	private DirectiveParser() {
		// cannot be constructed
	}

	public static Optional<String> findDirectiveInLines(String[] lines, String key) {
		// We iterate over the list of lines in reverse order because the last occurrence of a directive has a greater
		// precedence over an earlier occurrence of a directive.
		for (int index = lines.length - 1; index >= 0; index -= 1) {
			String line = lines[index];

			Optional<String> found = findDirectiveInLine(line, key);

			if (found.isPresent()) {
				return found;
			}
		}

		// Didn't find any declarations of the directive.
		return Optional.empty();
	}

	private static Optional<String> findDirectiveInLine(String line, String key) {
		String prefix = "/* " + key + ":";
		String suffix = " */";

		// Search for the last occurrence of the directive within the line, since those take precedence.
		int indexOfPrefix = line.lastIndexOf(prefix);

		if (indexOfPrefix == -1) {
			return Optional.empty();
		}

		// Remove everything up to and including the prefix
		line = line.substring(indexOfPrefix + prefix.length());

		int indexOfSuffix = line.indexOf(suffix);

		// If there isn't a proper suffix, this directive is malformed and should be discarded.
		if (indexOfSuffix == -1) {
			return Optional.empty();
		}

		// Remove the suffix and everything afterwards
		line = line.substring(0, indexOfSuffix);

		return Optional.of(line);
	}

	// Test code for directive parsing. It's a bit homegrown but it works.
	@SuppressWarnings("unused")
	private static class Tests {
		private static <T> void test(String name, T expected, Supplier<T> testCase) {
			T actual;

			try {
				actual = testCase.get();
			} catch (Throwable e) {
				System.err.println("Test \"" + name + "\" failed with an exception:");
				e.printStackTrace();

				return;
			}

			if (!expected.equals(actual)) {
				System.err.println("Test \"" + name + "\" failed: Expected " + expected + ", got " + actual);
			} else {
				System.out.println("Test \"" + name + "\" passed");
			}
		}

		public static void main(String[] args) {
			test("normal text", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a DRAWBUFFERS directive of any sort";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("partial directive", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /* DRAWBUFFERS: directive of any sort";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("bad spacing", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /*DRAWBUFFERS:321*/ directive of any sort";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("matchAtEnd", Optional.of("321"), () -> {
				String line = "A line containg a drawbuffers directive: /* DRAWBUFFERS:321 */";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("matchAtStart", Optional.of("31"), () -> {
				String line = "/* DRAWBUFFERS:31 */ This is a line containg a drawbuffers directive";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("matchInMiddle", Optional.of("31"), () -> {
				String line = "This is a line /* DRAWBUFFERS:31 */ containg a drawbuffers directive";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("emptyMatch", Optional.of(""), () -> {
				String line = "/* DRAWBUFFERS: */ This is a line containg an invalid but still matching drawbuffers directive";

				return DirectiveParser.findDirectiveInLine(line, "DRAWBUFFERS");
			});

			test("duplicates", Optional.of("3"), () -> {
				String line = "/* TEST:2 */ This line contains multiple directives, the last one should be used /* TEST:3 */";

				return DirectiveParser.findDirectiveInLine(line, "TEST");
			});

			test("lines", Optional.of("It works"), () -> {
				String[] lines = new String[] {
					"/* Here's a random comment line */",
					"/* Test directive:Duplicate handling? */",
					"uniform sampler2D test;",
					"/* Test directive:Duplicate handling within a line? */ Let's see /* Test directive:It works */"
				};

				return DirectiveParser.findDirectiveInLines(lines, "Test directive");
			});
		}
	}
}
