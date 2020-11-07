package net.coderbot.iris.gl.program;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

public class ProgramBuilder implements UniformHolder {
	private final GlProgram program;
	private final List<Uniform> once;
	private final List<Uniform> perTick;
	private final List<Uniform> perFrame;

	private ProgramBuilder(GlProgram program) {
		this.program = program;

		once = new ArrayList<>();
		perTick = new ArrayList<>();
		perFrame = new ArrayList<>();
	}

	public static ProgramBuilder begin(String name, InputStream vertexSource, InputStream fragmentSource) throws IOException {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader vertex;
		GlShader fragment;

		try {
			vertex = GlShader.createFromResource(GlShader.Type.VERTEX, name + ".vsh", vertexSource, "iris");
		} catch (IOException e) {
			throw new IOException("Failed to compile vertex shader for program " + name, e);
		}

		try {
			fragment = GlShader.createFromResource(GlShader.Type.FRAGMENT, name + ".fsh", fragmentSource, "iris");
		} catch (IOException e) {
			throw new IOException("Failed to compile fragment shader for program " + name, e);
		}

		int programId;

		try {
			programId = GlProgramManager.createProgram();
		} catch (IOException e) {
			e.printStackTrace();
			programId = 0;
		}

		final int finalProgramId = programId;

		GlProgram program = new GlProgram() {
			@Override
			public int getProgramRef() {
				return finalProgramId;
			}

			@Override
			public void markUniformsDirty() {
				// nah
			}

			@Override
			public GlShader getVertexShader() {
				return vertex;
			}

			@Override
			public GlShader getFragmentShader() {
				return fragment;
			}
		};

		try {
			GlProgramManager.linkProgram(program);
		} catch (IOException e) {
			e.printStackTrace();
		}

		vertex.release();
		fragment.release();

		return new ProgramBuilder(program);
	}

	@Override
	public UniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		switch (updateFrequency) {
			case ONCE:
				once.add(uniform);
				break;
			case PER_TICK:
				perTick.add(uniform);
				break;
			case PER_FRAME:
				perFrame.add(uniform);
				break;
		}

		return this;
	}

	@Override
	public int location(String name) {
		return GL21.glGetUniformLocation(program.getProgramRef(), name);
	}

	public Program build() {
		return new Program(program, ImmutableList.copyOf(once), ImmutableList.copyOf(perTick), ImmutableList.copyOf(perFrame));
	}
}
