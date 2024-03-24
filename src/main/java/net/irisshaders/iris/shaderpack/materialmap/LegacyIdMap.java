package net.irisshaders.iris.shaderpack.materialmap;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LegacyIdMap {
	private static final ImmutableList<String> COLORS =
		ImmutableList.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
			"light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black");

	private static final ImmutableList<String> WOOD_TYPES =
		ImmutableList.of("oak", "birch", "jungle", "spruce", "acacia", "dark_oak");

	public static void addLegacyValues(Int2ObjectMap<List<BlockEntry>> blockIdMap) {
		add(blockIdMap, 1, block("stone"), block("granite"), block("diorite"), block("andesite"));
		add(blockIdMap, 2, block("grass_block"));
		add(blockIdMap, 4, block("cobblestone"));

		add(blockIdMap, 50, block("torch"));
		add(blockIdMap, 89, block("glowstone"));

		// TODO: what about inactive redstone lamps?
		add(blockIdMap, 124, block("redstone_lamp"));

		add(blockIdMap, 12, block("sand"));
		add(blockIdMap, 24, block("sandstone"));

		add(blockIdMap, 41, block("gold_block"));
		add(blockIdMap, 42, block("iron_block"));
		add(blockIdMap, 57, block("diamond_block"));
		// Apparently this is what SEUS v11 expects? Maybe old shadersmod was buggy.
		add(blockIdMap, -123, block("emerald_block"));

		addMany(blockIdMap, 35, COLORS, color -> block(color + "_wool"));

		// NB: Use the "still" IDs for water and lava, since some shader packs don't properly support the "flowing"
		// versions: https://github.com/IrisShaders/Iris/issues/1462
		add(blockIdMap, 9, block("water"));
		add(blockIdMap, 11, block("lava"));
		add(blockIdMap, 79, block("ice"));

		addMany(blockIdMap, 18, WOOD_TYPES, woodType -> block(woodType + "_leaves"));

		addMany(blockIdMap, 95, COLORS, color -> block(color + "_stained_glass"));
		addMany(blockIdMap, 160, COLORS, color -> block(color + "_stained_glass_pane"));

		// Short grass / bush
		add(blockIdMap, 31, block("grass"), block("seagrass"), block("sweet_berry_bush"));

		// Crops (59 = wheat), but we include carrots and potatoes too.
		add(blockIdMap, 59, block("wheat"), block("carrots"), block("potatoes"));

		// Small flowers
		add(blockIdMap, 37, block("dandelion"), block("poppy"), block("blue_orchid"),
			block("allium"), block("azure_bluet"), block("red_tulip"), block("pink_tulip"),
			block("white_tulip"), block("orange_tulip"), block("oxeye_daisy"),
			block("cornflower"), block("lily_of_the_valley"), block("wither_rose"));

		// Big tall grass / flowers
		// Also include seagrass here
		add(blockIdMap, 175, block("sunflower"), block("lilac"), block("tall_grass"),
			block("large_fern"), block("rose_bush"), block("peony"), block("tall_seagrass"));

		// Fire
		add(blockIdMap, 51, block("fire"));

		// Lily pad
		add(blockIdMap, 111, block("lily_pad"));

		// TODO: 76 -> redstone_torch (on)
	}

	private static BlockEntry block(String name) {
		return new BlockEntry(new NamespacedId("minecraft", name), Collections.emptyMap());
	}

	private static void addMany(Int2ObjectMap<List<BlockEntry>> blockIdMap, int id, List<String> prefixes, Function<String, BlockEntry> toId) {
		List<BlockEntry> entries = new ArrayList<>();

		for (String prefix : prefixes) {
			entries.add(toId.apply(prefix));
		}

		blockIdMap.put(id, entries);
	}

	private static void add(Int2ObjectMap<List<BlockEntry>> blockIdMap, int id, BlockEntry... entries) {
		blockIdMap.put(id, Arrays.asList(entries));
	}
}
