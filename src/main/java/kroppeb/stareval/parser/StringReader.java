package kroppeb.stareval.parser;

/**
 * A class to facilitate the reading of strings, will completely ignore all spaces.
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
		this.move();

		// Initializing these variables to the actual start of the string.
		this.lastIndex = this.nextIndex;
		this.mark();
	}

	/**
	 * moves the indexes, nextIndex will skip all spaces.
	 */
	private void move() {
		this.lastIndex = this.nextIndex;

		if (this.nextIndex >= this.string.length()) {
			return;
		}

		this.nextIndex++;

		while (this.nextIndex < this.string.length() && this.string.charAt(this.nextIndex) == ' ') {
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
	public void skip() {
		this.move();
	}

	/**
	 * Read a character
	 */
	public char read() {
		char current = this.peek();
		this.skip();
		return current;
	}

	/**
	 * Read a character and verify it's the expected character.
	 */
	public void read(char c) throws Exception {
		char read = this.read();

		if (read != c) {
			throw new Exception("unexpected character: '" + read + "' expected '" + c + "'");
		}
	}

	/**
	 * Try to read a character.
	 *
	 * @param c the character to read
	 * @return whether it could read the character.
	 */
	public boolean tryRead(char c) {
		char read = this.peek();

		if (read != c) {
			return false;
		}

		this.skip();
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
}
