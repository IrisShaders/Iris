package net.coderbot.iris.shaderpack.option;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.include.IncludeGraph;
import net.coderbot.iris.shaderpack.option.values.MutableOptionValues;
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
	private final OptionSet optionSet;
	private final OptionValues optionValues;
	private final IncludeGraph includes;

	public ShaderPackOptions(IncludeGraph graph, Map<String, String> changedConfigs) {
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

		this.optionSet = setBuilder.build();
		this.optionValues = new MutableOptionValues(optionSet, changedConfigs);

		this.includes = graph.map(path -> allAnnotations.get(path).asTransform(optionValues));
	}

	public OptionSet getOptionSet() {
		return optionSet;
	}

	public OptionValues getOptionValues() {
		return optionValues;
	}

	public IncludeGraph getIncludes() {
		return includes;
	}
}
