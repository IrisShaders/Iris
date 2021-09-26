package net.coderbot.iris.shadows.frustum.fallback;

import com.mojang.math.Matrix4f;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class BoxCullingFrustum extends Frustum {
	private final BoxCuller boxCuller;

	public BoxCullingFrustum(BoxCuller boxCuller) {
		super(new Matrix4f(), new Matrix4f());

		this.boxCuller = boxCuller;
	}

	@Override
	public void prepare(double cameraX, double cameraY, double cameraZ) {
		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// for Sodium
	// TODO: Better way to do this... Maybe we shouldn't be using a frustum for the box culling in the first place!
	public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ);
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	@Override
	public boolean isVisible(AABB aabb) {
		return !boxCuller.isCulled(aabb);
	}
}
