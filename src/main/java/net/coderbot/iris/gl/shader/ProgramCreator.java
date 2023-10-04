// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GLDebug;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.KHRDebug;

public class ProgramCreator {
	private static final Logger LOGGER = LogManager.getLogger(ProgramCreator.class);

	public static int create(String name, GlShader... shaders) {
		int program = GlStateManager.glCreateProgram();

		GlStateManager._glBindAttribLocation(program, 11, "iris_Entity");
		GlStateManager._glBindAttribLocation(program, 11, "mc_Entity");
		GlStateManager._glBindAttribLocation(program, 12, "mc_midTexCoord");
		GlStateManager._glBindAttribLocation(program, 13, "at_tangent");
		GlStateManager._glBindAttribLocation(program, 14, "at_midBlock");

		GlStateManager._glBindAttribLocation(program, 0, "Position");
		GlStateManager._glBindAttribLocation(program, 1, "UV0");

		for (GlShader shader : shaders) {
			GlStateManager.glAttachShader(program, shader.getHandle());
		}

		GlStateManager.glLinkProgram(program);

		GLDebug.nameObject(KHRDebug.GL_PROGRAM, program, name);

		//Always detach shaders according to https://www.khronos.org/opengl/wiki/Shader_Compilation#Cleanup
        for (GlShader shader : shaders) {
            IrisRenderSystem.detachShader(program, shader.getHandle());
        }

		String log = IrisRenderSystem.getProgramInfoLog(program);

		if (!log.isEmpty()) {
			LOGGER.warn("Program link log for " + name + ": " + log);
		}

		int result = GlStateManager.glGetProgrami(program, GL20C.GL_LINK_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new ShaderCompileException(name, log);
		}

		return program;
	}
}
