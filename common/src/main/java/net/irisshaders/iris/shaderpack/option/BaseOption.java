package net.irisshaders.iris.shaderpack.option;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class BaseOption {
	@NotNull
	private final OptionType type;
	@NotNull
	private final String name;
	@Nullable
	private final String comment;

	BaseOption(@NotNull OptionType type, @NotNull String name, @Nullable String comment) {
		this.type = type;
		this.name = name;

		if (comment == null || comment.isEmpty()) {
			this.comment = null;
		} else {
			this.comment = comment;
		}
	}

	@NotNull
	public OptionType getType() {
		return type;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public Optional<String> getComment() {
		return Optional.ofNullable(comment);
	}
}
