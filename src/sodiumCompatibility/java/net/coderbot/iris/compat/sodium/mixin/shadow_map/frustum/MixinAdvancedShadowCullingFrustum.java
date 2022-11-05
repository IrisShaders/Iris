package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import net.caffeinemc.sodium.interop.vanilla.math.frustum.Frustum;
import net.caffeinemc.sodium.interop.vanilla.math.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancedShadowCullingFrustum.class)
public abstract class MixinAdvancedShadowCullingFrustum implements Frustum, FrustumAdapter {
	@Shadow(remap = false)
	public abstract int checkInfoSodium(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int prevMask);

	@Shadow(remap = false)
	public abstract boolean containsSodium(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int prevMask);

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}

	@Override
	public int intersectBox(float v, float v1, float v2, float v3, float v4, float v5, int i) {
		return this.checkInfoSodium(v, v1, v2, v3, v4, v5, i);
	}

	@Override
	public boolean containsBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int skipMask) {
		return this.containsSodium(minX, minY, minZ, maxX, maxY, maxZ, skipMask);
	}
}
