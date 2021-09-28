package kroppeb.stareval.exception;

public class UnexpectedEndingException extends ParseException {
	public UnexpectedEndingException() {
		this("Expected to read more text, but the string has ended");
	}

	public UnexpectedEndingException(String message) {
		super(message);
	}
}
