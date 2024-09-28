package net.irisshaders.iris.mixin;

import net.irisshaders.iris.mixinterface.EntityUniqueId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.BitSet;

import static net.irisshaders.iris.pathways.EntityIdStorage.ENTITY_ID_STORAGE;

@Mixin(Entity.class)
public abstract class MixinEntity implements EntityUniqueId {
	@Unique
	private int rollingId = -1;

	@Override
	public int iris$getRollingId() {
		if (rollingId == -1) {
			int newId = ENTITY_ID_STORAGE.nextClearBit(0);
			rollingId = newId;
			ENTITY_ID_STORAGE.set(newId);
		}

		return rollingId;
	}

	@Inject(method = "setRemoved", at = @At("HEAD"))
	private void iris$clearId(Entity.RemovalReason removalReason, CallbackInfo ci) {
		if (rollingId == -1) return;

		ENTITY_ID_STORAGE.clear(rollingId);
		rollingId = -1;
	}
}
