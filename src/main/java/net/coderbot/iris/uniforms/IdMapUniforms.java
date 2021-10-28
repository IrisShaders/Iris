package net.coderbot.iris.uniforms;

import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.IntSupplier;

public final class IdMapUniforms {

	private IdMapUniforms() {
	}

	public static void addIdMapUniforms(UniformHolder uniforms, IdMap idMap) {
		uniforms
				.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId",
						new HeldItemSupplier(InteractionHand.MAIN_HAND, idMap.getItemIdMap()))
				.uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2",
						new HeldItemSupplier(InteractionHand.OFF_HAND, idMap.getItemIdMap()));

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
}
