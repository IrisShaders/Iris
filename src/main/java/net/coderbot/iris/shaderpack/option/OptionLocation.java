package net.coderbot.iris.shaderpack.option;

import net.coderbot.iris.shaderpack.include.AbsolutePackPath;

/**
 * Encapsulates a single location of an option.
 */
public class OptionLocation {
    private final AbsolutePackPath filePath;
    private final int lineIndex;

    public OptionLocation(AbsolutePackPath filePath, int lineIndex) {
        this.filePath = filePath;
        this.lineIndex = lineIndex;
    }

    public AbsolutePackPath getFilePath() {
        return filePath;
    }

    /**
     * Gets the index of the line this option is on.
     * Note that this is the index - so the first line is
     * 0, the second is 1, etc.
     */
    public int getLineIndex() {
        return lineIndex;
    }
}
