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

/**
 * Digraph holding its reverse graph and granting access to incoming edges.
 */
public interface DoubledDigraph<V> extends Digraph<V> {
	/**
	 * @return number of edges ending at <code>vertex</code>
	 */
	int getInDegree(V vertex);

	/**
	 * Iterate over edge sources for given target vertex.
	 *
	 * @param target target vertex
	 * @return edge sources of edges ending at <code>target</code>
	 */
	Iterable<V> sources(V target);

	/**
	 * Restrict result type.
	 */
	@Override
	DoubledDigraph<V> reverse();
}
