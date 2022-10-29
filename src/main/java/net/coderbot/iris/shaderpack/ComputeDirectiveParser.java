package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector3i;

public class ComputeDirectiveParser {
	public static void setComputeWorkGroups(ComputeSource source, ConstDirectiveParser.ConstDirective directive) {
		if (!directive.getValue().startsWith("ivec3")) {
			Iris.logger.error("Failed to process " + directive + ": value was not a valid ivec3 constructor");
		}

		String ivec3Args = directive.getValue().substring("ivec3".length()).trim();

		if (!ivec3Args.startsWith("(") || !ivec3Args.endsWith(")")) {
			Iris.logger.error("Failed to process " + directive + ": value was not a valid ivec3 constructor");
		}

		ivec3Args = ivec3Args.substring(1, ivec3Args.length() - 1);

		String[] parts = ivec3Args.split(",");

		for (int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();
		}

		if (parts.length != 3) {
			Iris.logger.error("Failed to process " + directive +
				": expected 3 arguments to a ivec3 constructor, got " + parts.length);
		}

		try {
			source.setWorkGroups(new Vector3i(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2])));
		} catch (NumberFormatException e) {
			Iris.logger.error("Failed to process " + directive, e);
		}
	}

	public static void setComputeWorkGroupsRelative(ComputeSource source, ConstDirectiveParser.ConstDirective directive) {
		if (!directive.getValue().startsWith("vec2")) {
			Iris.logger.error("Failed to process " + directive + ": value was not a valid vec2 constructor");
		}

		String vec2Args = directive.getValue().substring("vec2".length()).trim();

		if (!vec2Args.startsWith("(") || !vec2Args.endsWith(")")) {
			Iris.logger.error("Failed to process " + directive + ": value was not a valid vec2 constructor");
		}

		vec2Args = vec2Args.substring(1, vec2Args.length() - 1);

		String[] parts = vec2Args.split(",");

		for (int i = 0; i < parts.length; i++) {
			parts[i] = parts[i].trim();
		}

		if (parts.length != 2) {
			Iris.logger.error("Failed to process " + directive +
				": expected 2 arguments to a vec2 constructor, got " + parts.length);
		}

		try {
			source.setWorkGroupRelative(new Vector2f(
				Float.parseFloat(parts[0]),
				Float.parseFloat(parts[1])
			));
		} catch (NumberFormatException e) {
			Iris.logger.error("Failed to process " + directive, e);
		}
	}
}
