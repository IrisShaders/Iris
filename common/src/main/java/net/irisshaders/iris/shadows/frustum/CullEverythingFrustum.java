package net.irisshaders.iris.shadows.frustum;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class CullEverythingFrustum extends Frustum implements ViewportProvider, net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum {
	private final Vector3d position = new Vector3d();

	public CullEverythingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	// For Immersive Portals
	// We return false here since isVisible is going to return false anyways.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(AABB box) {
		return false;
	}

	@Override
	public void prepare(double d, double e, double f) {
		this.position.set(d, e, f);
	}

	@Override
	public Viewport sodium$createViewport() {
		return new Viewport(this, position);
	}

	@Override
	public boolean testAab(float v, float v1, float v2, float v3, float v4, float v5) {
		return false;
	}

	@Override
	public int intersectAab(float v, float v1, float v2, float v3, float v4, float v5) {
		return FrustumIntersection.OUTSIDE;
	}
}
