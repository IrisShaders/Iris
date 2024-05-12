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

import java.util.Collection;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Directed graph interface.
 *
 * @param <V> vertex type
 */
public interface Digraph<V> extends EdgeWeights<V> {
	/**
	 * Get an edge.
	 *
	 * @param source source vertex
	 * @param target target vertex
	 * @return edge weight (<code>0</code> if there is no edge from <code>source</code> to <code>target</code>)
	 */
	OptionalInt get(V source, V target);

	/**
	 * Edge test.
	 *
	 * @param source source vertex
	 * @param target target vertex
	 * @return <code>true</code> iff this de.odysseus.ithaka.digraph contains an edge from <code>source</code> to <code>target</code>
	 */
	boolean contains(V source, V target);

	/**
	 * Vertex test
	 *
	 * @return <code>true</code> iff this de.odysseus.ithaka.digraph contains <code>vertex</code>
	 */
	boolean contains(V vertex);

	/**
	 * Add vertex.
	 *
	 * @return <code>true</code> iff <code>vertex</code> has been added
	 */
	boolean add(V vertex);

	/**
	 * Put an edge.
	 * Vertices are added automatically if they appear in an edge.
	 *
	 * @param source source vertex
	 * @param target target vertex
	 * @param weight edge weight
	 * @return edge weight that has been previously set (<code>0</code> if there was no edge from <code>source</code>
	 * to <code>target</code>)
	 */
	OptionalInt put(V source, V target, int weight);

	/**
	 * Remove an edge.
	 *
	 * @param source source vertex
	 * @param target target vertex
	 * @return edge weight that has been previously set (<code>0</code> if there was no edge from <code>source</code>
	 * to <code>target</code>)
	 */
	OptionalInt remove(V source, V target);

	/**
	 * Remove a vertex.
	 *
	 * @param vertex vertex
	 * @return <code>true</code> iff this de.odysseus.ithaka.digraph contained <code>vertex</code>
	 */
	boolean remove(V vertex);

	/**
	 * Remove all vertices.
	 *
	 * @param vertices vertices
	 */
	void removeAll(Collection<V> vertices);

	/**
	 * Iterate over vertices.
	 *
	 * @return vertices
	 */
	Iterable<V> vertices();

	/**
	 * Iterate over edge targets for given source vertex.
	 *
	 * @param source source vertex
	 * @return edge targets of edges starting at <code>source</code>
	 */
	Iterable<V> targets(V source);

	/**
	 * @return number of vertices in this de.odysseus.ithaka.digraph
	 */
	int getVertexCount();

	/**
	 * @return sum of edge weights
	 */
	int totalWeight();

	/**
	 * @return number of edges starting at <code>vertex</code>
	 */
	int getOutDegree(V vertex);

	/**
	 * @return number of edges in this de.odysseus.ithaka.digraph
	 */
	int getEdgeCount();

	/**
	 * @return <code>true</code> iff this de.odysseus.ithaka.digraph is acyclic (i.e. it is a DAG)
	 */
	boolean isAcyclic();

	/**
	 * Get reverse de.odysseus.ithaka.digraph (same vertices, with edges reversed).
	 *
	 * @return reverse de.odysseus.ithaka.digraph
	 */
	Digraph<V> reverse();

	/**
	 * Get induced subgraph (with vertices in this de.odysseus.ithaka.digraph and the given vertex set and edges that appear in this de.odysseus.ithaka.digraph over the given vertex set).
	 *
	 * @return subgraph
	 */
	Digraph<V> subgraph(Set<V> vertices);
}
