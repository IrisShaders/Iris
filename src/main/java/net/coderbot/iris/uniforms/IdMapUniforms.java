package net.coderbot.iris.uniforms;

import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(DynamicUniformHolder uniforms, IdMap idMap) {
		Map<BlockState, Integer> blockIdMap = idMap.getBlockProperties();

		uniforms
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId",
				new HeldItemSupplier(Hand.MAIN_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2",
				new HeldItemSupplier(Hand.OFF_HAND, idMap.getItemIdMap()))
			.uniform1i(UniformUpdateFrequency.PER_FRAME, "blockEntityId", () -> getBlockEntityId(blockIdMap));

		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity,
				CapturedRenderingState.INSTANCE.getEntityIdNotifier());
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
	private static int getBlockEntityId(Map<BlockState, Integer> blockIdMap) {
		BlockEntity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();

		if (entity == null || !entity.hasWorld()) {
			return -1;
		}

		ClientWorld world = Objects.requireNonNull(MinecraftClient.getInstance().world);

		BlockState blockAt = world.getBlockState(entity.getPos());

		if (!entity.getType().supports(blockAt.getBlock())) {
			// Somehow the block here isn't compatible with the block entity at this location.
			// I'm not sure how this could ever reasonably happen.
			return -1;
		}

		return blockIdMap.getOrDefault(blockAt, -1);
	}
}
