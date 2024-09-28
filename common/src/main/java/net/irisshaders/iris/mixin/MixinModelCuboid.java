package net.irisshaders.iris.mixin;

import net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.irisshaders.iris.mixinterface.ModelPartAccess;
import net.irisshaders.iris.pathways.EntityIdStorage;
import net.irisshaders.iris.pipeline.CubePositions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelCuboid.class)
public class MixinModelCuboid implements ModelPartAccess {
	private static final CubePositions INVALID_VALUE = new CubePositions();
	@Unique
	private CubePositions[] cubePositions = new CubePositions[EntityIdStorage.ENTITY_ID_STORAGE.size()];

	@Override
	public CubePositions getCubePosition(int entityId) {
		if (entityId == -1) return INVALID_VALUE;

		if (entityId >= cubePositions.length) {
			CubePositions[] newArray = new CubePositions[EntityIdStorage.ENTITY_ID_STORAGE.size()];

			System.arraycopy(cubePositions, 0, newArray, 0, cubePositions.length);

			this.cubePositions = newArray;
		}

		if (cubePositions[entityId] == null) {
			cubePositions[entityId] = new CubePositions();
		}

		return cubePositions[entityId];
	}
}
