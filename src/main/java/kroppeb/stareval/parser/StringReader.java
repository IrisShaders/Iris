package kroppeb.stareval.parser;

public class StringReader {
	final String string;
	int index = 0;
	int lastIndex = 0;
	int nextIndex = 0;
	int mark = 0;
	
	
	public StringReader(String string) {
		this.string = string;
		this.skipWhiteSpace();
	}
	
	private void skipWhiteSpace() {
		this.lastIndex = this.index;
		this.index = this.nextIndex;
		if (index >= this.string.length())
			return;
		this.nextIndex++;
		while (this.nextIndex < this.string.length() && this.string.charAt(this.nextIndex) == ' ') {
			nextIndex++;
		}
	}
	
	public char peek() {
		return string.charAt(this.index);
	}
	
	public void skip() {
		this.skipWhiteSpace();
	}
	
	public char read() {
		char current = peek();
		skip();
		return current;
	}
	
	public void read(char c) throws Exception {
		char read = read();
		if(read != c)
			throw new Exception("unexpected character: '" + read + "' expected '" + c + "'");
	}
	
	public boolean tryRead(char c){
		char read = peek();
		if(read != c)
			return false;
		skip();
		return true;
	}
	
	public void mark() {
		this.mark = this.lastIndex;
	}
	
	
	public String substring() {
		return this.string.substring(this.mark, this.lastIndex + 1);
	}
	
	public boolean canRead() {
		return this.index < this.string.length();
	}
}
