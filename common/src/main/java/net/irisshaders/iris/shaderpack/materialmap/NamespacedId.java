package net.irisshaders.iris.shaderpack.materialmap;

public record NamespacedId(String namespace, String name) {
	public NamespacedId(String combined) {
		int colonIdx = combined.indexOf(':');
		String namespace, name;

		if (colonIdx == -1) {
			namespace = "minecraft";
			name = combined;
		} else {
			namespace = combined.substring(0, colonIdx);
			name = combined.substring(colonIdx + 1);
		}

		this(namespace, name);
	}

	@Override
	public String toString() {
		return namespace + ":" + name;
	}
}
