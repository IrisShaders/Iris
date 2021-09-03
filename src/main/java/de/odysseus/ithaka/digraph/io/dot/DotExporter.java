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
package de.odysseus.ithaka.digraph.io.dot;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.DigraphProvider;

public class DotExporter {
	private static class Cluster<V, G extends Digraph<V>> {
		String id;
		G subgraph;
		V sample;
		DotAttribute tail;
		DotAttribute head;

		public Cluster(String id, G subgraph) {
			this.id = id;
			this.subgraph = subgraph;
			this.sample = subgraph.vertices().iterator().next();

			this.head = new DotAttribute("lhead", id);
			this.tail = new DotAttribute("ltail", id);
		}
	}

	private final String indent;
	private final String lineSpeparator;

	public DotExporter() {
		this("  ", System.getProperty("line.separator"));
	}

	public DotExporter(String indent, String newline) {
		this.indent = indent;
		this.lineSpeparator = newline;
	}

	private void indent(Writer writer, int level) throws IOException {
		for (int i = 0; i < level; i++) {
			writer.write(indent);
		}
	}

	private void writeAttributes(Writer writer, Iterator<DotAttribute> iterator) throws IOException {
		if (iterator.hasNext()) {
			boolean first = true;
			while (iterator.hasNext()) {
				if (first) {
					writer.write('[');
					first = false;
				} else {
					writer.write(", ");
				}
				iterator.next().write(writer);
			}
			writer.write(']');
		}
	}

	private void writeDefaultAttributes(Writer writer, int level, String name, Iterable<DotAttribute> attributes) throws IOException {
		if (attributes != null) {
			indent(writer, level);
			Iterator<DotAttribute> iterator = attributes.iterator();
			if (iterator.hasNext()) {
				writer.write(name);
				writeAttributes(writer, iterator);
			}
			writer.write(";");
			writer.write(lineSpeparator);
		}
	}

	private <V> void writeNode(Writer writer, int level, V vertex, DotProvider<V, ?> provider) throws IOException {
		indent(writer, level);
		writer.write(provider.getNodeId(vertex));
		Iterable<DotAttribute> attributes = provider.getNodeAttributes(vertex);
		if (attributes != null) {
			writeAttributes(writer, attributes.iterator());
		}
		writer.write(";");
		writer.write(lineSpeparator);
	}

	private <V> void writeEdge(Writer writer, int level, V source, V target, int edgeWeight, DotProvider<V, ?> provider,
							   Cluster<V, ?> sourceCluster, Cluster<V, ?> targetCluster) throws IOException {
		indent(writer, level);
		writer.write(provider.getNodeId(sourceCluster == null ? source : sourceCluster.sample));
		writer.write(" -> ");
		writer.write(provider.getNodeId(targetCluster == null ? target : targetCluster.sample));
		Iterable<DotAttribute> attributes = provider.getEdgeAttributes(source, target, edgeWeight);
		if (sourceCluster == null && targetCluster == null) {
			if (attributes != null) {
				writeAttributes(writer, attributes.iterator());
			}
		} else {
			List<DotAttribute> attributeList = new ArrayList<>();
			if (sourceCluster != null) {
				attributeList.add(sourceCluster.tail);
			}
			if (targetCluster != null) {
				attributeList.add(targetCluster.head);
			}
			if (attributes != null) {
				for (DotAttribute attribute : attributes) {
					attributeList.add(attribute);
				}
			}
			writeAttributes(writer, attributeList.iterator());
		}
		writer.write(";");
		writer.write(lineSpeparator);
	}

	private <V, G extends Digraph<V>> Map<V, Cluster<V, G>> createClusters(
			G digraph,
			DotProvider<V, G> provider,
			DigraphProvider<? super V, G> subgraphs) {
		Map<V, Cluster<V, G>> clusters = new HashMap<>();
		if (subgraphs != null) {
			for (V vertex : digraph.vertices()) {
				G subgraph = subgraphs.get(vertex);
				if (subgraph != null && subgraph.getVertexCount() > 0) {
					clusters.put(vertex, new Cluster<>("cluster_" + provider.getNodeId(vertex), subgraph));
				}
			}
		}
		return clusters;
	}

	public <V, G extends Digraph<V>> void export(
			DotProvider<V, G> provider,
			G digraph,
			DigraphProvider<V, G> subgraphs,
			Writer writer) throws IOException {

		writer.write("digraph G {");
		writer.write(lineSpeparator);

		Map<V, Cluster<V, G>> clusters = createClusters(digraph, provider, subgraphs);
		if (!clusters.isEmpty()) {
			indent(writer, 1);
			writer.write("compound=true;");
			writer.write(lineSpeparator);
		}

		writeDefaultAttributes(writer, 1, "graph", provider.getDefaultGraphAttributes(digraph));
		writeDefaultAttributes(writer, 1, "node", provider.getDefaultNodeAttributes(digraph));
		writeDefaultAttributes(writer, 1, "edge", provider.getDefaultEdgeAttributes(digraph));

		writeNodesAndEdges(writer, 1, provider, digraph, clusters, subgraphs);

		writer.write("}");
		writer.write(lineSpeparator);

		writer.flush();
	}

	private <V, G extends Digraph<V>> void writeNodesAndEdges(
			Writer writer,
			int level,
			DotProvider<V, G> provider,
			G digraph,
			Map<V, Cluster<V, G>> clusters,
			DigraphProvider<V, G> subgraphs) throws IOException {
		for (V vertex : digraph.vertices()) {
			if (clusters.containsKey(vertex)) {
				writeCluster(writer, level, provider, vertex, clusters.get(vertex), subgraphs);
			} else {
				writeNode(writer, level, vertex, provider);
			}
		}
		for (V source : digraph.vertices()) {
			for (V target : digraph.targets(source)) {
				writeEdge(writer, level, source, target, digraph.get(source, target).getAsInt(), provider,
						clusters.get(source), clusters.get(target));
			}
		}
	}

	private <V, G extends Digraph<V>> void writeCluster(
			Writer writer,
			int level,
			DotProvider<V, G> provider,
			V subgraphVertex,
			Cluster<V, G> cluster,
			DigraphProvider<V, G> subgraphs) throws IOException {

		indent(writer, level);
		writer.write("subgraph ");
		writer.write(cluster.id);
		writer.write(" {");
		writer.write(lineSpeparator);

		writeDefaultAttributes(writer, level + 1, "graph", provider.getSubgraphAttributes(cluster.subgraph, subgraphVertex));

		Map<V, Cluster<V, G>> subclusters = createClusters(cluster.subgraph, provider, subgraphs);
		writeNodesAndEdges(writer, level + 1, provider, cluster.subgraph, subclusters, subgraphs);

		indent(writer, level);
		writer.write("}");
		writer.write(lineSpeparator);
	}
}
