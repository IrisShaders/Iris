package net.irisshaders.iris.shadows.frustum;

import com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiShadowCullingFrustum;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

public class CullEverythingFrustum extends Frustum {
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
}
