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

	@Shadow
	public double x;

	@Shadow
	public double y;

	@Shadow
	public double z;

	@Override
	public boolean apply(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return ((AdvancedShadowCullingFrustum) (Object) this).fastAabbTest(minX, minY, minZ, maxX, maxY, maxZ) > 0;
	}

	@Override
	public Viewport sodium$createViewport() {
		return new ExtendedViewport(this, (float) x,  (float) y,  (float) z);
	}
}
