package net.irisshaders.iris.shaderpack.include;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

// TODO: Write tests for this
public class AbsolutePackPath {
	private final String path;

	private AbsolutePackPath(String absolute) {
		this.path = absolute;
	}

	public static AbsolutePackPath fromAbsolutePath(String absolutePath) {
		return new AbsolutePackPath(normalizeAbsolutePath(absolutePath));
	}

	private static String normalizeAbsolutePath(String path) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("Not an absolute path: " + path);
		}

		String[] segments = path.split(Pattern.quote("/"));
		List<String> parsedSegments = new ArrayList<>();

		for (String segment : segments) {
			if (segment.isEmpty() || segment.equals(".")) {
				continue;
			}

			if (segment.equals("..")) {
				if (!parsedSegments.isEmpty()) {
					parsedSegments.remove(parsedSegments.size() - 1);
				}
			} else {
				parsedSegments.add(segment);
			}
		}

		if (parsedSegments.isEmpty()) {
			return "/";
		}

		StringBuilder normalized = new StringBuilder();

		for (String segment : parsedSegments) {
			normalized.append('/');
			normalized.append(segment);
		}

		return normalized.toString();
	}

	public Optional<AbsolutePackPath> parent() {
		if (path.equals("/")) {
			return Optional.empty();
		}

		int lastSlash = path.lastIndexOf('/');

		return Optional.of(new AbsolutePackPath(path.substring(0, lastSlash)));
	}

	public AbsolutePackPath resolve(String path) {
		if (path.startsWith("/")) {
			return fromAbsolutePath(path);
		}

		String merged;

		if (!this.path.endsWith("/") & !path.startsWith("/")) {
			merged = this.path + "/" + path;
		} else {
			merged = this.path + path;
		}

		return fromAbsolutePath(merged);
	}

	public Path resolved(Path root) {
		if (path.equals("/")) {
			return root;
		}

		return root.resolve(path.substring(1));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AbsolutePackPath that = (AbsolutePackPath) o;
		return Objects.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path);
	}

	@Override
	public String toString() {
		return "AbsolutePackPath {" + getPathString() + "}";
	}

	public String getPathString() {
		return path;
	}
}
