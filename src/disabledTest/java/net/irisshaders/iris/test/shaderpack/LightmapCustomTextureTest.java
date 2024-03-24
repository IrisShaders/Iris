package net.irisshaders.iris.test.shaderpack;

import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.texture.CustomTextureData;
import net.irisshaders.iris.test.IrisTests;
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
