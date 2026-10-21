package net.irisshaders.iris.shaderpack;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PackDirectories {
	private PackDirectories() {
	}

	public static Stream<Path> list(Path directory) throws IOException {
		if (Files.isDirectory(directory) || !Files.exists(directory) || !"jar".equals(directory.getFileSystem().provider().getScheme())) {
			return Files.list(directory);
		}

		final String uri = directory.toUri().getRawSchemeSpecificPart();
		final int separatorIndex = uri.lastIndexOf("!/");
		if (separatorIndex < 0) {
			return Files.list(directory);
		}

		final FileSystem fileSystem = directory.getFileSystem();
		final String prefix = directory.toAbsolutePath().toString().substring(1) + "/";
		final Set<Path> children = new LinkedHashSet<>();
		try (final ZipFile zip = new ZipFile(Path.of(URI.create(uri.substring(0, separatorIndex))).toFile())) {
			for (final ZipEntry entry : Collections.list(zip.entries())) {
				final String name = entry.getName();
				if (name.startsWith(prefix) && name.length() != prefix.length()) {
					final String remaining = name.substring(prefix.length());
					final int slashIndex = remaining.indexOf('/');
					children.add(fileSystem.getPath(prefix + (slashIndex < 0 ? remaining : remaining.substring(0, slashIndex))));
				}
			}
		}

		return children.stream();
	}
}
