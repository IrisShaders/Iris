package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import net.coderbot.iris.compat.sodium.impl.shadow_map.ExtendedViewport;
import net.coderbot.iris.compat.sodium.impl.shadow_map.IrisFrustum;
import net.coderbot.iris.shadows.frustum.BoxCuller;
import net.coderbot.iris.shadows.frustum.fallback.BoxCullingFrustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BoxCullingFrustum.class)
public class MixinBoxCullingFrustum implements IrisFrustum, ViewportProvider {
	@Shadow(remap = false)
	@Final
	private BoxCuller boxCuller;

	@Override
	public Viewport sodium$createViewport() {
		return new ExtendedViewport(this);
	}

	@Override
	public boolean apply(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return boxCuller.isCulled(minX, minY, minZ, maxX, maxY, maxZ);
	}
}
