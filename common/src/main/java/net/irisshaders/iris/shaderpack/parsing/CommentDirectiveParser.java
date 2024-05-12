package net.irisshaders.iris.shaderpack.parsing;

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

	public static Optional<CommentDirective> findDirective(String haystack, CommentDirective.Type type) {
		String needle = type.name();
		String prefix = needle + ":";
		String suffix = "*/";

		// Search for the last occurrence of the directive within the text, since those take precedence.
		int indexOfPrefix = haystack.lastIndexOf(prefix);

		if (indexOfPrefix == -1) {
			return Optional.empty();
		}

		String before = haystack.substring(0, indexOfPrefix).trim();

		if (!before.endsWith("/*")) {
			// Reject a match if it doesn't actually start with a comment marker
			// TODO: If a line has two directives, one valid, and the other invalid, then this might not work properly
			return Optional.empty();
		}

		// Remove everything up to and including the prefix
		haystack = haystack.substring(indexOfPrefix + prefix.length());

		int indexOfSuffix = haystack.indexOf(suffix);

		// If there isn't a proper suffix, this directive is malformed and should be discarded.
		if (indexOfSuffix == -1) {
			return Optional.empty();
		}

		// Remove the suffix and everything afterwards, also trim any whitespace
		haystack = haystack.substring(0, indexOfSuffix).trim();

		return Optional.of(new CommentDirective(CommentDirective.Type.valueOf(needle), haystack, indexOfPrefix));
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

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("partial directive", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /* DRAWBUFFERS: directive of any sort";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("bad spacing", Optional.of("321"), () -> {
				String line = "/*DRAWBUFFERS:321*/ OptiFine will detect this directive, but ShadersMod will not...";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("matchAtEnd", Optional.of("321"), () -> {
				String line = "A line containing a drawbuffers directive: /* DRAWBUFFERS:321 */";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("matchAtStart", Optional.of("31"), () -> {
				String line = "/* DRAWBUFFERS:31 */ This is a line containing a drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("matchInMiddle", Optional.of("31"), () -> {
				String line = "This is a line /* DRAWBUFFERS:31 */ containing a drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("emptyMatch", Optional.of(""), () -> {
				String line = "/* DRAWBUFFERS: */ This is a line containing an invalid but still matching drawbuffers directive";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("duplicates", Optional.of("3"), () -> {
				String line = "/* TEST:2 */ This line contains multiple directives, the last one should be used /* TEST:3 */";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});

			test("multi-line", Optional.of("It works"), () -> {
				String lines =
					"""
						/* Here's a random comment line */
						/* RENDERTARGETS:Duplicate handling? */
						""" +
						"uniform sampler2D test;\n" +
						"/* RENDERTARGETS:Duplicate handling within a line? */ Let's see /* RENDERTARGETS:It works */\n";

				return CommentDirectiveParser.findDirective(lines, CommentDirective.Type.RENDERTARGETS).map(CommentDirective::getDirective);
			});

			// OptiFine finds this directive, but ShadersMod does not...
			test("bad spacing from BSL composite6", Optional.of("12"), () -> {
				String line = "    /*DRAWBUFFERS:12*/";

				return CommentDirectiveParser.findDirective(line, CommentDirective.Type.DRAWBUFFERS).map(CommentDirective::getDirective);
			});
		}
	}
}
