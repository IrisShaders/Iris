package net.coderbot.iris.gl.program;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlProgram;

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

		protected Builder(String name, int program) {
			this.name = name;
			this.program = program;

			once = new ArrayList<>();
			perTick = new ArrayList<>();
			perFrame = new ArrayList<>();
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
			int id = GL21.glGetUniformLocation(program, name);

			if (id == -1) {
				return OptionalInt.empty();
			}

			// TODO: Make these debug messages less spammy, or toggleable
			Iris.logger.info("[" + this.name + "] Activating uniform: " + name);
			return OptionalInt.of(id);
		}

		public ProgramUniforms buildUniforms() {
			return new ProgramUniforms(ImmutableList.copyOf(once), ImmutableList.copyOf(perTick), ImmutableList.copyOf(perFrame));
		}
	}
}
