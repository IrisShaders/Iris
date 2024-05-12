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

import de.odysseus.ithaka.digraph.Digraph;

public interface DotProvider<V, G extends Digraph<? extends V>> {
	Iterable<DotAttribute> getDefaultGraphAttributes(G digraph);

	Iterable<DotAttribute> getDefaultNodeAttributes(G digraph);

	Iterable<DotAttribute> getDefaultEdgeAttributes(G digraph);

	String getNodeId(V vertex);

	Iterable<DotAttribute> getNodeAttributes(V vertex);

	Iterable<DotAttribute> getEdgeAttributes(V source, V target, int edgeWeight);

	Iterable<DotAttribute> getSubgraphAttributes(G subgraph, V vertex);
}
