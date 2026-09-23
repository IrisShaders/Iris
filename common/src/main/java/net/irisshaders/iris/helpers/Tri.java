package net.irisshaders.iris.helpers;

import java.util.Objects;

public record Tri<X, Y, Z>(X first, Y second, Z third) {


	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		//noinspection rawtypes
		if (!(obj instanceof Tri(Object first1, Object second1, Object third1)))
			return false;
		return Objects.equals(first1, this.first) && Objects.equals(second1, this.second) && Objects.equals(third1, this.third);
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
