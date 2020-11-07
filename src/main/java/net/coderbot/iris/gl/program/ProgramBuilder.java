package net.coderbot.iris.gl.program;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

public class ProgramBuilder implements UniformHolder {
	private final String name;
	private final GlProgram program;
	private final List<Uniform> once;
	private final List<Uniform> perTick;
	private final List<Uniform> perFrame;

	private ProgramBuilder(String name, GlProgram program) {
		this.name = name;
		this.program = program;

		once = new ArrayList<>();
		perTick = new ArrayList<>();
		perFrame = new ArrayList<>();
	}

	public static ProgramBuilder begin(String name, @Nullable String vertexSource, @Nullable String fragmentSource) throws IOException {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader vertex;
		GlShader fragment;

		try {
			InputStream vertexSourceStream = new ByteArrayInputStream(vertexSource.getBytes(StandardCharsets.UTF_8));
			vertex = GlShader.createFromResource(GlShader.Type.VERTEX, name + ".vsh", vertexSourceStream, "iris");
		} catch (IOException e) {
			throw new IOException("Failed to compile vertex shader for program " + name, e);
		}

		try {
			InputStream fragmentSourceStream = new ByteArrayInputStream(fragmentSource.getBytes(StandardCharsets.UTF_8));
			fragment = GlShader.createFromResource(GlShader.Type.FRAGMENT, name + ".fsh", fragmentSourceStream, "iris");
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

		return new ProgramBuilder(name, program);
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
	public OptionalInt location(String name) {
		// TODO: Make these debug messages less spammy, or toggleable
		int id = GL21.glGetUniformLocation(program.getProgramRef(), name);

		if (id == -1) {
			System.out.println("[" + this.name + "] Skipping uniform:   " + name);
			return OptionalInt.empty();
		}

		System.out.println("[" + this.name + "] Activating uniform: " + name);
		return OptionalInt.of(id);
	}

	public Program build() {
		return new Program(program, ImmutableList.copyOf(once), ImmutableList.copyOf(perTick), ImmutableList.copyOf(perFrame));
	}
}
