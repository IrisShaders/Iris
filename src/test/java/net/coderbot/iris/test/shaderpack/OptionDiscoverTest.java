package net.coderbot.iris.test.shaderpack;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.option.OptionAnnotatedSource;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.StringOption;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OptionDiscoverTest {
	@Test
	void testSingleValueDefine() {
		testTrivialString(
				"#define SECRET 0 //[0]",
				"SECRET", "0", ImmutableList.of("0")
		);
	}

	@Test
	void testAstraLexDefine() {
		// AstraLex omits the space
		testTrivialString(
			"#define SECRET 0//[0]",
			"SECRET", "0", ImmutableList.of("0")
		);
	}

	@Test
	void testInferredDefaultDefine() {
		testTrivialString(
				"#define OPT 1 //[0]",
				"OPT", "1", ImmutableList.of("0", "1")
		);
	}

	@Test
	void testNonConfigurableConstant() {
		// TODO: What about "const bool" options???
		testNoDiscovery("#define PI 3.14 // It's PI.");
		testNoDiscovery("#define PI 3.14");
		testNoDiscovery("const int noiseTextureResolution = 512;");
		testNoDiscovery("const int noiseTextureResolution = 512; // Default noise texture size [");
	}

	private void testTrivialString(String base, String expectedOptionName, String expectedDefault,
							 ImmutableList<String> expectedAllowed) {
		OptionAnnotatedSource source = new OptionAnnotatedSource(base);
		OptionSet options = source.getOptionSet(
				AbsolutePackPath.fromAbsolutePath("/<hardcoded>"),
				source.getBooleanDefineReferences().keySet());

		Assertions.assertEquals(options.getBooleanOptions().size(), 0,
				"Unexpectedly discovered a boolean option");

		if (options.getStringOptions().size() == 0) {
			Assertions.fail("No string options were discovered, diagnostics: " + source.getDiagnostics());
		}

		StringOption option = options.getStringOptions().values().iterator().next().getOption();

		Assertions.assertEquals(option.getName(), expectedOptionName);
		Assertions.assertEquals(option.getDefaultValue(), expectedDefault);
		Assertions.assertEquals(option.getAllowedValues(), expectedAllowed);
	}

	private void testNoDiscovery(String base) {
		OptionAnnotatedSource source = new OptionAnnotatedSource(base);
		OptionSet options = source.getOptionSet(
				AbsolutePackPath.fromAbsolutePath("/<hardcoded>"),
				source.getBooleanDefineReferences().keySet());

		Assertions.assertEquals(options.getBooleanOptions().size(), 0,
				"Unexpectedly discovered a boolean option");

		Assertions.assertEquals(options.getStringOptions().size(), 0,
				"Unexpectedly discovered a string option");
	}
}
