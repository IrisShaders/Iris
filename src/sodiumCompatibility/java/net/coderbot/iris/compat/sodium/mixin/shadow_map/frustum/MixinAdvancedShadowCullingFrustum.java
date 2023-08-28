package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import me.jellysquid.mods.sodium.client.render.viewport.frustum.Frustum;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import org.joml.FrustumIntersection;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AdvancedShadowCullingFrustum.class)
public abstract class MixinAdvancedShadowCullingFrustum implements ViewportProvider, Frustum {
	@Shadow(remap = false)
	protected abstract int checkCornerVisibility(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

	@Shadow
	public double x;

	@Shadow
	public double y;

	@Shadow
	public double z;

	@Shadow
	@Final
	protected BoxCuller boxCuller;

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return (boxCuller == null || !boxCuller.isCulledSodium(minX, minY, minZ, maxX, maxY, maxZ)) && this.checkCornerVisibility(minX, minY, minZ, maxX, maxY, maxZ) > 0;
	}

	@Unique
	private Vector3d position = new Vector3d();

	@Override
	public Viewport sodium$createViewport() {
		return new Viewport(this, position.set(x, y, z));
	}
}
