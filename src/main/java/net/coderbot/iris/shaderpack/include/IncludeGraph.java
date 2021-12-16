package net.coderbot.iris.shaderpack.include;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.shaderpack.transform.line.LineTransform;

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
// TODO: Write tests for this code
public class IncludeGraph {
	private final ImmutableMap<AbsolutePackPath, FileNode> nodes;
	private final ImmutableMap<AbsolutePackPath, IOException> failures;

	private IncludeGraph(ImmutableMap<AbsolutePackPath, FileNode> nodes,
						 ImmutableMap<AbsolutePackPath, IOException> failures) {
		this.nodes = nodes;
		this.failures = failures;
	}

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

	public IncludeGraph map(Function<AbsolutePackPath, LineTransform> transformProvider) {
		ImmutableMap.Builder<AbsolutePackPath, FileNode> mappedNodes = ImmutableMap.builder();

		nodes.forEach((path, node) -> {
			mappedNodes.put(path, node.map(transformProvider.apply(path)));
		});

		return new IncludeGraph(mappedNodes.build(), failures);
	}

	public ImmutableMap<AbsolutePackPath, IOException> getFailures() {
		return failures;
	}

	private static String readFile(Path path) throws IOException {
		return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
	}
}
