package net.coderbot.iris.shadows.frustum.fallback;

import com.mojang.math.Matrix4f;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import net.coderbot.iris.shadows.frustum.SodiumFrustumExt;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;

public class NonCullingFrustum extends Frustum implements SodiumFrustumExt {
	public NonCullingFrustum() {
		super(new Matrix4f(), new Matrix4f());
	}

	// for Sodium
	public boolean fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return true;
	}

	// For Immersive Portals
	// NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
	//     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
	//     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
	public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return false;
	}

	public boolean isVisible(AABB box) {
		return true;
	}

	@Override
	public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return RenderRegionVisibility.FULLY_VISIBLE;
	}
}
