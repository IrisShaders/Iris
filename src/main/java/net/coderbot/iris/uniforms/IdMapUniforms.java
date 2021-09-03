package net.coderbot.iris.uniforms;

import java.util.Map;
import java.util.function.IntSupplier;

import net.coderbot.iris.gl.uniform.DynamicUniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
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
				new HeldItemSupplier(Hand.OFF_HAND, idMap.getItemIdMap()));

		uniforms.uniform1i("entityId", CapturedRenderingState.INSTANCE::getCurrentRenderedEntity,
				CapturedRenderingState.INSTANCE.getEntityIdNotifier());

		uniforms.uniform1i("blockEntityId", CapturedRenderingState.INSTANCE::getCurrentRenderedBlockEntity,
				CapturedRenderingState.INSTANCE.getBlockEntityIdNotifier());
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
}
