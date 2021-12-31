// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.program;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

import net.coderbot.iris.gl.GlObject;
import net.coderbot.iris.gl.image.ImageHolder;
import net.coderbot.iris.gl.sampler.SamplerHolder;
import net.coderbot.iris.gl.shader.Shader;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.uniform.Uniform;

public class Program extends GlObject implements SamplerHolder, ImageHolder {
	private static final Logger LOGGER = LogManager.getLogger(Program.class);

	private final ProgramSamplers.Builder samplers;
	private final ProgramImages.Builder images;

	public Program(Shader[] shaders, ImmutableSet<Integer> reservedTextureUnits) {
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

		String log = GL20C.glGetProgramInfoLog(program);

		if (!log.isEmpty()) {
			LOGGER.warn("Program link log: " + log);
		}

		int result = GlStateManager.glGetProgrami(program, GL20C.GL_LINK_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader program linking failed, see log for details");
		}

		this.samplers = ProgramSamplers.builder(program, reservedTextureUnits);
		this.images = ProgramImages.builder(program);
	}

	public void bind() {
		GL20C.glUseProgram(getHandle());

		samplers.update();
		images.update();
	}

	public static void unbind() {
		ProgramUniforms.clearActiveUniforms();

		GL20C.glUseProgram(0);
	}

	public void delete() {
		GlStateManager.glDeleteProgram(getHandle());

		this.invalidateHandle();
	}

	@Override
	public boolean hasImage(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addTextureImage(IntSupplier textureID, InternalTextureFormat internalFormat, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addExternalSampler(int textureUnit, String... names) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean hasSampler(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDefaultSampler(IntSupplier sampler, String... names) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDynamicSampler(IntSupplier sampler, String... names) {
		// TODO Auto-generated method stub
		return false;
	}
}
