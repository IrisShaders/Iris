package kroppeb.stareval.parser;

import kroppeb.stareval.exception.UnexpectedCharacterException;

/**
 * A class to facilitate the reading of strings.
 */
public class StringReader {
	/**
	 * The string we are reading
	 */
	private final String string;
	/**
	 * The next index to read at
	 */
	private int nextIndex = -1;
	/**
	 * The last index read
	 */
	private int lastIndex;
	/**
	 * The start of {@link #substring()}
	 */
	private int mark;


	public StringReader(String string) {
		this.string = string;
		this.advanceOneCharacter();
		this.skipWhitespace();

		// Initializing these variables to the actual start of the string.
		this.lastIndex = this.nextIndex;
		this.mark();
	}

	/**
	 * moves the indexes, nextIndex will skip all spaces.
	 */
	private void advanceOneCharacter() {
		this.lastIndex = this.nextIndex;

		if (this.nextIndex >= this.string.length()) {
			return;
		}

		this.nextIndex++;
	}

	/**
	 * Skips all whitespace characters until a non-whitespace character is encountered.
	 */
	public void skipWhitespace() {
		while (this.nextIndex < this.string.length() && Character.isWhitespace(this.string.charAt(this.nextIndex))) {
			this.nextIndex++;
		}
	}

	/**
	 * @return The character that would be returned by the next call to {@link #read}
	 */
	public char peek() {
		return this.string.charAt(this.nextIndex);
	}

	/**
	 * Skips the current character
	 */
	public void skipOneCharacter() {
		this.advanceOneCharacter();
	}

	/**
	 * Read a character
	 */
	public char read() {
		char current = this.peek();
		this.skipOneCharacter();
		return current;
	}

	/**
	 * Read a character and verify it's the expected character.
	 */
	public void read(char c) throws UnexpectedCharacterException {
		char read = this.read();

		if (read != c) {
			throw new UnexpectedCharacterException(c, read, this.getCurrentIndex());
		}
	}

	/**
	 * Try to read a character.
	 *
	 * @param c the character to read
	 * @return whether it could read the character.
	 */
	public boolean tryRead(char c) {
		if (!this.canRead()) {
			return false;
		}

		char read = this.peek();

		if (read != c) {
			return false;
		}

		this.skipOneCharacter();
		return true;
	}

	/**
	 * Place a mark at the character that just got read. Used for {@link #substring()}
	 */
	public void mark() {
		this.mark = this.lastIndex;
	}

	/**
	 * @return a string, starting at the last call to {@link #mark()}, up to and including the last read character
	 */
	public String substring() {
		return this.string.substring(this.mark, this.lastIndex + 1);
	}

	/**
	 * @return whether there is more text to read.
	 */
	public boolean canRead() {
		return this.nextIndex < this.string.length();
	}

	public int getCurrentIndex() {
		return this.lastIndex;
	}
}
