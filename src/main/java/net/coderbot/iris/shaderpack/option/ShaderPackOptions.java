package net.coderbot.iris.shaderpack.option;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.include.IncludeGraph;
import net.coderbot.iris.shaderpack.option.values.OptionValues;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A helper class that dispatches all the heavy lifting needed to discover, merge, and apply shader pack options to
 * an existing {@link IncludeGraph}.
 */
public class ShaderPackOptions {
	public static IncludeGraph apply(IncludeGraph graph, Map<String, String> changedConfigs) {
		Map<AbsolutePackPath, OptionAnnotatedSource> allAnnotations = new HashMap<>();
		OptionSet.Builder setBuilder = OptionSet.builder();

		graph.computeWeaklyConnectedSubgraphs().forEach(subgraph -> {
			ImmutableMap.Builder<AbsolutePackPath, OptionAnnotatedSource> annotationBuilder = ImmutableMap.builder();
			Set<String> referencedBooleanDefines = new HashSet<>();

			subgraph.getNodes().forEach((path, node) -> {
				OptionAnnotatedSource annotatedSource = new OptionAnnotatedSource(node.getLines());
				annotationBuilder.put(path, annotatedSource);
				referencedBooleanDefines.addAll(annotatedSource.getBooleanDefineReferences().keySet());
			});

			ImmutableMap<AbsolutePackPath, OptionAnnotatedSource> annotations = annotationBuilder.build();
			Set<String> referencedBooleanDefinesU = Collections.unmodifiableSet(referencedBooleanDefines);

			annotations.forEach((path, annotatedSource) -> {
				OptionSet set = annotatedSource.getOptionSet(path, referencedBooleanDefinesU);
				setBuilder.addAll(set);
			});

			allAnnotations.putAll(annotations);
		});

		OptionSet optionSet = setBuilder.build();
		OptionValues optionValues = new OptionValues(optionSet, changedConfigs);

		return graph.map(path -> allAnnotations.get(path).asTransform(optionValues));
	}
}
