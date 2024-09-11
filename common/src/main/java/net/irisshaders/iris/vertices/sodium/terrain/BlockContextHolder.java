package net.irisshaders.iris.vertices.sodium.terrain;

public class BlockContextHolder {
	private byte blockEmission;
	private int blockId;
	private byte renderType;
	private int localPosX, localPosY, localPosZ;

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
		// TODO ADD THIS
		return false;
	}
}
