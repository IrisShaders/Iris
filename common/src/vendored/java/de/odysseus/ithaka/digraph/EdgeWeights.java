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
package de.odysseus.ithaka.digraph;

import java.util.OptionalInt;

/**
 * Edge weights interface.
 *
 * @param <V> vertex type
 */
public interface EdgeWeights<V> {
	// We're storing this in order to avoid reallocating this optional constantly.
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	OptionalInt UNIT_WEIGHT = OptionalInt.of(1);

	/**
	 * Unit edge weights.
	 */
	EdgeWeights<Object> UNIT_WEIGHTS = (source, target) -> UNIT_WEIGHT;

	/**
	 * Get edge weight
	 *
	 * @param source source vertex
	 * @param target target vertex
	 * @return weight for edge starting at <code>source</code> and ending at <code>target</code>
	 */
	OptionalInt get(V source, V target);
}
