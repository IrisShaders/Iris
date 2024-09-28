package net.irisshaders.iris.mixin;

import net.irisshaders.iris.mixinterface.QuadPositionAccess;
import net.irisshaders.iris.pathways.EntityIdStorage;
import net.irisshaders.iris.pipeline.QuadPositions;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class MixinBakedQuad implements QuadPositionAccess {
	private static final QuadPositions INVALID_VALUE = new QuadPositions();
	@Unique
	private QuadPositions[] quadPositions = null;

	@Override
	public QuadPositions getQuadPosition(int entityId) {
		if (entityId == -1) return INVALID_VALUE;

		if (quadPositions == null) {
			quadPositions = new QuadPositions[EntityIdStorage.ENTITY_ID_STORAGE.size()];
		}

		if (entityId >= quadPositions.length) {
			QuadPositions[] newArray = new QuadPositions[EntityIdStorage.ENTITY_ID_STORAGE.size()];

			System.arraycopy(quadPositions, 0, newArray, 0, quadPositions.length);

			this.quadPositions = newArray;
		}

		if (quadPositions[entityId] == null) {
			quadPositions[entityId] = new QuadPositions();
		}

		return quadPositions[entityId];
	}
}
