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
package de.odysseus.ithaka.digraph.io.tgf;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import de.odysseus.ithaka.digraph.Digraph;

public class TgfExporter {
	private final String newline;

	public TgfExporter() {
		this(System.getProperty("line.separator"));
	}

	public TgfExporter(String newline) {
		this.newline = newline;
	}

	public <V> void export(
			TgfLabelProvider<V> provider,
			Digraph<V> digraph,
			Writer writer) throws IOException {
		Map<V, Integer> index = new HashMap<>();
		int n = 0;

		for (V vertex : digraph.vertices()) {
			n += 1;
			index.put(vertex, n);
			writer.write(String.valueOf(n));
			String label = provider.getVertexLabel(vertex);
			if (label != null) {
				writer.write(' ');
				writer.write(label);
			}
			writer.write(newline);
		}

		writer.write('#');
		writer.write(newline);

		for (V source : digraph.vertices()) {
			for (V target : digraph.targets(source)) {
				writer.write(String.valueOf(index.get(source)));
				writer.write(' ');
				writer.write(String.valueOf(index.get(target)));
				String label = provider.getEdgeLabel(digraph.get(source, target).getAsInt());
				if (label != null) {
					writer.write(' ');
					writer.write(label);
				}
				writer.write(newline);
			}
		}

		writer.flush();
	}
}
