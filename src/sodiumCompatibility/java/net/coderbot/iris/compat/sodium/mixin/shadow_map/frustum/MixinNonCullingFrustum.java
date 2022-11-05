package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NonCullingFrustum.class)
public class MixinNonCullingFrustum implements Frustum, FrustumAdapter {
	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}

	@Override
	public int intersectBox(float v, float v1, float v2, float v3, float v4, float v5, int i) {
		return 1;
	}
}
