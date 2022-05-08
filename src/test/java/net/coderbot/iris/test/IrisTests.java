package net.coderbot.iris.test;

import com.google.common.collect.ImmutableList;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IrisTests {
	public static Path getTestShaderPackPath(String name) {
		try {
			return Paths.get(IrisTests.class.getResource("/shaderpacks/" + name + "/shaders/").toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static ImmutableList<String> TEST_ENVIRONMENT_DEFINES = ImmutableList.of(
		"#define MC_OS_WINDOWS",
		"#define MC_VERSION 11605",
		"#define MC_GL_VERSION 460",
		"#define MC_GLSL_VERSION 460",
		"#define MC_GL_RENDERER_GEFORCE",
		"#define MC_GL_VENDOR_NVIDIA",
		"#define MC_RENDER_QUALITY 1.0",
		"#define MC_SHADOW_QUALITY 1.0",
		"#define MC_NORMAL_MAP",
		"#define MC_SPECULAR_MAP",
		"#define MC_HAND_DEPTH 0.125"
	);
}
