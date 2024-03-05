package net.irisshaders.iris.helpers;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An absurdly simple class for storing pairs of strings because Java lacks pair / tuple types.
 */
public record StringPair(String key, String value) {
	public StringPair(@NotNull String key, @NotNull String value) {
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}

	@Override
	@NotNull
	public String key() {
		return key;
	}

	@Override
	@NotNull
	public String value() {
		return value;
	}
}
