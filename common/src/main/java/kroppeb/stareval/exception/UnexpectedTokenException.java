package kroppeb.stareval.exception;

public class UnexpectedTokenException extends ParseException {
	public UnexpectedTokenException(String message, int index) {
		super(message + " at index " + index);
	}
}
