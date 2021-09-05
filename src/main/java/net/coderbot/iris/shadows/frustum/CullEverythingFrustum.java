package net.coderbot.iris.shadows.frustum;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class CullEverythingFrustum extends Frustum implements SodiumFrustumExt {
	public CullEverythingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	// for Sodium
	public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return false;
	}

	// For Immersive Portals
	// We return false here since isVisible is going to return false anyways.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(Box box) {
		return false;
	}

	@Override
	public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return RenderRegionVisibility.CULLED;
	}
}
