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

	private void addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
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
	}

	private int location(String name) {
		return GL21.glGetUniformLocation(program, name);
	}

	public void uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
		addUniform(updateFrequency, new FloatUniform(location(name), value));
	}

	public void uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsInt()));
	}

	public void uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
		addUniform(updateFrequency, new FloatUniform(location(name), () -> (float) value.getAsDouble()));
	}

	public void uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
		addUniform(updateFrequency, new IntUniform(location(name), value));
	}

	public void uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
		addUniform(updateFrequency, new BooleanUniform(location(name), value));
	}

	public void uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value) {
		addUniform(updateFrequency, new Vector3Uniform(location(name), value));
	}

	public void uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector4f> value) {
		addUniform(updateFrequency, Vector3Uniform.truncated(location(name), value));
	}

	public void uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3d> value) {
		addUniform(updateFrequency, Vector3Uniform.converted(location(name), value));
	}

	public void uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
		addUniform(updateFrequency, new MatrixUniform(location(name), value));
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
