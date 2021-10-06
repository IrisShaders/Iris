package net.irisshaders.iris.shaderpack;

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
}
