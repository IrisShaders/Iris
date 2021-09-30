package net.coderbot.iris.test.shaderpack;

import com.google.common.collect.ImmutableMap;
import net.coderbot.iris.shaderpack.DimensionId;
import net.coderbot.iris.shaderpack.ProgramSource;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.option.OptionAnnotatedSource;
import net.coderbot.iris.shaderpack.option.OptionSet;
import net.coderbot.iris.shaderpack.option.OptionValues;
import net.coderbot.iris.test.IrisTests;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class OptionApplyTest {
	@Test
	void testOptions() {
		ShaderPack shaderPack;

		// ensure that we can actually load the shader pack
		try {
			shaderPack = new ShaderPack(IrisTests.getTestShaderPackPath("options"));
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack options", e);
			return;
		}

		ProgramSource basic = shaderPack
				.getProgramSet(DimensionId.OVERWORLD)
				.getGbuffersBasic()
				.orElseThrow(RuntimeException::new);

		String basicVsh = basic.getVertexSource().orElseThrow(RuntimeException::new);
		String basicFsh = basic.getFragmentSource().orElseThrow(RuntimeException::new);

		OptionAnnotatedSource basicVshAnnotated = new OptionAnnotatedSource(basicVsh);
		OptionAnnotatedSource basicFshAnnotated = new OptionAnnotatedSource(basicFsh);

		// TODO: Separate includes will need more complex boolean define reference behavior
		OptionSet basicVshSet = basicVshAnnotated.getOptionSet("basic.vsh",
				basicVshAnnotated.getBooleanDefineReferences().keySet());
		OptionSet basicFshSet = basicFshAnnotated.getOptionSet("basic.fsh",
				basicFshAnnotated.getBooleanDefineReferences().keySet());

		OptionSet.Builder setBuilder = OptionSet.builder();

		setBuilder.addAll(basicVshSet);
		setBuilder.addAll(basicFshSet);

		OptionSet options = setBuilder.build();

		Map<String, String> changes = ImmutableMap.of(
				"SHADOWS", "false",
				"ambientOcclusionLevel", "0.0",
				"shadowDistance", "64",
				"ANNOYING_STUFF", "true",
				"GODRAYS", "16"
		);

		OptionValues values = new OptionValues(options, changes);

		System.out.println(basicVshAnnotated.apply(values));
		System.out.println(basicFshAnnotated.apply(values));

		System.out.println(options);
	}
}
