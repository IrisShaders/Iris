package net.coderbot.iris.shadows.frustum.fallback;

import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegionVisibility;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.SodiumFrustumExt;
import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;

public class BoxCullingFrustum extends Frustum implements SodiumFrustumExt {
	private final BoxCuller boxCuller;

	public BoxCullingFrustum(BoxCuller boxCuller) {
		super(new Matrix4f(), new Matrix4f());

		this.boxCuller = boxCuller;
	}

	public void setPosition(double cameraX, double cameraY, double cameraZ) {
		boxCuller.setPosition(cameraX, cameraY, cameraZ);
	}

	// for Sodium
	// TODO: Better way to do this... Maybe we shouldn't be using a frustum for the box culling in the first place!
	public boolean preAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return !boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ);
	}

	public boolean isVisible(Box box) {
		return !boxCuller.isCulled(box);
	}

	@Override
	public RenderRegionVisibility aabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// TODO: FULLY_VISIBLE?
		return boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ) ? RenderRegionVisibility.CULLED : RenderRegionVisibility.VISIBLE;
	}
}
