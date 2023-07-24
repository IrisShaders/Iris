package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import net.coderbot.iris.compat.sodium.impl.shadow_map.ExtendedViewport;
import net.coderbot.iris.compat.sodium.impl.shadow_map.IrisFrustum;
import net.coderbot.iris.shadows.frustum.CullEverythingFrustum;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CullEverythingFrustum.class)
public class MixinCullEverythingFrustum implements IrisFrustum, ViewportProvider {
	@Override
	public Viewport sodium$createViewport() {
		return new ExtendedViewport(this);
	}

	@Override
	public boolean apply(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return false;
	}
}
