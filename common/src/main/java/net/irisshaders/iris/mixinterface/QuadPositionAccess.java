package net.irisshaders.iris.mixinterface;

import net.irisshaders.iris.pipeline.QuadPositions;

public interface QuadPositionAccess {
    QuadPositions getQuadPosition(int entityId);
}
