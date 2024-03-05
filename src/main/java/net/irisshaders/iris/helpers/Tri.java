package net.irisshaders.iris.helpers;

public record Tri<X, Y, Z>(X first, Y second, Z third) {


	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Tri tri)) return false;
		return tri.first == this.first && tri.second == this.second && tri.third == this.third;
	}

	@Override
	public String toString() {
		return "First: " + first.toString() + " Second: " + second.toString() + " Third: " + third.toString();
	}
}
