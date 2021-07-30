package net.coderbot.iris.shaderpack.option;

/**
 * Encapsulates a single location of an option.
 */
public class OptionLocation {
    private final String filePath;
    private final int lineIndex;

    public OptionLocation(String filePath, int lineIndex) {
        this.filePath = filePath;
        this.lineIndex = lineIndex;
    }

    public String getFilePath() {
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
