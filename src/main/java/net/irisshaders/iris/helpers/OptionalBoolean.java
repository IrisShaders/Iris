package net.irisshaders.iris.helpers;

import java.util.function.BooleanSupplier;

public enum OptionalBoolean {
	DEFAULT,
	FALSE,
	TRUE;

	public boolean orElse(boolean defaultValue) {
		if (this == DEFAULT) {
			return defaultValue;
		}

		return this == TRUE;
	}

	public boolean orElseGet(BooleanSupplier defaultValue) {
		if (this == DEFAULT) {
			return defaultValue.getAsBoolean();
		}

		return this == TRUE;
	}
}
