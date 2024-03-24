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
import java.util.OptionalInt;

/**
 * Doubled de.odysseus.ithaka.digraph implementation.
 *
 * @param <V> vertex type
 */
public class DoubledDigraphAdapter<V> extends DigraphAdapter<V> implements DoubledDigraph<V> {
	private final DoubledDigraphAdapter<V> reverse;
	private final DigraphFactory<? extends Digraph<V>> factory;

	public DoubledDigraphAdapter() {
		this(MapDigraph.getDefaultDigraphFactory());
	}

	public DoubledDigraphAdapter(DigraphFactory<? extends Digraph<V>> factory) {
		super(factory.create());
		this.factory = factory;
		this.reverse = createReverse();
	}

	protected DoubledDigraphAdapter(DigraphFactory<? extends Digraph<V>> factory, DoubledDigraphAdapter<V> reverse) {
		super(factory.create());
		this.factory = factory;
		this.reverse = reverse;
	}

	/**
	 * Factory creating <code>DoubledDigraph</code>.
	 *
	 * @param factory delegate factory
	 * @return doubled de.odysseus.ithaka.digraph factory
	 */
	public static <V> DigraphFactory<DoubledDigraphAdapter<V>> getAdapterFactory(final DigraphFactory<? extends Digraph<V>> factory) {
		return () -> new DoubledDigraphAdapter<>(factory);
	}

	protected DoubledDigraphAdapter<V> createReverse() {
		return new DoubledDigraphAdapter<>(factory, this);
	}

	protected DigraphFactory<? extends DoubledDigraph<V>> getDigraphFactory() {
		return getAdapterFactory(factory);
	}

	protected DigraphFactory<? extends Digraph<V>> getDelegateFactory() {
		return factory;
	}

	@Override
	public int getInDegree(V vertex) {
		return reverse.getOutDegree(vertex);
	}

	@Override
	public Iterable<V> sources(V target) {
		return reverse.targets(target);
	}

	@Override
	public final boolean add(V vertex) {
		reverse.add0(vertex);
		return add0(vertex);
	}

	protected boolean add0(V vertex) {
		return super.add(vertex);
	}

	@Override
	public final boolean remove(V vertex) {
		reverse.remove0(vertex);
		return remove0(vertex);
	}

	protected boolean remove0(V vertex) {
		return super.remove(vertex);
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		reverse.removeAll0(vertices);
		removeAll0(vertices);
	}

	protected void removeAll0(Collection<V> vertices) {
		super.removeAll(vertices);
	}

	/**
	 * Make sure the reverse de.odysseus.ithaka.digraph is kept in sync if <code>Iterator.remove()</code> is called.
	 */
	@Override
	public Iterable<V> vertices() {
		final Iterator<V> delegate = super.vertices().iterator();
		if (!delegate.hasNext()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					V vertex;

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return vertex = delegate.next();
					}

					@Override
					public void remove() {
						delegate.remove();
						reverse.remove0(vertex);
					}
				};
			}

			@Override
			public String toString() {
				return DoubledDigraphAdapter.super.vertices().toString();
			}
		};
	}

	/**
	 * Make sure the reverse de.odysseus.ithaka.digraph is kept in sync if <code>Iterator.remove()</code> is called.
	 */
	@Override
	public Iterable<V> targets(final V source) {
		final Iterator<V> delegate = super.targets(source).iterator();
		if (!delegate.hasNext()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					V target;

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return target = delegate.next();
					}

					@Override
					public void remove() {
						delegate.remove();
						reverse.remove0(target, source);
					}
				};
			}

			@Override
			public String toString() {
				return DoubledDigraphAdapter.super.targets(source).toString();
			}
		};
	}

	@Override
	public final OptionalInt put(V source, V target, int edge) {
		reverse.put0(target, source, edge);
		return put0(source, target, edge);
	}

	protected OptionalInt put0(V source, V target, int edge) {
		return super.put(source, target, edge);
	}

	@Override
	public final OptionalInt remove(V source, V target) {
		reverse.remove0(target, source);
		return remove0(source, target);
	}

	protected OptionalInt remove0(V source, V target) {
		return super.remove(source, target);
	}

	@Override
	public final DoubledDigraphAdapter<V> reverse() {
		return reverse;
	}
}
