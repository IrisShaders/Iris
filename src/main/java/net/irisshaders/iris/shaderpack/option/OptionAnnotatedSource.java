package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.irisshaders.iris.helpers.OptionalBoolean;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.option.values.OptionValues;
import net.irisshaders.iris.shaderpack.parsing.ParsedString;
import net.irisshaders.iris.shaderpack.properties.PackShadowDirectives;
import net.irisshaders.iris.shaderpack.transform.line.LineTransform;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class encapsulates the source code of a single shader source file along with the
 * corresponding configurable options within the source file.
 * <p>
 * The shader configuration system revolves around a carefully defined way of directly editing
 * shader source files in order to change configuration options. This class handles the first
 * step of that process—discovering configurable options from shader source files—as well as
 * the final step of that process—editing shader source files to apply the modified values
 * of valid configuration options.
 * <p>
 * Intermediate steps of that process include considering the annotated source for all shader
 * source files within a shader pack in order to deduplicate options that are common to multiple
 * source files, and discarding options that are ambiguous between source files. In addition,
 * another step includes loading changed option values from on-disk configuration files.
 * <p>
 * The name "OptionAnnotatedSource" is based on the fact that this class simultaneously
 * stores a snapshot of the shader source code at the time of option discovery, as well
 * as data for each line ("annotations") about the relevant option represented by that
 * line, or alternatively an optional diagnostic message for that line saying why a potential
 * option was not parsed as a valid shader option.
 * <p>
 * Note that for the most part, each line of the file is parsed in isolation from every
 * other line. This means that option conflicts can arise even within the same source file,
 * where option declarations have the same name and type but different default values.
 * The only exception to this isolation is
 * {@link OptionAnnotatedSource#getBooleanDefineReferences() boolean define reference tracking},
 * which is nevertheless still relatively context-free.
 * <p>
 * The data stored within this class is immutable. This ensures that once you have discovered
 * options from a given shader source file, that you may then apply any changed option values
 * without having to re-parse the shader source code for options, and without risking having
 * the shader source code fall out of sync with the annotations.
 */
public final class OptionAnnotatedSource {
	private static final ImmutableSet<String> VALID_CONST_OPTION_NAMES;

	static {
		ImmutableSet.Builder<String> values = ImmutableSet.<String>builder().add(
			"shadowMapResolution",
			"shadowDistance",
			"voxelDistance",
			"shadowDistanceRenderMul",
			"entityShadowDistanceMul",
			"shadowIntervalSize",
			"generateShadowMipmap",
			"generateShadowColorMipmap",
			"shadowHardwareFiltering",
			"shadowtex0Mipmap",
			"shadowtexMipmap",
			"shadowtex1Mipmap",
			"shadowtex0Nearest",
			"shadowtexNearest",
			"shadow0MinMagNearest",
			"shadowtex1Nearest",
			"shadow1MinMagNearest",
			"wetnessHalflife",
			"drynessHalflife",
			"eyeBrightnessHalflife",
			"centerDepthHalflife",
			"sunPathRotation",
			"ambientOcclusionLevel",
			"superSamplingLevel",
			"noiseTextureResolution"
		);

		for (int i = 0; i < PackShadowDirectives.MAX_SHADOW_COLOR_BUFFERS_IRIS; i++) {
			values.add("shadowcolor" + i + "Mipmap");
			values.add("shadowColor" + i + "Mipmap");
			values.add("shadowcolor" + i + "Nearest");
			values.add("shadowColor" + i + "Nearest");
			values.add("shadowcolor" + i + "MinMagNearest");
			values.add("shadowColor" + i + "MinMagNearest");
			values.add("shadowHardwareFiltering" + i);
		}

		VALID_CONST_OPTION_NAMES = values.build();
	}

	/**
	 * The content of each line within this shader source file.
	 */
	private final ImmutableList<String> lines;
	private final ImmutableMap<Integer, BooleanOption> booleanOptions;
	private final ImmutableMap<Integer, StringOption> stringOptions;
	/**
	 * Optional diagnostic messages for each line. The parser may notice that though a shader pack
	 * author may have intended for a line to be a valid option, Iris might have ignored it due to
	 * a syntax error or some other issue.
	 * <p>
	 * These diagnostic messages provide reasons for why Iris decided to ignore a plausible option
	 * line, as well as hints for how an invalid option line can be modified to be a valid one.
	 */
	private final ImmutableMap<Integer, String> diagnostics;
	/**
	 * Keeps track of references to boolean #define options. Correlates the name of the #define
	 * option to one of the lines it was referenced on.
	 * <p>
	 * References to boolean #define options that happen in plain #if directives are not analyzed
	 * for the purposes of determining whether a boolean #define option is referenced or not, to
	 * match OptiFine behavior. Though this might have originally been an oversight, shader packs
	 * now anticipate this behavior, so it must be replicated here. Since it would be complex to
	 * fully parse #if directives, this also makes the code simpler.
	 * <p>
	 * Note that for the purposes of "confirming" a boolean #define option, it does not matter
	 * where the reference occurs in a given file - only that it is used at least once in the
	 * same "logical file" (that is, a file after all #includes have been processed) as it is
	 * defined. This is because shader config options are parsed as if all #include directives
	 * have already been substituted for the relevant file.
	 */
	// TODO: Use an immutable list type
	private final ImmutableMap<String, IntList> booleanDefineReferences;

	public OptionAnnotatedSource(final String source) {
		// Match any valid newline sequence
		// https://stackoverflow.com/a/31060125
		this(ImmutableList.copyOf(source.split("\\R")));
	}

	/**
	 * Parses the lines of a shader source file in order to locate valid options from it.
	 */
	public OptionAnnotatedSource(final ImmutableList<String> lines) {
		this.lines = lines;

		AnnotationsBuilder builder = new AnnotationsBuilder();

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index);
			parseLine(builder, index, line);
		}

		this.booleanOptions = builder.booleanOptions.build();
		this.stringOptions = builder.stringOptions.build();
		this.diagnostics = builder.diagnostics.build();
		this.booleanDefineReferences = ImmutableMap.copyOf(builder.booleanDefineReferences);
	}

	private static void parseLine(AnnotationsBuilder builder, int index, String lineText) {
		// Check to see if this line contains anything of interest before we try to parse it.
		if (!lineText.contains("#define")
			&& !lineText.contains("const")
			&& !lineText.contains("#ifdef")
			&& !lineText.contains("#ifndef")) {
			// Nothing of interest.
			return;
		}

		// Parse the trimmed form of the line to ignore indentation and trailing whitespace.
		ParsedString line = new ParsedString(lineText.trim());

		if (line.takeLiteral("#ifdef") || line.takeLiteral("#ifndef")) {
			// The presence of #ifdef and #ifndef directives is used to determine whether a given
			// boolean option should be recognized as a configurable option.
			//
			// As noted above, #if and #elif directives are not checked even though they may also
			// contain references.
			parseIfdef(builder, index, line);
		} else if (line.takeLiteral("const")) {
			parseConst(builder, index, line);
		} else if (line.currentlyContains("#define")) {
			parseDefineOption(builder, index, line);
		}
	}

	private static void parseIfdef(AnnotationsBuilder builder, int index, ParsedString line) {
		if (!line.takeSomeWhitespace()) {
			return;
		}

		String name = line.takeWord();

		line.takeSomeWhitespace();

		if (name == null || !line.isEnd()) {
			return;
		}

		builder.booleanDefineReferences
			.computeIfAbsent(name, n -> new IntArrayList()).add(index);
	}

	private static void parseConst(AnnotationsBuilder builder, int index, ParsedString line) {
		// const is already taken.

		if (!line.takeSomeWhitespace()) {
			builder.diagnostics.put(index, "Expected whitespace after const and before type declaration");
			return;
		}

		boolean isString;

		if (line.takeLiteral("int") || line.takeLiteral("float")) {
			isString = true;
		} else if (line.takeLiteral("bool")) {
			isString = false;
		} else {
			builder.diagnostics.put(index, "Unexpected type declaration after const. " +
				"Expected int, float, or bool. " +
				"Vector const declarations cannot be configured using shader options.");
			return;
		}

		if (!line.takeSomeWhitespace()) {
			builder.diagnostics.put(index, "Expected whitespace after type declaration.");
			return;
		}

		String name = line.takeWord();

		if (name == null) {
			builder.diagnostics.put(index, "Expected name of option after type declaration, " +
				"but an unexpected character was detected first.");
			return;
		}

		line.takeSomeWhitespace();

		if (!line.takeLiteral("=")) {
			builder.diagnostics.put(index, "Unexpected characters before equals sign in const declaration.");
			return;
		}

		line.takeSomeWhitespace();

		String value = line.takeWordOrNumber();

		if (value == null) {
			builder.diagnostics.put(index, "Unexpected non-whitespace characters after equals sign");
			return;
		}

		line.takeSomeWhitespace();

		if (!line.takeLiteral(";")) {
			builder.diagnostics.put(index, "Value between the equals sign and the semicolon wasn't parsed as a valid word or number.");
			return;
		}

		line.takeSomeWhitespace();

		String comment;

		if (line.takeComments()) {
			comment = line.takeRest().trim();
		} else if (!line.isEnd()) {
			builder.diagnostics.put(index, "Unexpected non-whitespace characters outside of comment after semicolon");
			return;
		} else {
			comment = null;
		}

		if (!isString) {
			boolean booleanValue;

			if ("true".equals(value)) {
				booleanValue = true;
			} else if ("false".equals(value)) {
				booleanValue = false;
			} else {
				builder.diagnostics.put(index, "Expected true or false as the value of a boolean const option, but got "
					+ value + ".");
				return;
			}

			if (!VALID_CONST_OPTION_NAMES.contains(name)) {
				builder.diagnostics.put(index, "This was a valid const boolean option declaration, but " + name +
					" was not recognized as being a name of one of the configurable const options.");
				return;
			}

			builder.booleanOptions.put(index, new BooleanOption(OptionType.CONST, name, comment, booleanValue));
			return;
		}

		if (!VALID_CONST_OPTION_NAMES.contains(name)) {
			builder.diagnostics.put(index, "This was a valid const option declaration, but " + name +
				" was not recognized as being a name of one of the configurable const options.");
			return;
		}

		StringOption option = StringOption.create(OptionType.CONST, name, comment, value);

		if (option != null) {
			builder.stringOptions.put(index, option);
		} else {
			builder.diagnostics.put(index, "Ignoring this const option because it is missing an allowed values list" +
				"in a comment, but is not a boolean const option.");
		}
	}

	private static void parseDefineOption(AnnotationsBuilder builder, int index, ParsedString line) {
		// Remove the leading comment for processing.
		boolean hasLeadingComment = line.takeComments();

		// allow but do not require whitespace between comments and #define
		line.takeSomeWhitespace();

		if (!line.takeLiteral("#define")) {
			builder.diagnostics.put(index,
				"This line contains an occurrence of \"#define\" " +
					"but it wasn't in a place we expected, ignoring it.");
			return;
		}

		if (!line.takeSomeWhitespace()) {
			builder.diagnostics.put(index,
				"This line properly starts with a #define statement but doesn't have " +
					"any whitespace characters after the #define.");
			return;
		}

		String name = line.takeWord();

		if (name == null) {
			builder.diagnostics.put(index,
				"Invalid syntax after #define directive. " +
					"No alphanumeric or underscore characters detected.");
			return;
		}

		// Maybe take some whitespace
		boolean tookWhitespace = line.takeSomeWhitespace();

		if (line.isEnd()) {
			// Plain define directive without a comment.
			builder.booleanOptions.put(index, new BooleanOption(OptionType.DEFINE, name, null, !hasLeadingComment));
			return;
		}

		if (line.takeComments()) {
			// Note that this is a bare comment, we don't need to look for the allowed values part.
			// Obviously that part isn't necessary since boolean options only have two possible
			// values (true and false)
			String comment = line.takeRest().trim();

			builder.booleanOptions.put(index, new BooleanOption(OptionType.DEFINE, name, comment, !hasLeadingComment));
			return;
		} else if (!tookWhitespace) {
			// Invalid syntax.
			builder.diagnostics.put(index,
				"Invalid syntax after #define directive. Only alphanumeric or underscore " +
					"characters are allowed in option names.");

			return;
		}

		if (hasLeadingComment) {
			builder.diagnostics.put(index,
				"Ignoring potential non-boolean #define option since it has a leading comment. " +
					"Leading comments (//) are only allowed on boolean #define options.");
			return;
		}

		String value = line.takeWordOrNumber();

		if (value == null) {
			builder.diagnostics.put(index, "Ignoring this #define directive because it doesn't appear to be a boolean #define, " +
				"and its potential value wasn't a valid number or a valid word.");
			return;
		}

		tookWhitespace = line.takeSomeWhitespace();

		if (line.isEnd()) {
			builder.diagnostics.put(index, "Ignoring this #define because it doesn't have a comment containing" +
				" a list of allowed values afterwards, but it has a value so is therefore not a boolean.");
			return;
		} else if (!tookWhitespace) {
			if (!line.takeComments()) {
				builder.diagnostics.put(index,
					"Invalid syntax after value #define directive. " +
						"Invalid characters after number or word.");
				return;
			}
		} else if (!line.takeComments()) {
			builder.diagnostics.put(index,
				"Invalid syntax after value #define directive. " +
					"Only comments may come after the value.");
			return;
		}

		String comment = line.takeRest().trim();

		StringOption option = StringOption.create(OptionType.DEFINE, name, comment, value);

		if (option == null) {
			builder.diagnostics.put(index, "Ignoring this #define because it is missing an allowed values list" +
				"in a comment, but is not a boolean define.");
			return;
		}

		builder.stringOptions.put(index, option);

		/*
	    //#define   SHADOWS // Whether shadows are enabled
		SHADOWS // Whether shadows are enabled
		// Whether shadows are enabled
		Whether shadows are enabled



		#define OPTION 0.5 // A test option
		OPTION 0.5 // A test option
		0.5 // A test option
		*/
	}

	private static boolean hasLeadingComment(String line) {
		return line.trim().startsWith("//");
	}

	private static String removeLeadingComment(String line) {
		ParsedString parsed = new ParsedString(line);

		parsed.takeSomeWhitespace();
		parsed.takeComments();

		return parsed.takeRest();
	}

	private static String setBooleanDefineValue(String line, OptionalBoolean newValue, boolean defaultValue) {
		if (hasLeadingComment(line) && newValue.orElse(defaultValue)) {
			return removeLeadingComment(line);
		} else if (!newValue.orElse(defaultValue)) {
			return "//" + line;
		} else {
			return line;
		}
	}

	public ImmutableMap<Integer, BooleanOption> getBooleanOptions() {
		return booleanOptions;
	}

	public ImmutableMap<Integer, StringOption> getStringOptions() {
		return stringOptions;
	}

	public ImmutableMap<Integer, String> getDiagnostics() {
		return diagnostics;
	}

	public ImmutableMap<String, IntList> getBooleanDefineReferences() {
		return booleanDefineReferences;
	}

	public OptionSet getOptionSet(AbsolutePackPath filePath, Set<String> booleanDefineReferences) {
		OptionSet.Builder builder = OptionSet.builder();

		booleanOptions.forEach((lineIndex, option) -> {
			if (booleanDefineReferences.contains(option.getName())) {
				OptionLocation location = new OptionLocation(filePath, lineIndex);
				builder.addBooleanOption(location, option);
			}
		});

		stringOptions.forEach((lineIndex, option) -> {
			OptionLocation location = new OptionLocation(filePath, lineIndex);
			builder.addStringOption(location, option);
		});

		return builder.build();
	}

	public LineTransform asTransform(OptionValues values) {
		return (index, line) -> edit(values, index, line);
	}

	public String apply(OptionValues values) {
		StringBuilder source = new StringBuilder();

		for (int index = 0; index < lines.size(); index++) {
			source.append(edit(values, index, lines.get(index)));
			source.append('\n');
		}

		return source.toString();
	}

	private String edit(OptionValues values, int index, String existing) {
		// See if it's a boolean option
		BooleanOption booleanOption = booleanOptions.get(index);

		if (booleanOption != null) {
			OptionalBoolean value = values.getBooleanValue(booleanOption.getName());
			if (booleanOption.getType() == OptionType.DEFINE) {
				return setBooleanDefineValue(existing, value, booleanOption.getDefaultValue());
			} else if (booleanOption.getType() == OptionType.CONST) {
				if (value != OptionalBoolean.DEFAULT) {
					// Value will never be default here, but we're using orElse just to get a normal boolean out of it.
					return editConst(existing, Boolean.toString(booleanOption.getDefaultValue()), Boolean.toString(value.orElse(booleanOption.getDefaultValue())));
				} else {
					return existing;
				}
			} else {
				throw new AssertionError("Unknown option type " + booleanOption.getType());
			}
		}

		StringOption stringOption = stringOptions.get(index);

		if (stringOption != null) {
			return values.getStringValue(stringOption.getName()).map(value -> {
				if (stringOption.getType() == OptionType.DEFINE) {
					return "#define " + stringOption.getName() + " " + value + " // OptionAnnotatedSource: Changed option";
				} else if (stringOption.getType() == OptionType.CONST) {
					return editConst(existing, stringOption.getDefaultValue(), value);
				} else {
					throw new AssertionError("Unknown option type " + stringOption.getType());
				}
			}).orElse(existing);
		}

		return existing;
	}

	private String editConst(String line, String currentValue, String newValue) {
		int equalsIndex = line.indexOf('=');

		if (equalsIndex == -1) {
			// This shouldn't be possible.
			throw new IllegalStateException();
		}

		String firstPart = line.substring(0, equalsIndex);
		String secondPart = line.substring(equalsIndex);

		secondPart = secondPart.replaceFirst(Pattern.quote(currentValue), Matcher.quoteReplacement(newValue));

		return firstPart + secondPart;
	}

	private static class AnnotationsBuilder {
		private final ImmutableMap.Builder<Integer, BooleanOption> booleanOptions;
		private final ImmutableMap.Builder<Integer, StringOption> stringOptions;
		private final ImmutableMap.Builder<Integer, String> diagnostics;
		private final Map<String, IntList> booleanDefineReferences;

		private AnnotationsBuilder() {
			booleanOptions = ImmutableMap.builder();
			stringOptions = ImmutableMap.builder();
			diagnostics = ImmutableMap.builder();
			booleanDefineReferences = new HashMap<>();
		}
	}
}
