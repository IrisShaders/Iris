package net.irisshaders.iris.shaderpack.transform.line;

import com.google.common.collect.ImmutableList;

public interface LineTransform {
	static ImmutableList<String> apply(ImmutableList<String> lines, LineTransform transform) {
		ImmutableList.Builder<String> newLines = ImmutableList.builder();
		int index = 0;

		for (String line : lines) {
			newLines.add(transform.transform(index, line));
			index += 1;
		}

		return newLines.build();
	}

	String transform(int index, String line);
}
