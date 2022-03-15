package net.coderbot.iris.shaderpack.include;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.error.RusticError;
import net.coderbot.iris.shaderpack.transform.line.LineTransform;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A directed graph data structure that holds the loaded source of all shader programs
 * and the files included by each source file. Each node / vertex in the graph
 * corresponds to a single file in the shader pack, and each edge / connection
 * corresponds to an {@code #include} directive on a given line.
 *
 * <p>Using a proper graph representation allows us to apply existing and
 * efficient algorithms with well-known properties to various tasks and
 * transformations necessary during shader pack loading. We receive a number of
 * immediate benefits from using a graph-based representation:</p>
 *
 * <ul>
 *     <li>Each file is read exactly one time, and it is only necessary to
 *         parse #include directives from a file once. This ensures efficient
 *         IO.
 *         </li>
 *     <li>Deferring the processing of inclusions allows transformers that only
 *         need to replace single lines at a time to operate more efficiently,
 *         avoiding processing lines duplicated across many files more than
 *         necessary.
 *         </li>
 *     <li>As a result, our shader configuration system is able to process and
 *         apply options much more efficiently than a naive one operating on
 *         included files only, allowing many operations to scale much more
 *         nicely, especially in the common case of shader pack authors having
 *         a single large settings file defining every config option that is
 *         then included in every shader program in the pack.
 *         </li>
 *     <li>Deferred processing of inclusions also allows the shader pack loader
 *         to reason about cyclic inclusions, allowing us to remove the
 *         arbitrary file include depth limit, and avoid stack overflows due to
 *         infinite recursion that a naive implementation might be subject to.
 *         </li>
 * </ul>
 */
public class IncludeGraph {
	private final ImmutableMap<AbsolutePackPath, FileNode> nodes;
	private final ImmutableMap<AbsolutePackPath, RusticError> failures;

	private IncludeGraph(ImmutableMap<AbsolutePackPath, FileNode> nodes,
						 ImmutableMap<AbsolutePackPath, RusticError> failures) {
		this.nodes = nodes;
		this.failures = failures;
	}

	public IncludeGraph(Path root, ImmutableList<AbsolutePackPath> startingPaths) {
		Map<AbsolutePackPath, AbsolutePackPath> cameFrom = new HashMap<>();
		Map<AbsolutePackPath, Integer> lineNumberInclude = new HashMap<>();

		Map<AbsolutePackPath, FileNode> nodes = new HashMap<>();
		Map<AbsolutePackPath, RusticError> failures = new HashMap<>();

		List<AbsolutePackPath> queue = new ArrayList<>(startingPaths);
		Set<AbsolutePackPath> seen = new HashSet<>(startingPaths);

		while (!queue.isEmpty()) {
			AbsolutePackPath next = queue.remove(queue.size() - 1);

			String source;

			try {
				source = readFile(next.resolved(root));
			} catch (IOException e) {
				AbsolutePackPath src = cameFrom.get(next);

				if (src == null) {
					throw new RuntimeException("unexpected error: failed to read " + next.getPathString(), e);
				}

				String topLevelMessage;
				String detailMessage;

				if (e instanceof NoSuchFileException) {
					topLevelMessage = "failed to resolve #include directive";
					detailMessage = "file not found";
				} else {
					topLevelMessage = "unexpected I/O error while resolving #include directive: " + e;
					detailMessage = "IO error";
				}

				String badLine = nodes.get(src).getLines().get(lineNumberInclude.get(next)).trim();

				RusticError topLevelError = new RusticError("error", topLevelMessage, detailMessage, src.getPathString(),
					lineNumberInclude.get(next) + 1, badLine);

				failures.put(next, topLevelError);

				continue;
			}

			ImmutableList<String> lines = ImmutableList.copyOf(source.split("\\R"));

			FileNode node = new FileNode(next, lines);
			boolean selfInclude = false;

			for (Map.Entry<Integer, AbsolutePackPath> include : node.getIncludes().entrySet()) {
				int line = include.getKey();
				AbsolutePackPath included = include.getValue();

				if (next.equals(included)) {
					selfInclude = true;
					failures.put(next, new RusticError("error", "trivial #include cycle detected",
						"file includes itself", next.getPathString(), line + 1, lines.get(line)));

					break;
				} else if (!seen.contains(included)) {
					queue.add(included);
					seen.add(included);
					cameFrom.put(included, next);
					lineNumberInclude.put(included, line);
				}
			}

			if (!selfInclude) {
				nodes.put(next, node);
			}
		}

		this.nodes = ImmutableMap.copyOf(nodes);
		this.failures = ImmutableMap.copyOf(failures);

		detectCycle();
	}

	private void detectCycle() {
		List<AbsolutePackPath> cycle = new ArrayList<>();
		Set<AbsolutePackPath> visited = new HashSet<>();

		for (AbsolutePackPath start : nodes.keySet()) {
			if (exploreForCycles(start, cycle, visited)) {
				AbsolutePackPath lastFilePath = null;

				StringBuilder error = new StringBuilder();

				for (AbsolutePackPath node : cycle) {
					if (lastFilePath == null) {
						lastFilePath = node;
						continue;
					}

					FileNode lastFile = nodes.get(lastFilePath);
					int lineNumber = -1;

					for (Map.Entry<Integer, AbsolutePackPath> include : lastFile.getIncludes().entrySet()) {
						if (include.getValue() == node) {
							lineNumber = include.getKey() + 1;
						}
					}

					String badLine = lastFile.getLines().get(lineNumber - 1);

					String detailMessage = node.equals(start) ? "final #include in cycle" : "#include involved in cycle";

					if (lastFilePath.equals(start)) {
						// first node in cycle
						error.append(new RusticError("error", "#include cycle detected",
							detailMessage, lastFilePath.getPathString(), lineNumber, badLine));
					} else {
						error.append("\n  = " + new RusticError("note", "cycle involves another file",
							detailMessage, lastFilePath.getPathString(), lineNumber, badLine));
					}

					lastFilePath = node;
				}

				error.append(
					"\n  = note: #include directives are resolved before any other preprocessor directives, any form of #include guard will not work" +
						"\n  = note: other cycles may still exist, only the first detected non-trivial cycle will be reported");

				// TODO: Expose this to the caller (more semantic error handling)
				Iris.logger.error(error.toString());

				throw new IllegalStateException("Cycle detected in #include graph, see previous messages for details");
			}
		}
	}

	private boolean exploreForCycles(AbsolutePackPath frontier, List<AbsolutePackPath> path, Set<AbsolutePackPath> visited) {
		if (visited.contains(frontier)) {
			path.add(frontier);
			return true;
		}

		path.add(frontier);
		visited.add(frontier);

		for (AbsolutePackPath included : nodes.get(frontier).getIncludes().values()) {
			if (!nodes.containsKey(included)) {
				// file that failed to load for another reason, error should already be reported
				continue;
			}

			if (exploreForCycles(included, path, visited)) {
				return true;
			}
		}

		path.remove(path.size() - 1);
		visited.remove(frontier);

		return false;
	}

	public ImmutableMap<AbsolutePackPath, FileNode> getNodes() {
		return nodes;
	}

	public List<IncludeGraph> computeWeaklyConnectedComponents() {
		//List<IncludeGraph> components = new ArrayList<>();

		// TODO: WCC
		//throw new UnsupportedOperationException();

		//return components;

		// TODO: This digraph might not be weakly connected
		//       A digraph is weakly connected if its corresponding undirected
		//       graph is connected
		//       Make an adjacency list and then go from there
		return Collections.singletonList(this);
	}

	public IncludeGraph map(Function<AbsolutePackPath, LineTransform> transformProvider) {
		ImmutableMap.Builder<AbsolutePackPath, FileNode> mappedNodes = ImmutableMap.builder();

		nodes.forEach((path, node) -> mappedNodes.put(path, node.map(transformProvider.apply(path))));

		return new IncludeGraph(mappedNodes.build(), failures);
	}

	public ImmutableMap<AbsolutePackPath, RusticError> getFailures() {
		return failures;
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
