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

import it.unimi.dsi.fastutil.objects.Object2IntAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;

/**
 * Map-based directed graph implementation.
 *
 * @param <V> vertex type
 */
public class MapDigraph<V> implements Digraph<V> {
	private static final int INVALID_WEIGHT = Integer.MIN_VALUE;
	private final VertexMapFactory<V> vertexMapFactory;
	private final EdgeMapFactory<V> edgeMapFactory;
	private final Map<V, Object2IntMap<V>> vertexMap;
	private int edgeCount;

	/**
	 * Create de.odysseus.ithaka.digraph.
	 * {@link LinkedHashMap}s will be used as vertex/edge maps.
	 * Vertices and edge targets will be iterated in no particular order.
	 */
	public MapDigraph() {
		this(null);
	}

	/**
	 * Create de.odysseus.ithaka.digraph.
	 * If a vertex comparator is given, {@link TreeMap}s will be used as vertex/edge maps.
	 * Vertices and edge targets will be iterated in the order given by the comparator.
	 *
	 * @param comparator vertex comparator (may be <code>null</code>)
	 */
	public MapDigraph(final Comparator<? super V> comparator) {
		this(comparator, comparator);
	}

	/**
	 * Create de.odysseus.ithaka.digraph.
	 * If a vertex comparator is given, {@link TreeMap}s will be used as vertex maps
	 * and vertices will be iterated in the order given by the vertex comparator.
	 * If an edge comparator is given, {@link TreeMap}s will be used as edge maps
	 * and edge targets will be iterated in the order given by the edge comparator.
	 */
	public MapDigraph(final Comparator<? super V> vertexComparator, final Comparator<? super V> edgeComparator) {
		this(MapDigraph.getDefaultVertexMapFactory(vertexComparator), MapDigraph.getDefaultEdgeMapFactory(edgeComparator));
	}

	/**
	 * Create de.odysseus.ithaka.digraph.
	 *
	 * @param vertexMapFactory factory to create vertex --> edge-map maps
	 * @param edgeMapFactory   factory to create edge-target --> edge-value maps
	 */
	public MapDigraph(VertexMapFactory<V> vertexMapFactory, EdgeMapFactory<V> edgeMapFactory) {
		this.vertexMapFactory = vertexMapFactory;
		this.edgeMapFactory = edgeMapFactory;

		vertexMap = vertexMapFactory.create();
	}

	/**
	 * Factory creating default <code>MapDigraph</code>.
	 *
	 * @return map de.odysseus.ithaka.digraph factory
	 */
	public static <V> DigraphFactory<MapDigraph<V>> getDefaultDigraphFactory() {
		return getMapDigraphFactory(MapDigraph.getDefaultVertexMapFactory(null), MapDigraph.getDefaultEdgeMapFactory(null));
	}

	/**
	 * Factory creating <code>MapDigraph</code>.
	 *
	 * @param vertexMapFactory factory to create vertex --> edge-map maps
	 * @param edgeMapFactory   factory to create edge-target --> edge-value maps
	 * @return map de.odysseus.ithaka.digraph factory
	 */
	public static <V> DigraphFactory<MapDigraph<V>> getMapDigraphFactory(
		final VertexMapFactory<V> vertexMapFactory,
		final EdgeMapFactory<V> edgeMapFactory) {
		return () -> new MapDigraph<>(vertexMapFactory, edgeMapFactory);
	}

	private static <V> VertexMapFactory<V> getDefaultVertexMapFactory(final Comparator<? super V> comparator) {
		return new VertexMapFactory<V>() {
			@Override
			public Map<V, Object2IntMap<V>> create() {
				if (comparator == null) {
					return new LinkedHashMap<>(16);
				} else {
					return new TreeMap<>(comparator);
				}
			}
		};
	}

	private static <V> EdgeMapFactory<V> getDefaultEdgeMapFactory(final Comparator<? super V> comparator) {
		return new EdgeMapFactory<V>() {
			@Override
			public Object2IntMap<V> create(V ignore) {
				Object2IntMap<V> map;

				if (comparator == null) {
					map = new Object2IntLinkedOpenHashMap<>(16);
				} else {
					map = new Object2IntAVLTreeMap<>(comparator);
				}

				map.defaultReturnValue(INVALID_WEIGHT);

				return map;
			}
		};
	}

	private static <V> Object2IntMap<V> createEmptyMap() {
		return Object2IntMaps.emptyMap();
	}

	@Override
	public boolean add(V vertex) {
		if (!vertexMap.containsKey(vertex)) {
			vertexMap.put(vertex, createEmptyMap());
			return true;
		}

		return false;
	}

	@Override
	public OptionalInt put(V source, V target, int weight) {
		if (weight == INVALID_WEIGHT) {
			throw new IllegalArgumentException("Invalid weight " + weight);
		}

		Object2IntMap<V> edgeMap = vertexMap.get(source);

		if (edgeMap == null || edgeMap.isEmpty()) {
			vertexMap.put(source, edgeMap = edgeMapFactory.create(source));
		}

		int previousInt = edgeMap.put(target, weight);
		OptionalInt previous;

		if (previousInt != INVALID_WEIGHT) {
			previous = OptionalInt.of(previousInt);
		} else {
			previous = OptionalInt.empty();
			add(target);
			edgeCount++;
		}

		return previous;
	}

