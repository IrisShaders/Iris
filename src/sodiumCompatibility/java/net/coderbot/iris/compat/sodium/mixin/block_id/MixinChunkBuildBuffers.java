package net.coderbot.iris.compat.sodium.mixin.block_id;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import me.jellysquid.mods.sodium.client.SodiumClientMod;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPassManager;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexType;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ChunkBuildBuffersExt;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Associates the block context holder with the chunk build buffers, allowing {@link MixinChunkRenderRebuildTask} to pass
 * data to {@link ContextAwareVertexWriter}.
 */
@Mixin(ChunkBuildBuffers.class)
public class MixinChunkBuildBuffers implements ChunkBuildBuffersExt {
	@Shadow
	@Final
	private ChunkVertexBufferBuilder[] vertexBuffers;
	@Unique
	private BlockContextHolder contextHolder;

	@Inject(method = "<init>", at = @At("RETURN"), remap = false)
	private void iris$onConstruct(ChunkVertexType vertexType, BlockRenderPassManager renderPassManager, CallbackInfo ci) {
		Object2IntMap<BlockState> blockStateIds = BlockRenderingSettings.INSTANCE.getBlockStateIds();

		if (blockStateIds != null) {
			this.contextHolder = new BlockContextHolder(blockStateIds);
		} else {
			this.contextHolder = new BlockContextHolder();
		}
	}

	@Inject(method = "<init>", remap = false, at = @At(value = "TAIL", remap = false))
	private void iris$redirectWriterCreation(ChunkVertexType vertexType, BlockRenderPassManager renderPassManager, CallbackInfo ci) {
		for (ChunkVertexBufferBuilder builder : this.vertexBuffers) {
			if (builder instanceof ContextAwareVertexWriter) {
				((ContextAwareVertexWriter) builder).iris$setContextHolder(contextHolder);
			}
		}
	}

	@Override
	public void iris$setLocalPos(int localPosX, int localPosY, int localPosZ) {
		this.contextHolder.setLocalPos(localPosX, localPosY, localPosZ);
	}

	@Override
	public void iris$setMaterialId(BlockState state, short renderType) {
		this.contextHolder.set(state, renderType);
	}

	@Override
	public void iris$resetBlockContext() {
		this.contextHolder.reset();
	}
}
