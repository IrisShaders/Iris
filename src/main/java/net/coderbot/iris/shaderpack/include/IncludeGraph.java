package net.coderbot.iris.shaderpack.include;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TODO: Write tests for this code
public class IncludeGraph {
	private final ImmutableMap<AbsolutePackPath, FileNode> nodes;
	private final ImmutableMap<AbsolutePackPath, IOException> failures;

	public IncludeGraph(Path root, ImmutableList<AbsolutePackPath> startingPaths) {
		Map<AbsolutePackPath, FileNode> nodes = new HashMap<>();
		Map<AbsolutePackPath, IOException> failures = new HashMap<>();

		List<AbsolutePackPath> queue = new ArrayList<>(startingPaths);
		Set<AbsolutePackPath> seen = new HashSet<>(startingPaths);

		while (!queue.isEmpty()) {
			AbsolutePackPath next = queue.remove(queue.size() - 1);

			String source;

			try {
				source = readFile(next.resolved(root));
			} catch (IOException e) {
				failures.put(next, e);
				continue;
			}

			ImmutableList<String> lines = ImmutableList.copyOf(source.split("\\R"));

			FileNode node = new FileNode(next, lines);
			nodes.put(next, node);

			ImmutableCollection<AbsolutePackPath> includes = node.getIncludes().values();

			for (AbsolutePackPath included : includes) {
				if (!seen.contains(included)) {
					queue.add(included);
					seen.add(included);
				}
			}
		}

		this.nodes = ImmutableMap.copyOf(nodes);
		this.failures = ImmutableMap.copyOf(failures);
	}

	public ImmutableMap<AbsolutePackPath, FileNode> getNodes() {
		return nodes;
	}

	public ImmutableMap<AbsolutePackPath, IOException> getFailures() {
		return failures;
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