	@Override
	public OptionalInt get(V source, V target) {
		Object2IntMap<V> edgeMap = vertexMap.get(source);

		if (edgeMap == null || edgeMap.isEmpty()) {
			return OptionalInt.empty();
		}

		int result = edgeMap.getInt(target);

		return result == INVALID_WEIGHT ? OptionalInt.empty() : OptionalInt.of(result);
	}

	@Override
	public OptionalInt remove(V source, V target) {
		Object2IntMap<V> edgeMap = vertexMap.get(source);
		if (edgeMap == null || !edgeMap.containsKey(target)) {
			return OptionalInt.empty();
		}
		int result = edgeMap.removeInt(target);
		edgeCount--;
		if (edgeMap.isEmpty()) {
			vertexMap.put(source, createEmptyMap());
		}
		return result == INVALID_WEIGHT ? OptionalInt.empty() : OptionalInt.of(result);
	}

	@Override
	public boolean remove(V vertex) {
		Object2IntMap<V> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return false;
		}
		edgeCount -= edgeMap.size();
		vertexMap.remove(vertex);
		for (V source : vertexMap.keySet()) {
			remove(source, vertex);
		}
		return true;
	}

	@Override
	public void removeAll(Collection<V> vertices) {
		for (V vertex : vertices) {
			Object2IntMap<V> edgeMap = vertexMap.get(vertex);
			if (edgeMap != null) {
				edgeCount -= edgeMap.size();
				vertexMap.remove(vertex);
			}
		}
		for (V source : vertexMap.keySet()) {
			Object2IntMap<V> edgeMap = vertexMap.get(source);
			Iterator<V> iterator = edgeMap.keySet().iterator();
			while (iterator.hasNext()) {
				if (vertices.contains(iterator.next())) {
					iterator.remove();
					edgeCount--;
				}
			}
			if (edgeMap.isEmpty()) {
				vertexMap.put(source, createEmptyMap());
			}
		}
	}

	@Override
	public boolean contains(V source, V target) {
		Object2IntMap<V> edgeMap = vertexMap.get(source);

		if (edgeMap == null || edgeMap.isEmpty()) {
			return false;
		}

		return edgeMap.containsKey(target);
	}

	@Override
	public boolean contains(V vertex) {
		return vertexMap.containsKey(vertex);
	}

	@Override
	public Iterable<V> vertices() {
		if (vertexMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					private final Iterator<V> delegate = vertexMap.keySet().iterator();
					V vertex = null;

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
						Object2IntMap<V> edgeMap = vertexMap.get(vertex);
						delegate.remove();
						edgeCount -= edgeMap.size();
						for (V source : vertexMap.keySet()) {
							MapDigraph.this.remove(source, vertex);
						}
					}
				};
			}

			@Override
			public String toString() {
				return vertexMap.keySet().toString();
			}
		};
	}

	@Override
	public Iterable<V> targets(final V source) {
		final Object2IntMap<V> edgeMap = vertexMap.get(source);
		if (edgeMap == null || edgeMap.isEmpty()) {
			return Collections.emptySet();
		}
		return new Iterable<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					private final Iterator<V> delegate = edgeMap.keySet().iterator();

					@Override
					public boolean hasNext() {
						return delegate.hasNext();
					}

					@Override
					public V next() {
						return delegate.next();
					}

					@Override
					public void remove() {
						delegate.remove();
						edgeCount--;
						if (edgeMap.isEmpty()) {
							vertexMap.put(source, createEmptyMap());
						}
					}
				};
			}

			@Override
			public String toString() {
				return edgeMap.keySet().toString();
			}
		};
	}

	@Override
	public int getVertexCount() {
		return vertexMap.size();
	}

	@Override
	public int totalWeight() {
		int weight = 0;

		for (V source : vertices()) {
			for (V target : targets(source)) {
				weight += get(source, target).getAsInt();
			}
		}

		return weight;
	}

	@Override
	public int getOutDegree(V vertex) {
		Object2IntMap<V> edgeMap = vertexMap.get(vertex);
		if (edgeMap == null) {
			return 0;
		}
		return edgeMap.size();
	}

	@Override
	public int getEdgeCount() {
		return edgeCount;
	}

	public DigraphFactory<? extends MapDigraph<V>> getDigraphFactory() {
		return () -> new MapDigraph<>(vertexMapFactory, edgeMapFactory);
	}

	@Override
	public MapDigraph<V> reverse() {
		return Digraphs.<V, MapDigraph<V>>reverse(this, getDigraphFactory());
	}

	@Override
	public MapDigraph<V> subgraph(Set<V> vertices) {
		return Digraphs.<V, MapDigraph<V>>subgraph(this, vertices, getDigraphFactory());
	}

	@Override
	public boolean isAcyclic() {
		return Digraphs.isAcyclic(this);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getClass().getName().substring(getClass().getName().lastIndexOf('.') + 1));
		b.append("(");
		Iterator<V> vertices = vertices().iterator();
		while (vertices.hasNext()) {
			V v = vertices.next();
			b.append(v);
			b.append(targets(v));
			if (vertices.hasNext()) {
				b.append(", ");
				if (b.length() > 1000) {
					b.append("...");
					break;
				}
			}
		}
		b.append(")");
		return b.toString();
	}

	/**
	 * Vertex map factory (vertex to edge map).
	 */
	public interface VertexMapFactory<V> {
		Map<V, Object2IntMap<V>> create();
	}

	/**
	 * Edge map factory (edge target to edge value).
	 */
	public interface EdgeMapFactory<V> {
		Object2IntMap<V> create(V source);
	}
}
