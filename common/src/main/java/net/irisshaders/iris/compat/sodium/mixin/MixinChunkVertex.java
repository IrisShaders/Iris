package net.irisshaders.iris.compat.sodium.mixin;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkVertexEncoder.Vertex.class)
public class MixinChunkVertex implements ChunkVertexExtension {
	private byte blockEmission;
	private int blockId;
	private byte renderType;
	private int localPosX, localPosY, localPosZ;
	private boolean ignoresMidBlock = false;

	@Override
	public void iris$setData(byte blockEmission, byte renderType, int blockId, int localX, int localY, int localZ) {
		this.blockEmission = blockEmission;
		this.renderType = renderType;
		this.blockId = blockId;
		this.localPosX = localX;
		this.localPosY = localY;
		this.localPosZ = localZ;
	}

	@Override
	public void iris$ignoresMidBlock(boolean setIgnore) {
		this.ignoresMidBlock = setIgnore;
	}

	@Override
	public void iris$copyData(ChunkVertexExtension dest) {
		dest.iris$setData(blockEmission, renderType, blockId, localPosX, localPosY, localPosZ);
	}

	@Inject(method = "copyVertexTo", at = @At("HEAD"))
	private static void iris$copyVertex(ChunkVertexEncoder.Vertex from, ChunkVertexEncoder.Vertex _to, CallbackInfo ci) {
		((ChunkVertexExtension) from).iris$copyData((ChunkVertexExtension) _to);
	}

	@Override
	public int getLocalPosX() {
		return localPosX;
	}

	@Override
	public int getLocalPosY() {
		return localPosY;
	}

	@Override
	public int getLocalPosZ() {
		return localPosZ;
	}

	@Override
	public int getBlockId() {
		return blockId;
	}

	@Override
	public byte getRenderType() {
		return renderType;
	}

	@Override
	public byte getBlockEmission() {
		return blockEmission;
	}

	@Override
	public boolean ignoreMidBlock() {
		return ignoresMidBlock;
	}
}
