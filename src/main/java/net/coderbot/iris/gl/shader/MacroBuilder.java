// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import com.google.common.collect.ImmutableList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MacroBuilder {
	private static final String EMPTY_VALUE = "";

	private final Map<String, String> macros = new LinkedHashMap<>();

	public MacroBuilder() {
	}

	public MacroBuilder define(String name) {
		this.define(Objects.requireNonNull(name), EMPTY_VALUE);
		return this;
	}

	public MacroBuilder define(String name, String value) {
		String prev = this.macros.get(name);

		if (prev != null) {
			throw new IllegalArgumentException("Macro " + name + " is already defined with value " + prev);
		}

		this.macros.put(Objects.requireNonNull(name), Objects.requireNonNull(value));

		return this;
	}

	public MacroBuilder defineAll(List<String> names) {
		names.forEach(this::define);
		return this;
	}

	public MacroBuilder defineAll(Map<String, String> macros) {
		macros.forEach(this::define);
		return this;
	}

	public ImmutableList<String> build() {
		ImmutableList.Builder<String> builder = ImmutableList.builder();

		for (Map.Entry<String, String> entry : this.macros.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (value.length() <= 0) {
				builder.add("#define " + key);
			} else {
				builder.add("#define " + key + " " + value);
			}
		}

		return builder.build();
	}
}
