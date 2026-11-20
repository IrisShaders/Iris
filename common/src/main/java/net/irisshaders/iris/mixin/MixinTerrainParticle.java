package net.irisshaders.iris.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.mixinterface.ParticleExtension;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.ExtendedDataHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TerrainParticle.class)
public class MixinTerrainParticle {
	@Unique
	private static final NamespacedId BLOCK = new NamespacedId("minecraft", "block");

	// Block break and crack particles skip ParticleEngine#makeParticle and the state is not stored, so take it here.
	@Inject(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
	private void iris$setBlockParticleId(ClientLevel level, double x, double y, double z, double xa, double ya, double za,
		BlockState state, BlockPos pos, CallbackInfo ci) {
		Object2IntFunction<NamespacedId> particleIds = WorldRenderingSettings.INSTANCE.getParticleIds();
		Object2IntMap<BlockState> blockStateIds = WorldRenderingSettings.INSTANCE.getBlockStateIds();

		if (particleIds == null || blockStateIds == null) {
			return;
		}

		((ParticleExtension) (Object) this).iris$setParticleId(
			ExtendedDataHelper.packParticleIds(particleIds.applyAsInt(BLOCK), blockStateIds.getInt(state)));
	}
}
