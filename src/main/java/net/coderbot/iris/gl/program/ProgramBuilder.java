package net.coderbot.iris.gl.program;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.mojang.blaze3d.systems.RenderSystem;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlShader;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.coderbot.iris.gl.GlShaders.createFromResource;

public class ProgramBuilder extends ProgramUniforms.Builder {
	private final GlProgram program;

	private ProgramBuilder(String name, GlProgram program) {
		super(name, program.getProgramRef());

		this.program = program;
	}

	public static ProgramBuilder begin(String name, @Nullable String vertexSource, @Nullable String fragmentSource) throws IOException {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);

		GlShader vertex;
		GlShader fragment;

		try {
			InputStream vertexSourceStream = new ByteArrayInputStream(checkNotNull(vertexSource).getBytes(StandardCharsets.UTF_8));
			vertex = createFromResource(GlShader.Type.VERTEX, name + ".vsh", vertexSourceStream, "iris");
		} catch (IOException e) {
			throw new IOException("Failed to compile vertex shader for program " + name, e);
		}

		try {
			InputStream fragmentSourceStream = new ByteArrayInputStream(checkNotNull(fragmentSource).getBytes(StandardCharsets.UTF_8));
			fragment = createFromResource(GlShader.Type.FRAGMENT, name + ".fsh", fragmentSourceStream, "iris");
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

	public Program build() {
		return new Program(program, super.buildUniforms());
	}

}
