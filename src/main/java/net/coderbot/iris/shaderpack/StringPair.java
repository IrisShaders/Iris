package net.coderbot.iris.shaderpack;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An absurdly simple class for storing pairs of strings because Java lacks pair / tuple types.
 */
public class StringPair {
	private final String key;
	private final String value;

	public StringPair(@NotNull String key, @NotNull String value) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}

	@NotNull
	public String getKey() {
		return key;
	}

	@NotNull
	public String getValue() {
		return value;
	}
}
