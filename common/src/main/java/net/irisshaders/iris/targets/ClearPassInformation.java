package net.irisshaders.iris.targets;

import org.joml.Vector4f;

public record ClearPassInformation(Vector4f color, int width, int height) {


	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ClearPassInformation information)) {
			return false;
		}

		return information.color.equals(this.color) && information.height == this.height && information.width == this.width;
	}
}
