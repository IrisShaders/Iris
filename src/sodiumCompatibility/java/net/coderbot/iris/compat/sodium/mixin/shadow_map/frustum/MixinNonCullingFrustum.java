package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import net.coderbot.iris.shadows.frustum.fallback.NonCullingFrustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NonCullingFrustum.class)
public class MixinNonCullingFrustum implements Frustum {
	@Override
	public Visibility testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return Visibility.INSIDE;
	}
}
