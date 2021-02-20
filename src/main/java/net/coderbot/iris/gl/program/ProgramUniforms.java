package net.coderbot.iris.gl.program;

import java.nio.IntBuffer;
import java.util.*;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
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

	public static class Builder implements UniformHolder {
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
			int id = GL20C.glGetUniformLocation(program, name);

			if (id == -1) {
				return OptionalInt.empty();
			}

			uniformNames.add(name);

			// TODO: Make these debug messages less spammy, or toggleable
			Iris.logger.info("[" + this.name + "] Activating uniform: " + name);
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
					Iris.logger.warn("[" + this.name + "] Unsupported uniform: " + name + " of size " + size + " and type " + type);
				}
			}

			return new ProgramUniforms(ImmutableList.copyOf(once), ImmutableList.copyOf(perTick), ImmutableList.copyOf(perFrame));
		}
	}
}
