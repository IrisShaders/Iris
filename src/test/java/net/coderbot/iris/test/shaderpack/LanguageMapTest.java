package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.LanguageMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class LanguageMapTest {
	@Test
	void testLoadLanguages() {
		ShaderPack shaderPack;

		// ensure that we can actually load the shader pack
		try {
			shaderPack = new ShaderPack(IrisTests.getTestShaderPackPath("language_maps"), IrisTests.TEST_ENVIRONMENT_DEFINES);
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack language_maps", e);
			return;
		}

		// ensure that we only loaded two language files
		LanguageMap languageMap = shaderPack.getLanguageMap();
		Assertions.assertEquals(2, languageMap.getLanguages().size(), "number of languages loaded");

		// test that en_US.lang was loaded properly
		{
			Map<String, String> english = languageMap.getTranslations("en_us");
			Assertions.assertNotNull(english, "en_us translations");

			Assertions.assertEquals(2, english.size(), "number of translations in en_US.lang");
			Assertions.assertEquals("Test screen", english.get("screen.TEST"));
			Assertions.assertEquals("Test option", english.get("option.TEST_OPTION"));
		}

		// test that fr_FR.lang was loaded properly
		{
			Map<String, String> french = languageMap.getTranslations("fr_fr");
			Assertions.assertNotNull(french, "fr_fr translations");

			Assertions.assertEquals(1, french.size(), "number of translations in fr_FR.lang");
			Assertions.assertEquals("Écran de test", french.get("screen.TEST"));
		}
	}
}
