package net.irisshaders.iris.shaderpack.option;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OptionTests {
	public static void main(String[] args) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get("run/shaderpacks/Sildurs Vibrant Shaders v1.29 Medium/shaders/shaders.settings"));

		OptionAnnotatedSource source = new OptionAnnotatedSource(ImmutableList.copyOf(lines));

		System.out.println("Boolean Options:");

		if (source.getBooleanOptions().isEmpty()) {
			System.out.println("(none)");
		} else {
			System.out.println("[Line] Type   | Name                             | Value | Comment");
			System.out.println("       ------ | -------------------------------- | ----- | -------");
		}

		source.getBooleanOptions().forEach((index, option) -> {
			String type = option.getType() == OptionType.CONST ? " Const" : "Define";

			System.out.println(
				"[" + StringUtils.leftPad(Integer.toString(index + 1), 4, ' ') + "] " + type + " | " +
					StringUtils.rightPad(option.getName(), 32, ' ') + " | " +
					StringUtils.leftPad(Boolean.toString(option.getDefaultValue()), 5, ' ') +
					" | " + option.getComment().orElse(""));
		});


		System.out.println("String Options:");

		if (source.getStringOptions().isEmpty()) {
			System.out.println("(none)");
		} else {
			System.out.println("[Line] | Type   | Name                             | Value    | Allowed Values");
			System.out.println("       | ------ | -------------------------------- | -------- | -------");
		}

		source.getStringOptions().forEach((index, option) -> {
			String type = option.getType() == OptionType.CONST ? " Const" : "Define";

			System.out.println(
				"[" + StringUtils.leftPad(Integer.toString(index + 1), 4, ' ') + "] | " + type + " | " +
					StringUtils.rightPad(option.getName(), 32, ' ') + " | " +
					StringUtils.leftPad(option.getDefaultValue(), 8, ' ') +
					" | " + option.getAllowedValues());
			System.out.println("       " + option.getComment().orElse("(no comment)"));
		});

		System.out.println("Diagnostics:");
		source.getDiagnostics().forEach((index, diagnostic) -> {
			System.out.println(
				"[" + StringUtils.leftPad(Integer.toString(index + 1), 4, ' ') + "] " +
					diagnostic);
		});

		if (source.getDiagnostics().isEmpty()) {
			System.out.println("(none)");
		}
	}
}
