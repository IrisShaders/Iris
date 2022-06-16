package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoxCullingFrustum.class)
public class MixinBoxCullingFrustum implements Frustum, FrustumAdapter {
	@Shadow(remap = false)
	@Final
	private BoxCuller boxCuller;

	// TODO: Better way to do this... Maybe we shouldn't be using a frustum for the box culling in the first place!
	@Override
	public int testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// TODO: Frustum.INSIDE
		return this.boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ) ? -3 : -1;
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
