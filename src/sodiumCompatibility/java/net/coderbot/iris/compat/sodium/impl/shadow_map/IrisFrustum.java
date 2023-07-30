package net.coderbot.iris.compat.sodium.impl.shadow_map;

public interface IrisFrustum {
	boolean apply(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);
}
