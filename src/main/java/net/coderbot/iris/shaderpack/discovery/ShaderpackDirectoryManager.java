package net.coderbot.iris.shaderpack.discovery;

import net.coderbot.iris.Iris;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class ShaderpackDirectoryManager {
	private final Path root;

	public ShaderpackDirectoryManager(Path root) {
		this.root = root;
	}

	public Collection<String> enumerate() throws IOException {
		return Files.list(root).filter(Iris::isValidShaderpack)
				.map(path -> path.getFileName().toString()).collect(Collectors.toList());
	}
}
