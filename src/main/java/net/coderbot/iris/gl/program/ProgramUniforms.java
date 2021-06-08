package net.coderbot.iris.gl.program;

import java.nio.IntBuffer;
import java.util.*;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.SystemTimeUniforms;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20C;

import net.minecraft.client.MinecraftClient;

public class ProgramUniforms {
	private final ImmutableList<Uniform> perTick;
	private final ImmutableList<Uniform> perFrame;

	private ImmutableList<Uniform> once;
	long lastTick = -1;
	int lastFrame = -1;

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

		// TODO: Move the frame counter to a different place?
		int currentFrame = SystemTimeUniforms.COUNTER.getAsInt();

		if (lastFrame != currentFrame) {
			lastFrame = currentFrame;

			updateStage(perFrame);
		}
	}

	public static Builder builder(String name, int program) {
		return new Builder(name, program);
	}

	public static class Builder implements LocationalUniformHolder {
		private final String name;
		private final int program;

		private final Map<Integer, String> locations;
		private final Map<String, Uniform> once;
		private final Map<String, Uniform> perTick;
		private final Map<String, Uniform> perFrame;
		private final Map<String, UniformType> uniformNames;

		protected Builder(String name, int program) {
			this.name = name;
			this.program = program;

			locations = new HashMap<>();
			once = new HashMap<>();
			perTick = new HashMap<>();
			perFrame = new HashMap<>();
			uniformNames = new HashMap<>();
		}

		@Override
		public Builder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
			switch (updateFrequency) {
				case ONCE:
					once.put(locations.get(uniform.getLocation()), uniform);
					break;
				case PER_TICK:
					perTick.put(locations.get(uniform.getLocation()), uniform);
					break;
				case PER_FRAME:
					perFrame.put(locations.get(uniform.getLocation()), uniform);
					break;
			}

			return this;
		}

		@Override
		public OptionalInt location(String name, UniformType type) {
			int id = GL20C.glGetUniformLocation(program, name);

			if (id == -1) {
				return OptionalInt.empty();
			}

			locations.put(id, name);
			uniformNames.put(name, type);

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

				UniformType provided = uniformNames.get(name);
				UniformType expected = getExpectedType(type);

				if (provided == null && !name.startsWith("gl_")) {
					String typeName = getTypeName(type);

					if (size == 1) {
						Iris.logger.warn("[" + this.name + "] Unsupported uniform: " + typeName + " " + name);
					} else {
						Iris.logger.warn("[" + this.name + "] Unsupported uniform: " + name + " of size " + size + " and type " + typeName);
					}
				}

				// TODO: This is an absolutely horrific hack, but is needed until custom uniforms work.
				if ("framemod8".equals(name) && expected == UniformType.FLOAT && provided == UniformType.INT) {
					SystemTimeUniforms.addFloatFrameMod8Uniform(this);
					provided = UniformType.FLOAT;
				}

				if (provided != null && provided != expected) {
					String expectedName;

					if (expected != null) {
						expectedName = expected.toString();
					} else {
						expectedName = "(unsupported type: " + getTypeName(type) + ")";
					}

					Iris.logger.error("[" + this.name + "] Wrong uniform type for " + name + ": Iris is providing " + provided + " but the program expects " + expectedName + ". Disabling that uniform.");

					once.remove(name);
					perTick.remove(name);
					perFrame.remove(name);
				}
			}

			return new ProgramUniforms(ImmutableList.copyOf(once.values()), ImmutableList.copyOf(perTick.values()), ImmutableList.copyOf(perFrame.values()));
		}
	}

	private static String getTypeName(int type) {
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

		return typeName;
	}

	private static UniformType getExpectedType(int type) {
		if (type == GL20C.GL_FLOAT) {
			return UniformType.FLOAT;
		} else if (type == GL20C.GL_INT) {
			return UniformType.INT;
		} else if (type == GL20C.GL_FLOAT_MAT4) {
			return UniformType.MAT4;
		} else if (type == GL20C.GL_FLOAT_VEC4) {
			return UniformType.VEC4;
		} else if (type == GL20C.GL_INT_VEC4) {
			return null;
		} else if (type == GL20C.GL_FLOAT_MAT3) {
			return null;
		} else if (type == GL20C.GL_FLOAT_VEC3) {
			return UniformType.VEC3;
		} else if (type == GL20C.GL_INT_VEC3) {
			return null;
		} else if (type == GL20C.GL_FLOAT_MAT2) {
			return null;
		} else if (type == GL20C.GL_FLOAT_VEC2) {
			return UniformType.VEC2;
		} else if (type == GL20C.GL_INT_VEC2) {
			return UniformType.VEC2I;
		} else if (type == GL20C.GL_SAMPLER_3D) {
			return UniformType.INT;
		} else if (type == GL20C.GL_SAMPLER_2D) {
			return UniformType.INT;
		} else if (type == GL20C.GL_SAMPLER_1D) {
			return UniformType.INT;
		} else if (type == GL20C.GL_SAMPLER_2D_SHADOW) {
			return UniformType.INT;
		} else if (type == GL20C.GL_SAMPLER_1D_SHADOW) {
			return UniformType.INT;
		} else {
			return null;
		}
	}
}
