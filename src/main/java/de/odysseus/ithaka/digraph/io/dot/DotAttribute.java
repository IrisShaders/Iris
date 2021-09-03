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

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;

public class DotAttribute {
	private static boolean isIdentifier(String value) {
		if (!Character.isJavaIdentifierStart(value.charAt(0))) {
			return false;
		}
		for (char c : value.substring(1).toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				return false;
			}
		}
		return true;
	}

	private final String name;
	private final String value;
	private final boolean quotes;

	public DotAttribute(String name, String value) {
		this.name = name;
		this.value = value;
		this.quotes = !isIdentifier(value);
	}

	public DotAttribute(String name, Number value) {
		this.name = name;
		this.value = value.toString();
		this.quotes = false;
	}

	public DotAttribute(String name, boolean value) {
		this.name = name;
		this.value = String.valueOf(value);
		this.quotes = false;
	}

	public DotAttribute(String name, Color value) {
		this.name = name;
		this.value = String.format("#%6X", value.getRGB() & 0x00FFFFFF);
		this.quotes = true;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void write(Writer writer) throws IOException {
		writer.write(name);
		writer.write('=');
		if (quotes) {
			writer.write('"');
		}
		writer.write(value);
		if (quotes) {
			writer.write('"');
		}
	}
}
