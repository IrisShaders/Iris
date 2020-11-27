package net.coderbot.iris.uniforms;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.shaderpack.IdMapParser;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Map;
import java.util.function.IntSupplier;

public final class PropertiesUniforms {

	private PropertiesUniforms(){}

	public static void addPropertiesUniforms(ProgramBuilder builder, IdMapParser idMap) {
		builder
		   .uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId",
				   new HeldItemSupplier(Hand.MAIN_HAND, idMap.getItemProperties()))
		   .uniform1i(UniformUpdateFrequency.PER_FRAME, "heldItemId2",
				   new HeldItemSupplier(Hand.OFF_HAND, idMap.getItemProperties()))
		   .uniform1i(UniformUpdateFrequency.PER_FRAME, "blockEntityId", PropertiesUniforms::getBlockEntityId)
		   .uniform1i(UniformUpdateFrequency.PER_FRAME, "entityId", PropertiesUniforms::getEntityId);

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
	 * @return the blockentity id
	 */
	private static int getBlockEntityId() {
		BlockEntity currentEntity = CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity();
		if (currentEntity != null){

			Block block = MinecraftClient.getInstance().world.getBlockState(currentEntity.getPos()).getBlock();
			boolean hasBlock = currentEntity.getType().supports(block) && currentEntity.hasWorld() && BlockEntityRenderDispatcher.INSTANCE.get(currentEntity) != null;
			boolean isBlockParsed = Iris.getPipeline().getPack().getIdMapParser().getBlockProperties().containsKey(Registry.BLOCK.getId(block));
			if (isBlockParsed && hasBlock){
				return Iris.getPipeline().getPack().getIdMapParser().getBlockProperties().get(Registry.BLOCK.getId(block));
			}
		}
		return -1;
	}

	/**
	 * returns the entity id based on the parsed entity id from entity.properties
	 * @return the id the entity. Defaults to -1 if not specified
	 */
	private static int getEntityId() {
		Entity entity = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		if (entity != null){
			Map<Identifier, Integer> entityMap = Iris.getPipeline().getPack().getIdMapParser().getEntityPropertiesMap();
			Identifier entityId = Registry.ENTITY_TYPE.getId(entity.getType());
			if (entityMap.containsKey(entityId)){
				return entityMap.get(entityId); //if defined in entity.properties, return the value specified
			}
		}
		return -1;//return -1 if the entity is not in entity.properties
	}

}
