package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import me.jellysquid.mods.sodium.client.util.frustum.FrustumAdapter;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancedShadowCullingFrustum.class)
public abstract class MixinAdvancedShadowCullingFrustum implements Frustum, FrustumAdapter {
	@Shadow(remap = false)
	public abstract int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

	@Override
	public Visibility testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		// TODO: Visibility.INSIDE
		return switch(fastAabbTest(minX, minY, minZ, maxX, maxY, maxZ)) {
			case 0 -> Visibility.OUTSIDE;
			case 1 -> Visibility.INSIDE;
			case 2 -> Visibility.INTERSECT;
			default ->
				throw new IllegalStateException("Unexpected value: " + fastAabbTest(minX, minY, minZ, maxX, maxY, maxZ));
		};
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
