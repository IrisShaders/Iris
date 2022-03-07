package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.preprocessor.PropertiesPreprocessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PreprocessorTest {
	@Test
	void testWeirdPropertiesLineContinuation() {
		// This weirdness shows up in Voyager Shaders.
		Assertions.assertEquals("Test\n", PropertiesPreprocessor.preprocessSource("\\\t\t   \nTest"));
	}

	@Test
	void testNormalPropertiesLineContinuation() {
		// This weirdness shows up in Voyager Shaders.
		Assertions.assertEquals("Test Test\n", PropertiesPreprocessor.preprocessSource("Test \\\nTest"));
	}
}
