package net.coderbot.iris.gl.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL21;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public class ProgramUniforms {
	private final int program;
	private final List<Uniform> once;
	private final List<Uniform> perTick;
	private final List<Uniform> perFrame;

	private long lastTick = -1;

	public ProgramUniforms(int program) {
		this.program = program;

		once = new ArrayList<>();
		perTick = new ArrayList<>();
		perFrame = new ArrayList<>();
	}

	private ProgramUniforms addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
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

	private int location(String name) {
		return GL21.glGetUniformLocation(program, name);
	}

	public ProgramUniforms uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), value));
	}

	public ProgramUniforms uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsInt()));
	}

	public ProgramUniforms uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		return addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsDouble()));
	}

	public ProgramUniforms uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		return addUniform(updateFrequency, new IntUniform(location(name), value));
	}

	public ProgramUniforms uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		return addUniform(updateFrequency, new BooleanUniform(location(name), value));
	}

	public ProgramUniforms uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value) {
		return addUniform(updateFrequency, new Vector3Uniform(location(name), value));
	}

	public ProgramUniforms uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		return addUniform(updateFrequency, Vector3Uniform.truncated(location(name), value));
	}

	public ProgramUniforms uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3d> value) {
		return addUniform(updateFrequency, Vector3Uniform.converted(location(name), value));
	}

	public ProgramUniforms uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
		return addUniform(updateFrequency, new MatrixUniform(location(name), value));
	}

	public void tick() {
		for (Uniform uniform : perTick) {
			uniform.update();
		}
	}

	public void update() {
		if (!once.isEmpty()) {
			for (Uniform uniform : once) {
				uniform.update();
			}

			once.clear();
		}

		long currentTick = Objects.requireNonNull(MinecraftClient.getInstance().world).getTime();

		if (lastTick != currentTick) {
			lastTick = currentTick;

			tick();
		}

		for (Uniform uniform : perFrame) {
			uniform.update();
		}
	}
}
