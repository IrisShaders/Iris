package net.coderbot.iris.uniforms;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(UniformHolder uniforms, IdMap idMap) {
		Map<BlockState, Integer> blockIdMap = idMap.getBlockProperties();
		Map<ResourceLocation, Integer> entityIdMap = idMap.getEntityIdMap();

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId",
				new HeldItemSupplier(InteractionHand.MAIN_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2",
				new HeldItemSupplier(InteractionHand.OFF_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "blockEntityId", () -> getBlockEntityId(blockIdMap))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "entityId", () -> getEntityId(entityIdMap));

	}

	/**
	 * Provides the currently held item in the given hand as a uniform. Uses the item.properties ID map to map the item
	 * to an integer.
	 */
	private static class HeldItemSupplier implements IntSupplier {
		private final InteractionHand hand;
		private final Map<ResourceLocation, Integer> itemIdMap;

		HeldItemSupplier(InteractionHand hand, Map<ResourceLocation, Integer> itemIdMap) {
			this.hand = hand;
			this.itemIdMap = itemIdMap;
		}

		@Override
		public int getAsInt() {
			if (Minecraft.getInstance().player == null) {
				// Not valid when the player doesn't exist
				return -1;
			}

			ItemStack heldStack = Minecraft.getInstance().player.getItemInHand(hand);
			ResourceLocation heldItemId = Registry.ITEM.getKey(heldStack.getItem());

			return itemIdMap.getOrDefault(heldItemId, -1);
		}
	}

	/**
	 * returns the block entity id of the block entity that is currently being rendererd
	 * based on values from block.properties.
	 *
	 * @return the blockentity id
	 */
	private static int getBlockEntityId(Map<BlockState, Integer> blockIdMap) {
		BlockEntity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();

		if (entity == null || !entity.hasLevel()) {
			return -1;
		}

		ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);

		BlockState blockAt = level.getBlockState(entity.getBlockPos());

		if (!entity.getType().isValid(blockAt.getBlock())) {
			// Somehow the block here isn't compatible with the block entity at this location.
			// I'm not sure how this could ever reasonably happen.
			return -1;
		}

		return blockIdMap.getOrDefault(blockAt, -1);
	}

	/**
	 * returns the entity id based on the parsed entity id from entity.properties
	 *
	 * @return the id the entity. Defaults to -1 if not specified
	 */
	private static int getEntityId(Map<ResourceLocation, Integer> entityIdMap) {
		Entity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();

		if (entity == null) {
			// Not valid if no entity is being rendered
			return -1;
		}

		ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(entity.getType());

		return entityIdMap.getOrDefault(entityId, -1);
	}
}
