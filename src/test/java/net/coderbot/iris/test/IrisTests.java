package net.coderbot.iris.test;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.coderbot.iris.shaderpack.StringPair;
import org.junit.jupiter.api.Assertions;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IrisTests {
	public static ShaderPack loadPackOrFail(String name) {
		try {
			return new ShaderPack(IrisTests.getTestShaderPackPath(name), TEST_ENVIRONMENT_DEFINES);
		} catch (Exception e) {
			Assertions.fail("Couldn't load test shader pack " + name, e);
			throw new AssertionError();
		}
	}

	public static Path getTestShaderPackPath(String name) {
		try {
			return Paths.get(IrisTests.class.getResource("/shaderpacks/" + name + "/shaders/").toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static ImmutableList<StringPair> TEST_ENVIRONMENT_DEFINES = ImmutableList.of(
		new StringPair("MC_OS_WINDOWS", ""),
		new StringPair("MC_VERSION", "11605"),
		new StringPair("MC_GL_VERSION", "460"),
		new StringPair("MC_GLSL_VERSION", "460"),
		new StringPair("MC_GL_RENDERER_GEFORCE", ""),
		new StringPair("MC_GL_VENDOR_NVIDIA", ""),
		new StringPair("MC_RENDER_QUALITY", "1.0"),
		new StringPair("MC_SHADOW_QUALITY", "1.0"),
		new StringPair("MC_NORMAL_MAP", ""),
		new StringPair("MC_SPECULAR_MAP", ""),
		new StringPair("MC_HAND_DEPTH", "0.125")
	);
}
