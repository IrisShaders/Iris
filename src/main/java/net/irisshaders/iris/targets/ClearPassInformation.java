package net.irisshaders.iris.targets;

import org.joml.Vector4f;

public class ClearPassInformation {
	private final Vector4f color;
	private final int width;
	private final int height;

	public ClearPassInformation(Vector4f vector4f, int width, int height) {
		this.color = vector4f;
		this.width = width;
		this.height = height;
	}

	public Vector4f getColor() {
		return color;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClearPassInformation information)) {
			return false;
		}

		return information.color.equals(this.color) && information.height == this.height && information.width == this.width;
	}
}
