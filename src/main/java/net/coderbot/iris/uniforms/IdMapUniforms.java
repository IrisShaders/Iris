package net.coderbot.iris.uniforms;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(UniformHolder uniforms, IdMap idMap) {
		Map<Identifier, Integer> blockIdMap = idMap.getBlockProperties();
		Map<Identifier, Integer> entityIdMap = idMap.getEntityIdMap();

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId",
				new HeldItemSupplier(Hand.MAIN_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2",
				new HeldItemSupplier(Hand.OFF_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "blockEntityId", () -> getBlockEntityId(blockIdMap))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "entityId", () -> getEntityId(entityIdMap));

	}

	/**
	 * Provides the currently held item in the given hand as a uniform. Uses the item.properties ID map to map the item
	 * to an integer.
	 */
	private static class HeldItemSupplier implements IntSupplier {
		private final Hand hand;
		private final Map<Identifier, Integer> itemIdMap;

		HeldItemSupplier(Hand hand, Map<Identifier, Integer> itemIdMap) {
			this.hand = hand;
			this.itemIdMap = itemIdMap;
		}

		@Override
		public int getAsInt() {
			if (MinecraftClient.getInstance().player == null) {
				// Not valid when the player doesn't exist
				return -1;
			}

			ItemStack heldStack = MinecraftClient.getInstance().player.getStackInHand(hand);
			Identifier heldItemId = Registry.ITEM.getId(heldStack.getItem());

			return itemIdMap.getOrDefault(heldItemId, -1);
		}
	}

	/**
	 * returns the block entity id of the block entity that is currently being rendererd
	 * based on values from block.properties.
	 *
	 * @return the blockentity id
	 */
	private static int getBlockEntityId(Map<Identifier, Integer> blockIdMap) {
		BlockEntity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();

		if (entity == null || !entity.hasWorld()) {
			return -1;
		}

		ClientWorld world = Objects.requireNonNull(MinecraftClient.getInstance().world);

		Block blockAt = world.getBlockState(entity.getPos()).getBlock();

		if (!entity.getType().supports(blockAt)) {
			// Somehow the block here isn't compatible with the block entity at this location.
			// I'm not sure how this could ever reasonably happen.
			return -1;
		}

		return blockIdMap.getOrDefault(Registry.BLOCK.getId(blockAt), -1);
	}

	/**
	 * returns the entity id based on the parsed entity id from entity.properties
	 *
	 * @return the id the entity. Defaults to -1 if not specified
	 */
	private static int getEntityId(Map<Identifier, Integer> entityIdMap) {
		Entity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();

		if (entity == null) {
			// Not valid if no entity is being rendered
			return -1;
		}

		Identifier entityId = Registry.ENTITY_TYPE.getId(entity.getType());

		return entityIdMap.getOrDefault(entityId, -1);
	}
}
