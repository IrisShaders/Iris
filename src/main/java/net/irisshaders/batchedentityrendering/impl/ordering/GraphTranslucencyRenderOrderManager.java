package net.irisshaders.batchedentityrendering.impl.ordering;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.MapDigraph;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSet;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetPolicy;
import de.odysseus.ithaka.digraph.util.fas.FeedbackArcSetProvider;
import de.odysseus.ithaka.digraph.util.fas.SimpleFeedbackArcSetProvider;
import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class GraphTranslucencyRenderOrderManager implements RenderOrderManager {
	private final FeedbackArcSetProvider feedbackArcSetProvider;
	private final EnumMap<TransparencyType, Digraph<RenderType>> types;
	private final EnumMap<TransparencyType, RenderType> currentTypes;
	private boolean inGroup = false;

	public GraphTranslucencyRenderOrderManager() {
		feedbackArcSetProvider = new SimpleFeedbackArcSetProvider();
		types = new EnumMap<>(TransparencyType.class);
		currentTypes = new EnumMap<>(TransparencyType.class);

		for (TransparencyType type : TransparencyType.values()) {
			types.put(type, new MapDigraph<>());
		}
	}

	private static TransparencyType getTransparencyType(RenderType type) {
		while (type instanceof WrappableRenderType) {
			type = ((WrappableRenderType) type).unwrap();
		}

		if (type instanceof BlendingStateHolder) {
			return ((BlendingStateHolder) type).getTransparencyType();
		}

		// Default to "generally transparent" if we can't figure it out.
		return TransparencyType.GENERAL_TRANSPARENT;
	}

	public void begin(RenderType renderType) {
		TransparencyType transparencyType = getTransparencyType(renderType);
		Digraph<RenderType> graph = types.get(transparencyType);
		graph.add(renderType);

		if (inGroup) {
			RenderType previous = currentTypes.put(transparencyType, renderType);

			if (previous == null) {
				return;
			}

			int weight = graph.get(previous, renderType).orElse(0);
			weight += 1;
			graph.put(previous, renderType, weight);
		}
	}

	public void startGroup() {
		if (inGroup) {
			throw new IllegalStateException("Already in a group");
		}

		currentTypes.clear();
		inGroup = true;
	}

	public boolean maybeStartGroup() {
		if (inGroup) {
			return false;
		}

		currentTypes.clear();
		inGroup = true;
		return true;
	}

	public void endGroup() {
		if (!inGroup) {
			throw new IllegalStateException("Not in a group");
		}

		currentTypes.clear();
		inGroup = false;
	}

	@Override
	public void reset() {
		// TODO: Is reallocation efficient?
		types.clear();

		for (TransparencyType type : TransparencyType.values()) {
			types.put(type, new MapDigraph<>());
		}
	}

	@Override
	public void resetType(TransparencyType type) {
		// TODO: Is reallocation efficient?
		types.put(type, new MapDigraph<>());
	}

	public List<RenderType> getRenderOrder() {
		int layerCount = 0;

		for (Digraph<RenderType> graph : types.values()) {
			layerCount += graph.getVertexCount();
		}

		List<RenderType> allLayers = new ArrayList<>(layerCount);

		for (Digraph<RenderType> graph : types.values()) {
			// TODO: Make sure that FAS can't become a bottleneck!
			// Running NP-hard algorithms in a real time rendering loop might not be an amazing idea.
			// This shouldn't be necessary in sane scenes, though, and if there aren't cycles,
			// then this *should* be relatively inexpensive, since it'll bail out and return an empty set.
			FeedbackArcSet<RenderType> arcSet =
				feedbackArcSetProvider.getFeedbackArcSet(graph, graph, FeedbackArcSetPolicy.MIN_WEIGHT);

			if (arcSet.getEdgeCount() > 0) {
				// This means that our dependency graph had cycles!!!
				// This is very weird and isn't expected - but we try to handle it gracefully anyways.

				// Our feedback arc set algorithm finds some dependency links that can be removed hopefully
				// without disrupting the overall order too much. Hopefully it isn't too slow!
				for (RenderType source : arcSet.vertices()) {
					for (RenderType target : arcSet.targets(source)) {
						graph.remove(source, target);
					}
				}
			}

			allLayers.addAll(Digraphs.toposort(graph, false));
		}

		return allLayers;
	}
}
