package net.coderbot.iris.gl.program;

public class GlUniform1iCall {
	private final int location;
	private final int value;

	public GlUniform1iCall(int location, int value) {
		this.location = location;
		this.value = value;
	}

	public int getLocation() {
		return location;
	}

	public int getValue() {
		return value;
	}
}
