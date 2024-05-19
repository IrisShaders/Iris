package net.irisshaders.iris.gl.uniform;

public enum UniformType {
	INT(4, 4),
	FLOAT(4, 4),
	MAT3(36, 16),
	MAT4(64, 16),
	VEC2(8, 8),
	VEC2I(8, 8),
	VEC3(12, 16),
	VEC3I(12, 16),
	VEC4(16, 16),
	VEC4I(16, 16);

	private final int size;
	private final int alignment;

	UniformType(int size, int alignment) {
		this.size = size;
		this.alignment = alignment;
	}

	public int getSize() {
		return size;
	}

	public int getAlignment() {
		return alignment;
	}
}
