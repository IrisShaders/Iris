package net.irisshaders.iris.helpers;

public record Tri<X, Y, Z>(X first, Y second, Z third) {


	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		//noinspection rawtypes
		if (!(obj instanceof Tri tri)) return false;
		return tri.first == this.first && tri.second == this.second && tri.third == this.third;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + ((third == null) ? 0 : third.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "First: " + first.toString() + " Second: " + second.toString() + " Third: " + third.toString();
	}
}
