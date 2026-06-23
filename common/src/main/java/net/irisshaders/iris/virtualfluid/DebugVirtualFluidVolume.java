package net.irisshaders.iris.virtualfluid;

import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidSurfaceConsumer;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidType;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidVisualProperties;
import net.irisshaders.iris.api.v0.virtualfluid.VirtualFluidVolume;

final class DebugVirtualFluidVolume implements VirtualFluidVolume {
	private final double minX;
	private final double minY;
	private final double minZ;
	private final double maxX;
	private final double maxY;
	private final double maxZ;
	private final VirtualFluidVisualProperties properties;

	DebugVirtualFluidVolume(double centerX, double centerY, double centerZ, int materialId) {
		this.minX = centerX - 2.0D;
		this.minY = centerY - 1.0D;
		this.minZ = centerZ - 2.0D;
		this.maxX = centerX + 2.0D;
		this.maxY = centerY;
		this.maxZ = centerZ + 2.0D;
		this.properties = VirtualFluidVisualProperties.waterLike("iris:debug_virtual_water", materialId);
	}

	@Override
	public String id() {
		return "iris:debug_virtual_water";
	}

	@Override
	public VirtualFluidType type() {
		return VirtualFluidType.WATER_LIKE;
	}

	@Override
	public VirtualFluidVisualProperties visualProperties() {
		return properties;
	}

	@Override
	public double minX() {
		return minX;
	}

	@Override
	public double minY() {
		return minY;
	}

	@Override
	public double minZ() {
		return minZ;
	}

	@Override
	public double maxX() {
		return maxX;
	}

	@Override
	public double maxY() {
		return maxY;
	}

	@Override
	public double maxZ() {
		return maxZ;
	}

	@Override
	public boolean contains(double worldX, double worldY, double worldZ) {
		return worldX >= minX && worldX <= maxX
			&& worldY >= minY && worldY <= maxY
			&& worldZ >= minZ && worldZ <= maxZ;
	}

	@Override
	public double surfaceHeightAt(double worldX, double worldZ) {
		return maxY;
	}

	@Override
	public double depthAt(double worldX, double worldZ) {
		if (worldX < minX || worldX > maxX || worldZ < minZ || worldZ > maxZ) {
			return 0.0D;
		}
		return maxY - minY;
	}

	@Override
	public void forEachSurfaceQuad(VirtualFluidSurfaceConsumer consumer) {
		float r = 0.20F, g = 0.55F, b = 0.95F, a = 0.55F;
		
		// Top Face
		consumer.acceptQuad(
			minX, maxY, minZ,
			maxX, maxY, minZ,
			maxX, maxY, maxZ,
			minX, maxY, maxZ,
			r, g, b, a
		);

		// South Face (+Z)
		consumer.acceptQuad(
			minX, minY, maxZ,
			maxX, minY, maxZ,
			maxX, maxY, maxZ,
			minX, maxY, maxZ,
			r, g, b, a
		);

		// North Face (-Z)
		consumer.acceptQuad(
			maxX, minY, minZ,
			minX, minY, minZ,
			minX, maxY, minZ,
			maxX, maxY, minZ,
			r, g, b, a
		);

		// East Face (+X)
		consumer.acceptQuad(
			maxX, minY, maxZ,
			maxX, minY, minZ,
			maxX, maxY, minZ,
			maxX, maxY, maxZ,
			r, g, b, a
		);

		// West Face (-X)
		consumer.acceptQuad(
			minX, minY, minZ,
			minX, minY, maxZ,
			minX, maxY, maxZ,
			minX, maxY, minZ,
			r, g, b, a
		);
	}
}
