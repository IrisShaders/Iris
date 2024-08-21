package net.irisshaders.iris.shaderpack.include;

import java.nio.file.NoSuchFileException;

public class FileIncludeException extends NoSuchFileException {
	public FileIncludeException(String message) {
		super(message);
	}
}
