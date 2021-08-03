package net.coderbot.iris.shadows.frustum;

import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class NonCullingFrustum extends Frustum {
	public NonCullingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	public boolean isVisible(AABB box) {
		return true;
	}
}
