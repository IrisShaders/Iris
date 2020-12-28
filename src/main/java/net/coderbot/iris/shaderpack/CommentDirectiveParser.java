package net.coderbot.iris.shaderpack;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Parses comment-based directives found in shader source files of the form:
 *
 * <pre>/* KEY:VALUE *<i></i>/</pre>
 * <p>
 * A common example is draw buffer directives:
 *
 * <pre>/* DRAWBUFFERS:157 *<i></i>/</pre>
 * <p>
 * A given directive should only occur once in a shader file. If there are multiple occurrences of a directive with a
 * given key, the last occurrence is used.
 */
public class CommentDirectiveParser {
	// Directives take the following form:
	// /* KEY:VALUE */

	private CommentDirectiveParser() {
		// cannot be constructed
	}

	public static Optional<String> findDirectiveInLines(List<String> lines, String key) {
		// We iterate over the list of lines in reverse order because the last occurrence of a directive has a greater
		// precedence over an earlier occurrence of a directive.
		for (int index = lines.size() - 1; index >= 0; index -= 1) {
			String line = lines.get(index);

			Optional<String> found = findDirective(line, key);

			if (found.isPresent()) {
				return found;
			}
		}

		// Didn't find any declarations of the directive.
		return Optional.empty();
	}

	public static Optional<String> findDirective(String haystack, String needle) {
		String prefix = "/* " + needle + ":";
		String suffix = " */";

		// Search for the last occurrence of the directive within the text, since those take precedence.
		int indexOfPrefix = haystack.lastIndexOf(prefix);

		if (indexOfPrefix == -1) {
			return Optional.empty();
		}

		// Remove everything up to and including the prefix
		haystack = haystack.substring(indexOfPrefix + prefix.length());

		int indexOfSuffix = haystack.indexOf(suffix);

		// If there isn't a proper suffix, this directive is malformed and should be discarded.
		if (indexOfSuffix == -1) {
			return Optional.empty();
		}

		// Remove the suffix and everything afterwards
		haystack = haystack.substring(0, indexOfSuffix);

		return Optional.of(haystack);
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

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("partial directive", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /* DRAWBUFFERS: directive of any sort";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("bad spacing", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /*DRAWBUFFERS:321*/ directive of any sort";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("matchAtEnd", Optional.of("321"), () -> {
				String line = "A line containg a drawbuffers directive: /* DRAWBUFFERS:321 */";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("matchAtStart", Optional.of("31"), () -> {
				String line = "/* DRAWBUFFERS:31 */ This is a line containg a drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("matchInMiddle", Optional.of("31"), () -> {
				String line = "This is a line /* DRAWBUFFERS:31 */ containg a drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("emptyMatch", Optional.of(""), () -> {
				String line = "/* DRAWBUFFERS: */ This is a line containg an invalid but still matching drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, "DRAWBUFFERS");
			});

			test("duplicates", Optional.of("3"), () -> {
				String line = "/* TEST:2 */ This line contains multiple directives, the last one should be used /* TEST:3 */";

				return CommentDirectiveParser.findDirective(line, "TEST");
			});

			test("lines", Optional.of("It works"), () -> {
				String[] linesArray = new String[]{
					"/* Here's a random comment line */",
					"/* Test directive:Duplicate handling? */",
					"uniform sampler2D test;",
					"/* Test directive:Duplicate handling within a line? */ Let's see /* Test directive:It works */"
				};

				List<String> lines = Arrays.asList(linesArray);

				return CommentDirectiveParser.findDirectiveInLines(lines, "Test directive");
			});
		}
	}
}
