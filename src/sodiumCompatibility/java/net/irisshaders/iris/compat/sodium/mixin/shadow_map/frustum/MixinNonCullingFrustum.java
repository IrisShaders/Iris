package net.irisshaders.iris.compat.sodium.mixin.shadow_map.frustum;

import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import me.jellysquid.mods.sodium.client.render.viewport.ViewportProvider;
import me.jellysquid.mods.sodium.client.render.viewport.frustum.Frustum;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import net.minecraft.client.Minecraft;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(NonCullingFrustum.class)
public class MixinNonCullingFrustum implements Frustum, ViewportProvider {
	private final Vector3d pos = new Vector3d();

	@Override
	public Viewport sodium$createViewport() {
		return new Viewport(this, pos.set(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().x, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().y, Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().z));
	}

	@Override
	public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return true;
	}
}
