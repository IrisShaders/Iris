package net.coderbot.iris.shaderpack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.Util;

import static net.coderbot.iris.shaderpack.DefineOptionParser.*;

public class ConstOptionParser {

	private static final Pattern CONST_BOOLEAN_PATTERN = Pattern.compile("const\\s+bool\\s+(?<name>\\w+)\\s*=\\s*(?<value>true|false)\\s*;\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?");

	private static final Pattern CONST_FLOAT_PATTERN = Pattern.compile("const\\s+float\\s+(?<name>\\w+)\\s*=\\s*(?<value>-?[\\d.fF]+)\\s*;\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?");

	private static final Pattern CONST_INT_PATTERN = Pattern.compile("const\\s+int\\s+(?<name>\\w+)\\s*=\\s*(?<value>-?\\d+)\\s*;\\s*(?<comment>(?<commentChar>//+)(?<commentContent>.*))?");

	private static final Set<String> CONST_VARIABLE_NAMES = Util.make(new HashSet<>(), (set) -> {
		set.add("shadowMapResolution");
		set.add("shadowMapFov");
		set.add("shadowDistance");
		set.add("shadowDistanceRenderMul");
		set.add("shadowIntervalSize");
		set.add("generateShadowMipmap");
		set.add("generateShadowColorMipmap");
		set.add("shadowHardwareFiltering");
		set.add("shadowHardwareFiltering0");
		set.add("shadowHardwareFiltering1");
		set.add("shadowtex0Mipmap");
		set.add("shadowtexMipmap");
		set.add("shadowtex1Mipmap");
		set.add("shadowcolor0Mipmap");
		set.add("shadowColor0Mipmap");
		set.add("shadowcolor1Mipmap");
		set.add("shadowColor1Mipmap");
		set.add("shadowtex0Nearest");
		set.add("shadowtexNearest");
		set.add("shadow0MinMagNearest");
		set.add("shadowtex1Nearest");
		set.add("shadow1MinMagNearest");
		set.add("shadowcolor0Nearest");
		set.add("shadowColor0Nearest");
		set.add("shadowColor0MinMagNearest");
		set.add("shadowcolor1Nearest");
		set.add("shadowColor1Nearest");
		set.add("shadowColor1MinMagNearest");
		set.add("wetnessHalflife");
		set.add("drynessHalflife");
		set.add("eyeBrightnessHalflife");
		set.add("centerDepthHalflife");
		set.add("sunPathRotation");
		set.add("ambientOcclusionLevel");
		set.add("superSamplingLevel");
		set.add("noiseTextureResolution");
	});


	public static void processConstOptions(List<String> lines, ShaderPackConfig config) {
		for (int i = 0; i < lines.size(); i++) {
			String trimmedLine = lines.get(i).trim();

			Matcher booleanMatcher = CONST_BOOLEAN_PATTERN.matcher(trimmedLine);
			Matcher floatMatcher = CONST_FLOAT_PATTERN.matcher(trimmedLine);
			Matcher intMatcher = CONST_INT_PATTERN.matcher(trimmedLine);

			if (!booleanMatcher.matches() && !floatMatcher.matches() && !intMatcher.matches()) {
				continue;
			}

			if (booleanMatcher.matches()) {
				String name = group(booleanMatcher, "name");
				String value = group(booleanMatcher, "value");
				String comment = group(booleanMatcher, "commentContent");

				if (name == null || value == null)
					continue; //not sure how this is possible since the regex matches, but to be safe we will ignore it

				if (!CONST_VARIABLE_NAMES.contains(name)) {
					continue;
				}

				Option<Boolean> booleanOption = createConstBooleanOption(name, value, comment, config);

				lines.set(i, trimmedLine.replaceFirst(value, booleanOption.getValue().toString()));
			} else if (floatMatcher.matches()) {
				String name = group(floatMatcher, "name");
				String value = group(floatMatcher, "value");
				String comment = group(floatMatcher, "commentContent");

				if (name == null || value == null)
					continue; //not sure how this is possible since the regex matches, but to be safe we will ignore it

				if (!CONST_VARIABLE_NAMES.contains(name)) {
					continue;
				}

				Option<Float> floatOption = createFloatOption(name, comment, value, config);

				if (floatOption != null) {
					lines.set(i, trimmedLine.replaceFirst(value, floatOption.getValue().toString()));
				}

			} else if (intMatcher.matches()) {
				String name = group(intMatcher, "name");
				String value = group(intMatcher, "value");
				String comment = group(intMatcher, "commentContent");

				if (name == null || value == null)
					continue; //not sure how this is possible since the regex matches, but to be safe we will ignore it

				if (!CONST_VARIABLE_NAMES.contains(name)) {
					continue;
				}

				Option<Integer> integerOption = createIntegerOption(name, comment, value, config);

				if (integerOption != null) {
					lines.set(i, trimmedLine.replaceFirst(value, integerOption.getValue().toString()));
				}
			}
		}
	}

	private static Option<Boolean> createConstBooleanOption(String name, String value, String comment, ShaderPackConfig config) {
		boolean defaultValue = Boolean.parseBoolean(value);

		Option<Boolean> booleanOption = new Option<>(comment, Arrays.asList(true, false), name, defaultValue, Boolean::parseBoolean);

		booleanOption = config.processOption(booleanOption);

		return config.addBooleanOption(booleanOption);
	}


}
