// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.program;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

import net.coderbot.iris.gl.GlObject;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.shader.Shader;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.uniform.Uniform;

public class Program extends GlObject {
	private static final Logger LOGGER = LogManager.getLogger(Program.class);

	private final ProgramUniforms uniforms;
	private final ProgramSamplers samplers;
	private final ProgramImages images;

	public Program(Shader[] shaders) {
		int program = GL20C.glCreateProgram();

		this.setHandle(program);

		for (Shader shader : shaders) {
			GL20C.glAttachShader(program, shader.getHandle());
		}

		GL20C.glLinkProgram(program);

		//Always detach shaders according to https://www.khronos.org/opengl/wiki/Shader_Compilation#Cleanup
		for (Shader shader : shaders) {
			GL20C.glDetachShader(program, shader.getHandle());
		}

		String log = IrisRenderSystem.getProgramInfoLog(program);

		if (!log.isEmpty()) {
			LOGGER.warn("Program link log: " + log);
		}

		int result = GlStateManager.glGetProgrami(program, GL20C.GL_LINK_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}
	}

	public void bind() {
		ProgramManager.glUseProgram(getHandle());
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();

		ProgramManager.glUseProgram(0);
	}

	public void delete() {
		GlStateManager.glDeleteProgram(getHandle());

		this.invalidateHandle();
	}

	public int getActiveImages() {
		return images.getActiveImages();
	}
}
