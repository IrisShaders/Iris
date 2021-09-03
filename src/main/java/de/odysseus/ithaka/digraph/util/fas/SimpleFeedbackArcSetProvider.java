/*
 * Copyright 2012 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.odysseus.ithaka.digraph.util.fas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphFactory;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.MapDigraph;

/**
 * Simple feedback arc set provider.
 */
public class SimpleFeedbackArcSetProvider extends AbstractFeedbackArcSetProvider {
	/**
	 * Calculate feedback arc in the current thread.
	 */
	public SimpleFeedbackArcSetProvider() {
		super();
	}

	/**
	 * Calculate feedback arc set using the specified number of threads.
	 */
	public SimpleFeedbackArcSetProvider(int numberOfThreads) {
		super(numberOfThreads);
	}

	/**
	 * create equivalent graphs with different edge orderings.
	 *
	 * @param digraph digraph to copy
	 * @return list of copies
	 */
	private <V> List<Digraph<V>> copies(Digraph<V> digraph, int count) {
		List<Digraph<V>> copies = new ArrayList<>();
		copies.add(digraph);

		final List<Integer> shuffle = new ArrayList<>();
		final Map<V, Integer> order = new HashMap<>();
		int index = 0;
		for (V source : digraph.vertices()) {
			order.put(source, index);
			shuffle.add(index++);
		}

		Random random = new Random(7);
		for (int i = 0; i < count; i++) {
			Collections.shuffle(shuffle, random);
			List<Integer> mapping = new ArrayList<>(shuffle);

			copies.add(Digraphs.copy(digraph, new DigraphFactory<Digraph<V>>() {
				@Override
				public Digraph<V> create() {
					return new MapDigraph<>(new Comparator<V>() {
						@Override
						public int compare(V v1, V v2) {
							int value1 = mapping.get(order.get(v1));
							int value2 = mapping.get(order.get(v2));
							return Integer.compare(value1, value2);
						}
					});
				}
			}));
		}
		return copies;
	}

	/**
	 * Compute simple feedback arc set by performing |n| DFS traversals (each starting
	 * with a different vertex) on the tangle, taking non-forward edges as feedback.
	 * The minimum weight feedback arc set among those |n| results is returned.
	 *
	 * @param tangle  strongly connected component
	 * @param weights edge weights
	 * @return feedback arc set
	 */
	@Override
	protected <V> Digraph<V> lfas(Digraph<V> tangle, EdgeWeights<? super V> weights) {
		/*
		 * store best results
		 */
		int minWeight = Integer.MAX_VALUE;
		int minSize = Integer.MAX_VALUE;
		List<V> minFinished = null;

		/*
		 * threshold on max. number of iterations (avoid running forever)
		 */
		int maxIterationsLeft = Math.max(1, 1000000 / (tangle.getVertexCount() + tangle.getEdgeCount()));

		/*
		 * perform DFS for each node, keep best result
		 */
		List<Digraph<V>> copies = copies(tangle, Math.min(10, tangle.getVertexCount()));
		List<V> finished = new ArrayList<>(tangle.getVertexCount());
		Set<V> discovered = new HashSet<>(tangle.getVertexCount());
		for (V start : tangle.vertices()) {
			for (Digraph<V> copy : copies) {
				finished.clear();
				discovered.clear();
				Digraphs.dfs(copy, start, discovered, finished);
				assert finished.size() == tangle.getVertexCount();

				int weight = 0;
				int size = 0;
				discovered.clear();
				for (V source : finished) {
					discovered.add(source);
					for (V target : tangle.targets(source)) {
						if (!discovered.contains(target)) { // feedback edge
							weight += weights.get(source, target).getAsInt();
							size++;
						}
					}
					if (weight > minWeight) {
						break;
					}
				}
				if (weight < minWeight || weight == minWeight && size < minSize) {
					minFinished = new ArrayList<>(finished);
					minWeight = weight;
					minSize = size;
				}
			}
			if (--maxIterationsLeft == 0) {
				break;
			}
		}

		// If the input graph has at least a single vertex, we'll get at least one result.
		Objects.requireNonNull(minFinished);

		/*
		 * create feedback graph
		 */
		Digraph<V> feedback = MapDigraph.<V>getDefaultDigraphFactory().create();
		discovered.clear();
		for (V source : minFinished) {
			discovered.add(source);
			for (V target : tangle.targets(source)) {
				if (!discovered.contains(target)) { // feedback edge
					feedback.put(source, target, tangle.get(source, target).getAsInt());
				}
			}
		}

		return feedback;
	}
}
