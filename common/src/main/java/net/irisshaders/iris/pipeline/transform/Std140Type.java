package net.irisshaders.iris.pipeline.transform;

import java.util.Collections;
import java.util.List;

public record Std140Type(String glsl, int scalarBytes, int columns, int rows, List<Integer> dimensions) {
	public Std140Type {
		dimensions = List.copyOf(dimensions);

		if ((scalarBytes != 4 && scalarBytes != 8) || columns < 1 || columns > 4 || rows < 1 || rows > 4 || dimensions.stream()
			.anyMatch(n -> n <= 0)) {
			throw new IllegalArgumentException("invalid uniform type: " + glsl);
		}
	}

	public static int align(int value, int alignment) {
		return Math.multiplyExact(Math.floorDiv(Math.addExact(value, alignment - 1), alignment), alignment);
	}

	public int alignment() {
		int vectorAlignment = this.scalarBytes * (this.rows == 3 ? 4 : this.rows);

		return this.columns > 1 || !this.dimensions.isEmpty() ? align(vectorAlignment, 16) : vectorAlignment;
	}

	public int matrixStride() {
		return this.columns > 1 ? align(this.scalarBytes * (this.rows == 3 ? 4 : this.rows), 16) : 0;
	}

	public int size() {
		int size = this.columns > 1 ? Math.multiplyExact(this.columns, this.matrixStride()) : this.scalarBytes * this.rows;

		for (int i = this.dimensions.size() - 1; i >= 0; i--) {
			size = Math.multiplyExact(align(size, this.alignment()), this.dimensions.get(i));
		}

		return size;
	}

	public String declaration(String name) {
		StringBuilder result = new StringBuilder(this.glsl).append(' ').append(name);

		for (int dimension : this.dimensions) {
			result.append('[').append(dimension).append(']');
		}

		return result.append(';').toString();
	}

	public String zeroConstant() {
		String value = this.glsl + "(0)";
		String suffix = "";

		for (int i = this.dimensions.size() - 1; i >= 0; i--) {
			int count = this.dimensions.get(i);
			suffix = "[" + count + "]" + suffix;
			value = this.glsl + suffix + "(" + String.join(",", Collections.nCopies(count, value)) + ")";
		}

		return value;
	}
}
