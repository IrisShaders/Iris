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
 * Abstract Digraph adapter.
 * A de.odysseus.ithaka.digraph adapter delegates to a de.odysseus.ithaka.digraph supplied at construction time.
 *
 * @param <V> vertex type
 */
public abstract class DigraphAdapter<V> implements Digraph<V> {
	private final Digraph<V> delegate;

	public DigraphAdapter(Digraph<V> delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean add(V vertex) {
		return delegate.add(vertex);
	}

	@Override
	public boolean contains(V source, V target) {
		return delegate.contains(source, target);
	}

	@Override
	public boolean contains(V vertex) {
		return delegate.contains(vertex);
	}

	@Override
	public OptionalInt get(V source, V target) {
		return delegate.get(source, target);
	}

	@Override
	public int getOutDegree(V vertex) {
		return delegate.getOutDegree(vertex);
	}

	@Override
	public int getEdgeCount() {
		return delegate.getEdgeCount();
	}

	@Override
	public int getVertexCount() {
		return delegate.getVertexCount();
	}

	@Override
	public int totalWeight() {
		return delegate.totalWeight();
	}

	@Override
	public Iterable<V> vertices() {
		return delegate.vertices();
	}

	@Override
	public OptionalInt put(V source, V target, int edge) {
		return delegate.put(source, target, edge);
	}

	@Override
	public OptionalInt remove(V source, V target) {
		return delegate.remove(source, target);
	}

	@Override
	public boolean remove(V vertex) {
		return delegate.remove(vertex);
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		delegate.removeAll(vertices);
	}

	@Override
	public Digraph<V> reverse() {
		return delegate.reverse();
	}

	@Override
	public Digraph<V> subgraph(Set<V> vertices) {
		return delegate.subgraph(vertices);
	}

	@Override
	public boolean isAcyclic() {
		return delegate.isAcyclic();
	}

	@Override
	public Iterable<V> targets(V source) {
		return delegate.targets(source);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this.getClass() != obj.getClass()) {
			return false;
		}

		return delegate.equals(((DigraphAdapter<?>) obj).delegate);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}
}
