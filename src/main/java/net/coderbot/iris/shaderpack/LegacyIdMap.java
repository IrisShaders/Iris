package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// TODO: Don't reference vanilla Minecraft classes, so that the shaderpack loader can be properly isolated from
//       Minecraft.
public class LegacyIdMap {

	public static void addLegacyValues(Object2IntMap<BlockState> blockIdMap) {
		add(blockIdMap, 1, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
		add(blockIdMap, 2, Blocks.GRASS_BLOCK);
		add(blockIdMap, 4, Blocks.COBBLESTONE);

		add(blockIdMap, 50, Blocks.TORCH);
		add(blockIdMap, 89, Blocks.GLOWSTONE);
		add(blockIdMap, 124, Blocks.REDSTONE_LAMP);

		add(blockIdMap, 12, Blocks.SAND);
		add(blockIdMap, 24, Blocks.SANDSTONE);

		add(blockIdMap, 41, Blocks.GOLD_BLOCK);
		add(blockIdMap, 42, Blocks.IRON_BLOCK);
		add(blockIdMap, 57, Blocks.DIAMOND_BLOCK);
		// Apparently this is what SEUS v11 expects? Maybe old shadersmod was buggy.
		add(blockIdMap, -123, Blocks.EMERALD_BLOCK);

		add(blockIdMap, 34, Blocks.WHITE_WOOL, Blocks.ORANGE_WOOL, Blocks.MAGENTA_WOOL,
				Blocks.LIGHT_BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.LIME_WOOL,
				Blocks.PINK_WOOL, Blocks.GRAY_WOOL, Blocks.LIGHT_GRAY_WOOL,
				Blocks.CYAN_WOOL, Blocks.PURPLE_WOOL, Blocks.BLUE_WOOL,
				Blocks.BROWN_WOOL, Blocks.GREEN_WOOL, Blocks.RED_WOOL,
				Blocks.BLACK_WOOL);

		add(blockIdMap, 8, Blocks.WATER);
		add(blockIdMap, 10, Blocks.LAVA);
		add(blockIdMap, 79, Blocks.ICE);

		add(blockIdMap, 18, Blocks.OAK_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.SPRUCE_LEAVES,
				Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

		add(blockIdMap, 95, Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS,
				Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS,
				Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS,
				Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS,
				Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS,
				Blocks.BLACK_STAINED_GLASS);

		add(blockIdMap, 160, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE,
				Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
				Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
				Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE,
				Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE,
				Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE,
				Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE,
				Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE);

		// Short grass / bush
		add(blockIdMap, 31, Blocks.GRASS, Blocks.SEAGRASS, Blocks.SWEET_BERRY_BUSH);

		// Crops (59 = wheat), but we include carrots and potatoes too.
		add(blockIdMap, 59, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES);

		// Small flowers
		add(blockIdMap, 37, Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
				Blocks.RED_TULIP, Blocks.PINK_TULIP, Blocks.WHITE_TULIP, Blocks.ORANGE_TULIP, Blocks.OXEYE_DAISY,
				Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE);

		// Big tall grass / flowers
		// Also include seagrass here
		add(blockIdMap, 175, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.ROSE_BUSH,
				Blocks.PEONY, Blocks.TALL_SEAGRASS);

		// Fire
		add(blockIdMap, 51, Blocks.FIRE);

		// Lily pad
		add(blockIdMap, 111, Blocks.LILY_PAD);
	}

	private static void add(Object2IntMap<BlockState> blockIdMap, int id, Block... leaves) {
		for (Block leaf : leaves) {
			leaf.getStateDefinition().getPossibleStates().forEach(state -> blockIdMap.put(state, id));
		}
	}
}
