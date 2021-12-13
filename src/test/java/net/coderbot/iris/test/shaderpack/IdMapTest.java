package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.materialmap.BlockRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class IdMapTest {
	private static final Map<NamespacedId, BlockRenderType> EXPECTED;
	
	static {
		EXPECTED = new HashMap<>();

		/*
		layer.translucent=glass_pane fence minecraft:wooden_door
		layer.solid=minecraft:oak_leaves
		layer.cutout=terrestria:hemlock_leaves
		layer.cutout_mipped=grass
		 */
		EXPECTED.put(new NamespacedId("minecraft", "glass_pane"), BlockRenderType.TRANSLUCENT);
		EXPECTED.put(new NamespacedId("minecraft", "fence"), BlockRenderType.TRANSLUCENT);
		EXPECTED.put(new NamespacedId("minecraft", "wooden_door"), BlockRenderType.TRANSLUCENT);
		EXPECTED.put(new NamespacedId("minecraft", "oak_leaves"), BlockRenderType.SOLID);
		EXPECTED.put(new NamespacedId("terrestria", "hemlock_leaves"), BlockRenderType.CUTOUT);
		EXPECTED.put(new NamespacedId("minecraft", "grass"), BlockRenderType.CUTOUT_MIPPED);
	}
	
	@Test
	void testLoadIdMaps() {
		ShaderPack shaderPack;

		// ensure that we can actually load the shader pack
		try {
			shaderPack = new ShaderPack(IrisTests.getTestShaderPackPath("id_maps"));
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack id_maps", e);
			return;
		}

		Map<NamespacedId, BlockRenderType> overrides = shaderPack.getIdMap().getBlockRenderTypeMap();
		Assertions.assertEquals(EXPECTED, overrides);
	}
}
