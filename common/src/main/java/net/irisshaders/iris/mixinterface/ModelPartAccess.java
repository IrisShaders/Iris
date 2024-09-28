package net.irisshaders.iris.mixinterface;

import net.irisshaders.iris.pipeline.CubePositions;

public interface ModelPartAccess {
	CubePositions getCubePosition(int entityId);
}
