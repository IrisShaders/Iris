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

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}

	@Override
	public int intersectBox(float v, float v1, float v2, float v3, float v4, float v5, int i) {
		return this.boxCuller.isCulled(v, v1, v2, v3, v4, v5) ? -1 : 1;
	}
}
