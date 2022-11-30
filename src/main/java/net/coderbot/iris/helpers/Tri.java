package net.coderbot.iris.helpers;

public class Tri<X, Y, Z> {
	private final X first;
	private final Y second;
	private final Z third;

	public Tri(X first, Y second, Z third) {
		this.first = first;
		this.second = second;
		this.third = third;
	}

	public X getFirst() {
		return first;
	}

	public Y getSecond() {
		return second;
	}

	public Z getThird() {
		return third;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Tri)) return false;
		Tri tri = (Tri) obj;
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
