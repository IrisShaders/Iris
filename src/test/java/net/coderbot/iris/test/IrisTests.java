package net.coderbot.iris.test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IrisTests {
	public static Path getTestShaderPackPath(String name) {
		try {
			return Paths.get(IrisTests.class.getResource("/shaderpacks/" + name + "/shaders/").toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
