// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

public class ProgramCreator {
	private static final Logger LOGGER = LogManager.getLogger(ProgramCreator.class);

	public static int create(String name, GlShader... shaders) {
		int program = GL20C.glCreateProgram();

		// TODO: This is *really* hardcoded, we need to refactor this to support external calls
		// to glBindAttribLocation
		GL20C.glBindAttribLocation(program, 10, "mc_Entity");
		GL20C.glBindAttribLocation(program, 11, "mc_midTexCoord");
		GL20C.glBindAttribLocation(program, 12, "at_tangent");

		for (GlShader shader : shaders) {
			GL20C.glAttachShader(program, shader.getHandle());
		}

		GL20C.glLinkProgram(program);

		String log = GL20C.glGetProgramInfoLog(program);

		if (!log.isEmpty()) {
			LOGGER.warn("Program link log for " + name + ": " + log);
		}

		int result = GL20C.glGetProgrami(program, GL20C.GL_LINK_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		return program;
	}
}
