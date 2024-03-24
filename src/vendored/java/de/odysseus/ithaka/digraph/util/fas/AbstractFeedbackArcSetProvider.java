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

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.Digraphs;
import de.odysseus.ithaka.digraph.EdgeWeights;
import de.odysseus.ithaka.digraph.MapDigraph;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Abstract feedback arc set provider.
 */
public abstract class AbstractFeedbackArcSetProvider implements FeedbackArcSetProvider {
	private final ExecutorService executor;

	/**
	 * Create provider which calculates a feedback arc set on a de.odysseus.ithaka.digraph (in the
	 * current thread).
	 * <p>
	 * The provider decomposes a de.odysseus.ithaka.digraph into strongly connected components and computes
	 * feedback arc sets on the components and combines the results.
	 * <p>
	 * The {@link #mfas(Digraph, EdgeWeights)} and {@link #lfas(Digraph, EdgeWeights)}
	 * implementation methods do not have to handle arbitrary digraphs for this reason.
	 */
	protected AbstractFeedbackArcSetProvider() {
		this.executor = null;
	}

	/**
	 * Create provider which decomposes a de.odysseus.ithaka.digraph into strongly connected components
	 * and computes feedback arc sets on the components and combines the results.
	 * Feedback calculations can be distributed to a given number of threads.
	 * If <code>numberOfThreads == 0</code>, calculation is done in the current thread.
	 *
	 * @param numberOfThreads number
	 */
	protected AbstractFeedbackArcSetProvider(int numberOfThreads) {
		if (numberOfThreads > 0) {
			this.executor = Executors.newFixedThreadPool(numberOfThreads);
		} else {
			this.executor = null;
		}
	}

	/**
	 * Compute minimum feedback arc set.
	 *
	 * @return feedback arc set or <code>null</code>
	 */
	protected <V> Digraph<V> mfas(Digraph<V> digraph, EdgeWeights<? super V> weights) {
		return null;
	}

	/**
	 * Compute light feedback arc set.
	 *
	 * @param digraph original graph or tangle of it (if decompose == true)
	 * @return feedback arc set
	 */
	protected abstract <V> Digraph<V> lfas(Digraph<V> digraph, EdgeWeights<? super V> weights);

	private <V> FeedbackArcSet<V> fas(Digraph<V> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy) {
		EdgeWeights<? super V> filteredWeights = weights;
		if (policy == FeedbackArcSetPolicy.MIN_SIZE) {
			/*
			 * Manipulate graph weights if the feedback arc set has to be of minimum size (i.e., #arcs):
			 * all weights are increased by an amount (delta) equal to the sum of all weights,
			 * so that every arc is heavier than any arc subset with the original weights.
			 * A minimum weight feedback arc set (mwfas) of the resulting graph has a total weight of
			 *  #arcs(mwfas) * delta + origWeight(mwfas).
			 * Since origWeight(mwfas) < delta, the determined mwfas has a minimum #arcs and
			 * from all those feedback arc sets of minimum size it has minimum original weight (we could
			 * have obtained the first result easily by setting all weights to 1, but not the second).
			 */
			final EdgeWeights<? super V> origWeights = weights;
			final int delta = totalWeight(digraph, origWeights);
			filteredWeights = new EdgeWeights<V>() {
				@Override
				public OptionalInt get(V source, V target) {
					OptionalInt original = origWeights.get(source, target);

					if (original.isPresent()) {
						return OptionalInt.of(original.getAsInt() + delta);
					} else {
						return OptionalInt.empty();
					}
				}
			};
		}
		Digraph<V> result = mfas(digraph, filteredWeights);
		boolean exact = true;
		if (result == null) {
			result = lfas(digraph, filteredWeights);
			exact = false;
		}
		return new FeedbackArcSet<>(result, totalWeight(result, weights), policy, exact);
	}

	protected <V> int totalWeight(Digraph<V> digraph, EdgeWeights<? super V> weights) {
		int weight = 0;
		for (V source : digraph.vertices()) {
			for (V target : digraph.targets(source)) {
				weight += weights.get(source, target).getAsInt();
			}
		}
		return weight;
	}

	private <V> List<FeedbackArcSet<V>> executeAll(List<FeedbackTask<V>> tasks) {
		List<FeedbackArcSet<V>> result = new ArrayList<>();

		if (executor == null) {
			for (FeedbackTask<V> task : tasks) {
				result.add(task.call());
			}
		} else {
			try {
				for (Future<FeedbackArcSet<V>> future : executor.invokeAll(tasks)) {
					result.add(future.get());
				}
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
				return null; // should not happen
			}
		}

		return result;
	}

	@Override
	public <V> FeedbackArcSet<V> getFeedbackArcSet(Digraph<V> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy) {
		if (Digraphs.isTriviallyAcyclic(digraph)) {
			// known acyclic based on low vertex count
			return FeedbackArcSet.empty(policy);
		}

		List<Set<V>> components = Digraphs.scc(digraph);

		if (components.size() == digraph.getVertexCount()) {
			// known acyclic based on strongly connected components
			return FeedbackArcSet.empty(policy);
		}

		if (components.size() == 1) {
			return fas(digraph, weights, policy);
		}

		List<FeedbackTask<V>> tasks = new ArrayList<>();

		for (Set<V> component : components) {
			if (component.size() > 1) {
				tasks.add(new FeedbackTask<>(digraph, weights, policy, component));
			}
		}

		List<FeedbackArcSet<V>> feedbacks = executeAll(tasks);
		if (feedbacks == null) {
			return null;
		}

		int weight = 0;
		boolean exact = true;

		Digraph<V> result = new MapDigraph<>();
		for (FeedbackArcSet<V> feedback : feedbacks) {
			for (V source : feedback.vertices()) {
				for (V target : feedback.targets(source)) {
					result.put(source, target, digraph.get(source, target).getAsInt());
				}
			}
			exact &= feedback.isExact();
			weight += feedback.getWeight();
		}

		return new FeedbackArcSet<>(result, weight, policy, exact);
	}

	class FeedbackTask<V> implements Callable<FeedbackArcSet<V>> {
		final Digraph<V> digraph;
		final EdgeWeights<? super V> weights;
		final FeedbackArcSetPolicy policy;
		final Set<V> scc;

		FeedbackTask(Digraph<V> digraph, EdgeWeights<? super V> weights, FeedbackArcSetPolicy policy, Set<V> scc) {
			this.digraph = digraph;
			this.weights = weights;
			this.policy = policy;
			this.scc = scc;
		}

		@Override
		public FeedbackArcSet<V> call() {
			return fas(digraph.subgraph(scc), weights, policy);
		}
	}
}
