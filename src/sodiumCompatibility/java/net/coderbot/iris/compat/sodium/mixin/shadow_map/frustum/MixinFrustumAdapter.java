package net.coderbot.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.util.frustum.FrustumAdapter;
import me.jellysquid.mods.sodium.client.util.frustum.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FrustumAdapter.class)
public interface MixinFrustumAdapter {
	/**
	 * @reason FabricMC Mixin does not support injections into interfaces
	 * @author IMS
	 */
	@Overwrite
	static Frustum adapt(net.minecraft.client.renderer.culling.Frustum frustum) {
		if (frustum instanceof Frustum) {
			return (Frustum) frustum;
		} else {
			return ((FrustumAdapter) frustum).sodium$createFrustum();
		}
	}
}
