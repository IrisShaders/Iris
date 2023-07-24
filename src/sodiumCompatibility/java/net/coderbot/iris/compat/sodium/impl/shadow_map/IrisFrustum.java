package net.coderbot.iris.compat.sodium.impl.shadow_map;

public interface IrisFrustum {
	boolean apply(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);
}
