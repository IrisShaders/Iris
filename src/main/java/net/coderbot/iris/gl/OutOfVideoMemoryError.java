package net.coderbot.iris.gl;

public class OutOfVideoMemoryError extends RuntimeException {
	public OutOfVideoMemoryError(String error) {
		super(error);
	}
}
