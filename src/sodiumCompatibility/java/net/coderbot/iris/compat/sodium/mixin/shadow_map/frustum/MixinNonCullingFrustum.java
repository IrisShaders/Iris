package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NonCullingFrustum.class)
public class MixinNonCullingFrustum implements Frustum, FrustumAdapter {
	@Override
	public int testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return -2;
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
