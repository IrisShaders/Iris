package net.irisshaders.iris.shaderpack.option;

public final class BooleanOption extends BaseOption {
	private final boolean defaultValue;

	public BooleanOption(OptionType type, String name, String comment, boolean defaultValue) {
		super(type, name, comment);

		this.defaultValue = defaultValue;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		return "BooleanDefineOption{" +
			"name=" + getName() +
			", comment=" + getComment() +
			", defaultValue=" + defaultValue +
			'}';
	}
}
