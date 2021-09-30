package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.LanguageMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LanguageMapTest {
	@Test
	void testLoadLanguages() {
		ShaderPack shaderPack;

		try {
			shaderPack = new ShaderPack(IrisTests.getTestShaderPackPath("language_maps"));
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack language_maps", e);
			return;
		}

		LanguageMap languageMap = shaderPack.getLanguageMap();
	}
}
