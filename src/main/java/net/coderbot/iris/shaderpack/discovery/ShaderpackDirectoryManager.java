package net.coderbot.iris.shaderpack.discovery;

import net.coderbot.iris.Iris;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class ShaderpackDirectoryManager {
	private final Path root;

	public ShaderpackDirectoryManager(Path root) {
		this.root = root;
	}

	public void copyPackIntoDirectory(String name, Path source) throws IOException {
		Path target = Iris.getShaderpacksDirectory().resolve(name);

		// Copy the pack file into the shaderpacks folder.
		Files.copy(source, target);
		// Zip or other archive files will be copied without issue,
		// however normal folders will require additional handling below.

		// Manually copy the contents of the pack if it is a folder
		if (Files.isDirectory(source)) {
			// Use for loops instead of forEach due to createDirectory throwing an IOException
			// which requires additional handling when used in a lambda

			// Copy all sub folders, collected as a list in order to prevent issues with non-ordered sets
			for (Path p : Files.walk(source).filter(Files::isDirectory).collect(Collectors.toList())) {
				Path folder = source.relativize(p);

				if (Files.exists(folder)) {
					continue;
				}

				Files.createDirectory(target.resolve(folder));
			}

			// Copy all non-folder files
			for (Path p : Files.walk(source).filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet())) {
				Path file = source.relativize(p);

				Files.copy(p, target.resolve(file));
			}
		}
	}

	public Collection<String> enumerate() throws IOException {
		return Files.list(root).filter(Iris::isValidShaderpack)
				.map(path -> path.getFileName().toString()).collect(Collectors.toList());
	}

	public URI getDirectoryUri() {
		return root.toUri();
	}
}
