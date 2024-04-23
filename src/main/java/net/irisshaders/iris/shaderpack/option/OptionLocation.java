package net.irisshaders.iris.shaderpack.option;

import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;

/**
 * Encapsulates a single location of an option.
 */
public record OptionLocation(AbsolutePackPath filePath, int lineIndex) {


	/**
	 * Gets the index of the line this option is on.
	 * Note that this is the index - so the first line is
	 * 0, the second is 1, etc.
	 */
	@Override
	public int lineIndex() {
		return lineIndex;
	}
}
