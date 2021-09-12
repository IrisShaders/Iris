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
		/*// We iterate over the list of lines in reverse order because the last occurrence of a directive has a greater
		// precedence over an earlier occurrence of a directive.
		for (int index = lines.size() - 1; index >= 0; index -= 1) {
			String line = lines.get(index);

			Optional<String> found = findDirective(line, key);

			if (found.isPresent()) {
				return found;
			}
		}*/

		// TODO: Temporary workaround for the fact that BSL uses multiple drawbuffers directives in some files, and
		// expects preprocessor directives for comment directives to be properly, handled. But we don't do that! After
		// observation, it seems like the first DRAWBUFFERS directive is generally the "default" directive.
		//
		// This is important because if we add a draw buffer that isn't written to, undefined behavior happens!
		for (String line : lines) {
			Optional<String> found = findDirectiveAndType(line, key, false);

			if (found.isPresent()) {
				return found;
			}
		}

		// Didn't find any declarations of the directive.
		return Optional.empty();
	}

	public static Optional<String> findDirectiveAndType(String haystack, String needle, boolean searchingForType) {
		String prefix;
		String suffix;
		if (searchingForType) {
			prefix = "/*";
			suffix = ":";
		} else {
			prefix = needle + ":";
			suffix = "*/";
		}


		/*// Search for the last occurrence of the directive within the text, since those take precedence.
		int indexOfPrefix = haystack.lastIndexOf(prefix);*/

		// TODO: Temporary workaround for the fact that BSL uses multiple drawbuffers directives in some files, and
		// expects preprocessor directives for comment directives to be properly, handled. But we don't do that! After
		// observation, it seems like the first DRAWBUFFERS directive is generally the "default" directive.
		//
		// This is important because if we add a draw buffer that isn't written to, undefined behavior happens!
		int indexOfPrefix;

		if ((haystack.contains("https://bitslablab.com") || haystack.contains("By LexBoosT")) && needle.equals("DRAWBUFFERS")) {
			indexOfPrefix = haystack.indexOf(prefix);
		} else {
			indexOfPrefix = haystack.lastIndexOf(prefix);
		}

		if (indexOfPrefix == -1) {
			return Optional.empty();
		}

		// TODO: But in this case, the second DRAWBUFFERS directive is the default one!!!
		// TODO: Actually process #ifdef and #ifndef before processing comment directives!!!!
		// This hack is needed to get BSL reflections to work for now until we do that.
		if (!searchingForType && haystack.contains("REFLECTION_PREVIOUS")
				&& (haystack.contains("https://bitslablab.com") || haystack.contains("By LexBoosT"))
				&& haystack.contains("/*DRAWBUFFERS:05*/") && needle.equals("DRAWBUFFERS")) {
			return Optional.of("05");
		}

		// TODO: This is a similar hack but just for complementary.
		if (haystack.contains("COLORED_LIGHT") && haystack.contains("Complementary Shaders by EminGT")
				&& haystack.contains("/* DRAWBUFFERS:03618 */") && needle.equals("DRAWBUFFERS") && !searchingForType) {
			return Optional.of("0361");
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

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("partial directive", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /* DRAWBUFFERS: directive of any sort";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("bad spacing", Optional.of("321"), () -> {
				String line = "/*DRAWBUFFERS:321*/ OptiFine will detect this directive, but ShadersMod will not...";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("matchAtEnd", Optional.of("321"), () -> {
				String line = "A line containg a drawbuffers directive: /* DRAWBUFFERS:321 */";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("matchAtStart", Optional.of("31"), () -> {
				String line = "/* DRAWBUFFERS:31 */ This is a line containg a drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("matchInMiddle", Optional.of("31"), () -> {
				String line = "This is a line /* DRAWBUFFERS:31 */ containg a drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("emptyMatch", Optional.of(""), () -> {
				String line = "/* DRAWBUFFERS: */ This is a line containg an invalid but still matching drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("duplicates", Optional.of("3"), () -> {
				String line = "/* TEST:2 */ This line contains multiple directives, the last one should be used /* TEST:3 */";

				return CommentDirectiveParser.findDirectiveAndType(line, "TEST", false);
			});

			// OptiFine finds this directive, but ShadersMod does not...
			test("bad spacing from BSL composite6", Optional.of("12"), () -> {
				String line = "    /*DRAWBUFFERS:12*/";

				return CommentDirectiveParser.findDirectiveAndType(line, "DRAWBUFFERS", false);
			});

			test("normal text rendertargets", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a RENDERTARGETS directive of any sort";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("partial directive rendertargets", Optional.empty(), () -> {
				String line = "Some normal text that doesn't contain a /* RENDERTARGETS: directive of any sort";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("bad spacing rendertargets", Optional.of("3,2,1"), () -> {
				String line = "/*RENDERTARGETS:3,2,1*/ OptiFine will detect this directive, but ShadersMod will not...";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("matchAtEnd rendertargets", Optional.of("3,2,1"), () -> {
				String line = "A line containg a drawbuffers directive: /* RENDERTARGETS:3,2,1 */";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("matchAtStart rendertargets", Optional.of("3,1"), () -> {
				String line = "/* RENDERTARGETS:3,1 */ This is a line containg a drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("matchInMiddle rendertargets", Optional.of("3,1"), () -> {
				String line = "This is a line /* RENDERTARGETS:3,1 */ containg a drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("double digits rendertargets", Optional.of("12,1,15,5"), () -> {
				String line = "/* RENDERTARGETS: 12,1,15,5 */";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
			});

			test("emptyMatch rendertargets", Optional.of(""), () -> {
				String line = "/* RENDERTARGETS: */ This is a line containg an invalid but still matching drawbuffers directive";

				return CommentDirectiveParser.findDirectiveAndType(line, "RENDERTARGETS", false);
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
