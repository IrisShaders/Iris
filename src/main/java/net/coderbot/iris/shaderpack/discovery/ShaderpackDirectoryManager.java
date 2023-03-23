package net.coderbot.iris.shaderpack.discovery;

import net.coderbot.iris.Iris;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
			try (Stream<Path> stream = Files.walk(source)) {
				for (Path p : stream.filter(Files::isDirectory).collect(Collectors.toList())) {
					Path folder = source.relativize(p);

					if (Files.exists(folder)) {
						continue;
					}

					Files.createDirectory(target.resolve(folder));
				}
			}

			// Copy all non-folder files
			try (Stream<Path> stream = Files.walk(source)) {
				for (Path p : stream.filter(p -> !Files.isDirectory(p)).collect(Collectors.toSet())) {
					Path file = source.relativize(p);

					Files.copy(p, target.resolve(file));
				}
			}
		}
	}

	public List<String> enumerate() throws IOException {
		// Make sure the list is sorted since not all OSes sort the list of files in the directory.
		// Case-insensitive sorting is the most intuitive for the user, but we then sort naturally
		// afterwards so that we don't alternate cases weirdly in the sorted list.
		//
		// We also ignore chat formatting characters when sorting - some shader packs include chat
		// formatting in the file name so that they have fancy text when displayed in the shaders list.
		Comparator<String> baseComparator = String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder());
		Comparator<String> comparator = (a, b) -> {
			a = removeFormatting(a);
			b = removeFormatting(b);

			return baseComparator.compare(a, b);
		};

		try (Stream<Path> list = Files.list(root)) {
			return list.filter(Iris::isValidToShowPack)
				.map(path -> path.getFileName().toString())
				.sorted(comparator).collect(Collectors.toList());
		}
	}

	/**
	 * Straightforward method to use section-sign based chat formatting from a String
	 */
	private static String removeFormatting(String formatted) {
		char[] original = formatted.toCharArray();
		char[] cleaned = new char[original.length];
		int c = 0;

		for (int i = 0; i < original.length; i++) {
			// check if it's a section sign
			if (original[i] == '\u00a7') {
				i++;
			} else {
				cleaned[c++] = original[i];
			}
		}

		return new String(cleaned, 0, c);
	}

	public URI getDirectoryUri() {
		return root.toUri();
	}
}
