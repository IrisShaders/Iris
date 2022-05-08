package net.coderbot.iris.test.shaderpack;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.materialmap.BlockEntry;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdMapTest {
	private static final Map<NamespacedId, BlockRenderType> EXPECTED_LAYERS;
	private static final Int2ObjectMap<List<BlockEntry>> EXPECTED_BLOCKS;

	static {
		EXPECTED_LAYERS = new HashMap<>();

		/*
		layer.translucent=glass_pane fence minecraft:wooden_door
		layer.solid=minecraft:oak_leaves
		layer.cutout=terrestria:hemlock_leaves
		layer.cutout_mipped=grass
		 */
		EXPECTED_LAYERS.put(new NamespacedId("minecraft", "glass_pane"), BlockRenderType.TRANSLUCENT);
		EXPECTED_LAYERS.put(new NamespacedId("minecraft", "fence"), BlockRenderType.TRANSLUCENT);
		EXPECTED_LAYERS.put(new NamespacedId("minecraft", "wooden_door"), BlockRenderType.TRANSLUCENT);
		EXPECTED_LAYERS.put(new NamespacedId("minecraft", "oak_leaves"), BlockRenderType.SOLID);
		EXPECTED_LAYERS.put(new NamespacedId("terrestria", "hemlock_leaves"), BlockRenderType.CUTOUT);
		EXPECTED_LAYERS.put(new NamespacedId("minecraft", "grass"), BlockRenderType.CUTOUT_MIPPED);

		EXPECTED_BLOCKS = new Int2ObjectOpenHashMap<>();
		EXPECTED_BLOCKS.put(37, Arrays.asList(
				new BlockEntry(new NamespacedId("minecraft", "red_stained_glass"), new HashMap<>()),
				new BlockEntry(new NamespacedId("minecraft", "blue_stained_glass"), new HashMap<>()),
				new BlockEntry(new NamespacedId("minecraft", "white_stained_glass"), new HashMap<>())));
	}

	@Test
	void testLoadIdMaps() {
		ShaderPack shaderPack;

		// ensure that we can actually load the shader pack
		try {
			shaderPack = new ShaderPack(IrisTests.getTestShaderPackPath("id_maps"), IrisTests.TEST_ENVIRONMENT_DEFINES);
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack id_maps", e);
			return;
		}

		Map<NamespacedId, BlockRenderType> overrides = shaderPack.getIdMap().getBlockRenderTypeMap();
		Int2ObjectMap<List<BlockEntry>> blocks = shaderPack.getIdMap().getBlockProperties();
		Assertions.assertEquals(EXPECTED_LAYERS, overrides);
		Assertions.assertEquals(EXPECTED_BLOCKS, blocks);
	}
}
