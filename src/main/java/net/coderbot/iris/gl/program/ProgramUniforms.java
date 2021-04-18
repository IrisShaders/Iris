package net.coderbot.iris.gl.program;

import java.nio.IntBuffer;
import java.util.*;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20C;

import net.minecraft.client.MinecraftClient;


public class ProgramUniforms {
	private final ImmutableList<Uniform> perTick;
	private final ImmutableList<Uniform> perFrame;

	private ImmutableList<Uniform> once;
	long lastTick = -1;

	public ProgramUniforms(ImmutableList<Uniform> once, ImmutableList<Uniform> perTick, ImmutableList<Uniform> perFrame) {
		this.once = once;
		this.perTick = perTick;
		this.perFrame = perFrame;
	}

	private void updateStage(ImmutableList<Uniform> uniforms) {
		for (Uniform uniform : uniforms) {
			uniform.update();
		}
	}

	private static long getCurrentTick() {
		return Objects.requireNonNull(MinecraftClient.getInstance().world).getTime();
	}

	public void update() {
		if (once != null) {
			updateStage(once);
			updateStage(perTick);
			updateStage(perFrame);
			lastTick = getCurrentTick();

			once = null;
			return;
		}

		long currentTick = getCurrentTick();

		if (lastTick != currentTick) {
			lastTick = currentTick;

			updateStage(perTick);
		}

		updateStage(perFrame);
	}

	public static Builder builder(String name, int program) {
		return new Builder(name, program);
	}

	public static class Builder implements LocationalUniformHolder {
		private final String name;
		private final int program;

		private final List<Uniform> once;
		private final List<Uniform> perTick;
		private final List<Uniform> perFrame;
		private final Set<String> uniformNames;

		protected Builder(String name, int program) {
			this.name = name;
			this.program = program;

			once = new ArrayList<>();
			perTick = new ArrayList<>();
			perFrame = new ArrayList<>();
			uniformNames = new HashSet<>();
		}

		@Override
		public Builder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
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
			int id = GL20C.glGetUniformLocation(program, name);

			if (id == -1) {
				return OptionalInt.empty();
			}

			uniformNames.add(name);

			return OptionalInt.of(id);
		}

		public ProgramUniforms buildUniforms() {
			// Check for any unsupported uniforms and warn about them so that we can easily figure out what uniforms we
			// need to add.
			int activeUniforms = GL20C.glGetProgrami(program, GL20C.GL_ACTIVE_UNIFORMS);
			IntBuffer sizeBuf = BufferUtils.createIntBuffer(1);
			IntBuffer typeBuf = BufferUtils.createIntBuffer(1);

			for (int index = 0; index < activeUniforms; index++) {
				String name = GL20C.glGetActiveUniform(program, index, 128, sizeBuf, typeBuf);

				int size = sizeBuf.get(0);
				int type = typeBuf.get(0);

				if (!name.startsWith("gl_") && !uniformNames.contains(name)) {
					String typeName;

					if (type == GL20C.GL_FLOAT) {
						typeName = "float";
					} else if (type == GL20C.GL_INT) {
						typeName = "int";
					} else if (type == GL20C.GL_FLOAT_MAT4) {
						typeName = "mat4";
					} else if (type == GL20C.GL_FLOAT_VEC4) {
						typeName = "vec4";
					} else if (type == GL20C.GL_FLOAT_MAT3) {
						typeName = "mat3";
					} else if (type == GL20C.GL_FLOAT_VEC3) {
						typeName = "vec3";
					} else if (type == GL20C.GL_FLOAT_MAT2) {
						typeName = "mat2";
					} else if (type == GL20C.GL_FLOAT_VEC2) {
						typeName = "vec2";
					} else if (type == GL20C.GL_SAMPLER_3D) {
						typeName = "sampler3D";
					} else if (type == GL20C.GL_SAMPLER_2D) {
						typeName = "sampler2D";
					} else if (type == GL20C.GL_SAMPLER_1D) {
						typeName = "sampler1D";
					} else if (type == GL20C.GL_SAMPLER_2D_SHADOW) {
						typeName = "sampler2DShadow";
					} else if (type == GL20C.GL_SAMPLER_1D_SHADOW) {
						typeName = "sampler1DShadow";
					} else {
						typeName = "(unknown:" + type + ")";
					}

					if (size == 1) {
						Iris.logger.warn("[" + this.name + "] Unsupported uniform: " + typeName + " " + name);
					} else {
						Iris.logger.warn("[" + this.name + "] Unsupported uniform: " + name + " of size " + size + " and type " + typeName);
					}
				}
			}

			return new ProgramUniforms(ImmutableList.copyOf(once), ImmutableList.copyOf(perTick), ImmutableList.copyOf(perFrame));
		}
	}
}
