package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.texture.CustomTextureData;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class LightmapCustomTextureTest {
	@Test
	void testLightmapCustomTexture() {
		ShaderPack shaderPack = IrisTests.loadPackOrFail("lightmap_custom_texture");

		Assertions.assertEquals(Optional.of(new CustomTextureData.LightmapMarker()), shaderPack.getCustomNoiseTexture());
	}
}
