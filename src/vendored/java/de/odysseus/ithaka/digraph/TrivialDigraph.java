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
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Convenience class representing a de.odysseus.ithaka.digraph with zero or one vertex and an optional loop edge.
 * Vertex as well as edge <code>null</code> is forbidden.
 *
 * @param <V> vertex type
 * @author beck
 */
public class TrivialDigraph<V> implements DoubledDigraph<V> {
	private V vertex;
	private boolean hasLoop;
	private int loopWeight;

	public TrivialDigraph() {
		vertex = null;
		hasLoop = false;
		loopWeight = 0;
	}

	/**
	 * @throws UnsupportedOperationException if adding the vertex would result in having 2 vertices in the graph
	 * @throws IllegalArgumentException      if <code>vertex == null</code>
	 */
	@Override
	public boolean add(V vertex) {
		if (vertex == null) {
			throw new IllegalArgumentException("Cannot add null vertex!");
		}

		if (this.vertex == null) {
			this.vertex = vertex;
			return true;
		}

		if (this.vertex.equals(vertex)) {
			return false;
		}

		throw new UnsupportedOperationException("TrivialDigraph must contain at most one vertex!");
	}

	@Override
	public boolean contains(Object source, Object target) {
		return vertex != null && hasLoop && vertex.equals(source) && vertex.equals(target);
	}

	@Override
	public boolean contains(Object vertex) {
		return this.vertex != null && this.vertex.equals(vertex);
	}

	@Override
	public OptionalInt get(Object source, Object target) {
		return contains(source, target) ? OptionalInt.of(loopWeight) : OptionalInt.empty();
	}

	@Override
	public int getInDegree(Object vertex) {
		return hasLoop ? 1 : 0;
	}

	@Override
	public int getOutDegree(Object vertex) {
		return hasLoop ? 1 : 0;
	}

	@Override
	public int getEdgeCount() {
		return hasLoop ? 1 : 0;
	}

	@Override
	public int getVertexCount() {
		return vertex == null ? 0 : 1;
	}

	@Override
	public int totalWeight() {
		return hasLoop ? loopWeight : 0;
	}

	@Override
	public Iterable<V> vertices() {
		if (vertex == null) {
			return Collections.emptyList();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					boolean hasNext = true;

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public V next() {
						if (hasNext) {
							hasNext = false;
							return vertex;
						}
						throw new NoSuchElementException("No more vertices");
					}

					@Override
					public void remove() {
						if (hasNext) {
							throw new IllegalStateException();
						}
						TrivialDigraph.this.remove(vertex);
					}
				};
			}

			@Override
			public String toString() {
				return "[" + vertex + "]";
			}
		};
	}

	@Override
	public OptionalInt put(V source, V target, int loopWeight) {
		if (source != target) {
			throw new UnsupportedOperationException("TrivialDigraph must not contain no-loop edges!");
		}

		OptionalInt previousLoopWeight = hasLoop ? OptionalInt.of(this.loopWeight) : OptionalInt.empty();
		add(source);
		this.hasLoop = true;
		this.loopWeight = loopWeight;

		return previousLoopWeight;
	}

	@Override
	public OptionalInt remove(V source, V target) {
		if (contains(source, target)) {
			int loopWeight = this.loopWeight;
			this.loopWeight = 0;
			this.hasLoop = false;
			return OptionalInt.of(loopWeight);
		}

		return OptionalInt.empty();
	}

	@Override
	public boolean remove(V vertex) {
		if (this.vertex != null && this.vertex.equals(vertex)) {
			this.vertex = null;
			this.loopWeight = 0;
			this.hasLoop = false;
			return true;
		}

		return false;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		if (vertices.contains(vertex)) {
			remove(vertex);
		}
	}

	@Override
	public DoubledDigraph<V> reverse() {
		return this;
	}

	@Override
	public Digraph<V> subgraph(Set<V> vertices) {
		return vertex != null && vertices.contains(vertex) ? this : Digraphs.emptyDigraph();
	}

	@Override
	public Iterable<V> sources(Object target) {
		return targets(target);
	}

	@Override
	public Iterable<V> targets(Object source) {
		if (!hasLoop || vertex == null || !vertex.equals(source)) {
			return Collections.emptyList();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					boolean hasNext = true;

					@Override
					public boolean hasNext() {
						return hasNext;
					}

					@Override
					public V next() {
						if (hasNext) {
							hasNext = false;
							return vertex;
						}
						throw new NoSuchElementException("No more vertices");
					}

					@Override
					public void remove() {
						if (hasNext) {
							throw new IllegalStateException();
						}
						TrivialDigraph.this.remove(vertex, vertex);
					}
				};
			}

			@Override
			public String toString() {
				return "[" + vertex + "]";
			}
		};
	}

	@Override
	public boolean isAcyclic() {
		return !hasLoop;
	}
}
