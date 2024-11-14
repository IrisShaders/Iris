package net.irisshaders.iris.shadows.frustum.fallback;

import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.render.viewport.ViewportProvider;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class BoxCullingFrustum extends Frustum implements net.caffeinemc.mods.sodium.client.render.viewport.frustum.Frustum, ViewportProvider {
	private final BoxCuller boxCuller;
	private final Vector3d position = new Vector3d();

	public BoxCullingFrustum(BoxCuller boxCuller) {
		super(new Matrix4f(), new Matrix4f());

		this.boxCuller = boxCuller;
	}

	public void prepare(double cameraX, double cameraY, double cameraZ) {
		this.position.set(cameraX, cameraY, cameraZ);
		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(AABB box) {
		return !boxCuller.isCulled(box);
	}

	@Override
	public Viewport sodium$createViewport() {
		return new Viewport(this, position);
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public int intersectAab(float v, float v1, float v2, float v3, float v4, float v5) {
		return this.boxCuller.isCulledSodium(v, v1, v2, v3, v4, v5) ? FrustumIntersection.OUTSIDE : FrustumIntersection.INTERSECT;
	}
}
