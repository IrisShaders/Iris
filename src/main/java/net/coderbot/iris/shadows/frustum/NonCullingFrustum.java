package net.coderbot.iris.shadows.frustum;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class NonCullingFrustum extends Frustum {
	public NonCullingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	public boolean isVisible(Box box) {
		return true;
	}
}
