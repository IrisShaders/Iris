package net.irisshaders.iris.gl.shader;

public class ShaderCompileException extends RuntimeException {
	private final String filename;
	private final String error;

	public ShaderCompileException(String filename, String error) {
		super(filename + ": " + error);

		this.filename = filename;
		this.error = error;
	}

	public ShaderCompileException(String filename, Exception error) {
		super(error);

		this.filename = filename;
		this.error = error.getMessage();
	}

	@Override
	public String getMessage() {
		return filename + ": " + super.getMessage();
	}

	public String getError() {
		return error;
	}

	public String getFilename() {
		return filename;
	}
}
