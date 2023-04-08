package net.irisshaders.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import me.jellysquid.mods.sodium.client.util.frustum.FrustumAdapter;
import net.irisshaders.iris.shadows.frustum.CullEverythingFrustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CullEverythingFrustum.class)
public class MixinCullEverythingFrustum implements Frustum, FrustumAdapter {
	@Override
	public Visibility testBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return Visibility.OUTSIDE;
	}

	@Override
	public Frustum sodium$createFrustum() {
		return this;
	}
}
