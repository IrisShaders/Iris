package net.irisshaders.iris.vertices.sodium.terrain;

public class BlockContextHolder {
	private byte blockEmission;
	private int blockId;
	private byte renderType;
	private int localPosX, localPosY, localPosZ;
	private boolean ignoreMidBlock;
	private int oldId = -1;

	public int getBlockId() {
		return blockId;
	}

	public byte getRenderType() {
		return renderType;
	}

	public byte getBlockEmission() {
		return blockEmission;
	}

	public int getLocalPosX() {
		return localPosX;
	}

	public int getLocalPosY() {
		return localPosY;
	}

	public int getLocalPosZ() {
		return localPosZ;
	}

	public void setBlockData(int blockId, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
		this.blockId = blockId;
		this.renderType = renderType;
		this.blockEmission = blockEmission;
		this.localPosX = localPosX;
		this.localPosY = localPosY;
		this.localPosZ = localPosZ;
	}

	public boolean ignoreMidBlock() {
		return ignoreMidBlock;
	}

	public void setIgnoreMidBlock(boolean ignoreMidBlock) {
		this.ignoreMidBlock = ignoreMidBlock;
	}

	public void overrideBlock(int block) {
		if (this.blockId == block)
			return;

		if (this.oldId == -1) {
			this.oldId = blockId;
		}

		this.blockId = block;
	}

	public void restoreBlock() {
		if (this.oldId == -1)
			return;
		this.blockId = oldId;
		this.oldId = -1;
	}
}
