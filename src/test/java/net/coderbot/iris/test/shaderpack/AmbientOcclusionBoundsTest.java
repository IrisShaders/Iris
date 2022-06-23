package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AmbientOcclusionBoundsTest {
	@Test
	void testAmbientOcclusionBounds() {
		ShaderPack shaderPack = IrisTests.loadPackOrFail("ambient_occlusion_out_of_bounds");

		Assertions.assertEquals(1.0f,
			shaderPack.getProgramSet(DimensionId.OVERWORLD).getPackDirectives().getAmbientOcclusionLevel());
	}
}
