package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import net.coderbot.iris.compat.sodium.impl.shadow_map.ExtendedViewport;
import net.coderbot.iris.compat.sodium.impl.shadow_map.IrisFrustum;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AdvancedShadowCullingFrustum.class)
public abstract class MixinAdvancedShadowCullingFrustum implements ViewportProvider, IrisFrustum {
	@Shadow(remap = false)
	public abstract int fastAabbTest(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

	@Override
	public boolean apply(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return ((AdvancedShadowCullingFrustum) (Object) this).checkCornerVisibilityBool(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public Viewport sodium$createViewport() {
		return new ExtendedViewport(this);
	}
}
