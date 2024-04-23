package net.irisshaders.iris.shaderpack.materialmap;

import java.util.Objects;

public class NamespacedId {
	private final String namespace;
	private final String name;

	public NamespacedId(String combined) {
		int colonIdx = combined.indexOf(':');

		if (colonIdx == -1) {
			namespace = "minecraft";
			name = combined;
		} else {
			namespace = combined.substring(0, colonIdx);
			name = combined.substring(colonIdx + 1);
		}
	}

	public NamespacedId(String namespace, String name) {
		this.namespace = Objects.requireNonNull(namespace);
		this.name = Objects.requireNonNull(name);
	}

	public String getNamespace() {
		return namespace;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		NamespacedId that = (NamespacedId) o;

		return namespace.equals(that.namespace) && name.equals(that.name);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "NamespacedId{" +
			"namespace='" + namespace + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
