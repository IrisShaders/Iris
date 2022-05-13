package net.coderbot.iris.test.shaderpack;

import net.coderbot.iris.shaderpack.preprocessor.PropertiesPreprocessor;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PreprocessorTest {
	@Test
	void testWeirdPropertiesLineContinuation() {
		// This weirdness shows up in Voyager Shaders.
		Assertions.assertEquals("Test\n", PropertiesPreprocessor.preprocessSource("\\\t\t   \nTest",
			IrisTests.TEST_ENVIRONMENT_DEFINES));
	}

	@Test
	void testNormalPropertiesLineContinuation() {
		// This weirdness shows up in Voyager Shaders.
		Assertions.assertEquals("Test Test\n", PropertiesPreprocessor.preprocessSource("Test \\\nTest",
			IrisTests.TEST_ENVIRONMENT_DEFINES));
	}

	@Test
	void testWeirdHashComment() {
		String line = "test= <empty> #[Comment]";

		// This needs to be handled properly for Oceano shaders to work right
		Assertions.assertEquals(line + "\n", PropertiesPreprocessor.preprocessSource(line,
			IrisTests.TEST_ENVIRONMENT_DEFINES));
	}

	@Test
	void testHashComment() {
		// Note: Verify that this does not spam the log by inspecting manually.
		String line =
			"# Test\n" +
			"#test comment\n" +
			"#1xxx\n";

		Assertions.assertEquals("", PropertiesPreprocessor.preprocessSource(line,
			IrisTests.TEST_ENVIRONMENT_DEFINES).trim());
	}

	@Test
	void testPreprocessorDirectives() {
		String line =
			"#if MC_VERSION >= 11300\n" +
			"block.1=minecraft:grass_block\n" +
			"#else\n" +
			"block.1=minecraft:grass\n" +
			"#endif\n";

		Assertions.assertEquals("block.1=minecraft:grass_block", PropertiesPreprocessor.preprocessSource(line,
			IrisTests.TEST_ENVIRONMENT_DEFINES).trim());
	}

	@Test
	void testWeirdDefineDoesNotCrash() {
		String line = "# define TEST";

		Assertions.assertEquals("", PropertiesPreprocessor.preprocessSource(line,
			IrisTests.TEST_ENVIRONMENT_DEFINES).trim());
	}
}
