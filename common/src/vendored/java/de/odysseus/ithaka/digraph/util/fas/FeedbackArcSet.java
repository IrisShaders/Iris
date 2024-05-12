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
import de.odysseus.ithaka.digraph.UnmodifiableDigraph;

/**
 * Feedback arc set.
 *
 * @param <V> vertex type
 */
public class FeedbackArcSet<V> extends UnmodifiableDigraph<V> {
	private final FeedbackArcSetPolicy policy;
	private final boolean exact;
	private final int weight;

	public FeedbackArcSet(Digraph<V> feedback, int weight, FeedbackArcSetPolicy policy, boolean exact) {
		super(feedback);
		this.weight = weight;
		this.policy = policy;
		this.exact = exact;
	}

	public static <V> FeedbackArcSet<V> empty(FeedbackArcSetPolicy policy) {
		return new FeedbackArcSet<>(Digraphs.emptyDigraph(), 0, policy, true);
	}

	/**
	 * @return <code>true</code> if this FAS is known to be of minimal
	 */
	public boolean isExact() {
		return exact;
	}

	/**
	 * @return total weight
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * @return minimization policy (weight/#arcs)
	 */
	public FeedbackArcSetPolicy getPolicy() {
		return policy;
	}
}
