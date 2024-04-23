package net.irisshaders.iris.compat.sodium.mixin.copyEntity.cull;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
	@Inject(method = "shouldRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z", shift = At.Shift.AFTER), cancellable = true)
	private void preShouldRender(T entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		var renderer = SodiumWorldRenderer.instanceNullable();

		if (renderer == null) {
			return;
		}

		// If the entity isn't culled already by other means, try to perform a second pass
		if (cir.getReturnValue() && !renderer.isEntityVisible(entity)) {
			cir.setReturnValue(false);
		}
	}
}
