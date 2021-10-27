package net.coderbot.iris.uniforms.custom;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import kroppeb.stareval.function.Type;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.FloatSupplier;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.uniforms.custom.cached.*;
import net.coderbot.iris.vendored.joml.*;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class CustomUniformFixedInputUniformsHolder {
	final private ImmutableMap<String, CachedUniform> inputVariables;

	public CustomUniformFixedInputUniformsHolder(
			ImmutableMap<String, CachedUniform> inputVariables) {
		this.inputVariables = inputVariables;
	}

	public Type getType(String name) {
		CachedUniform uniform = this.inputVariables.get(name);
		if (uniform == null)
			return null;
		return uniform.getType();
	}

	public boolean containsKey(String name) {
		return this.inputVariables.containsKey(name);
	}

	public CachedUniform getUniform(String name) {
		return this.inputVariables.get(name);
	}

	public void updateAll() {
		for (CachedUniform value : this.inputVariables.values()) {
			value.update();
		}
	}
public Collection<CachedUniform> getAll() {
		return this.inputVariables.values();
	}

	public static class Builder implements UniformHolder {
		final private Map<String, CachedUniform> inputVariables = new Object2ObjectOpenHashMap<>();

		private Builder put(String name, CachedUniform uniform) {
			if (inputVariables.containsKey(name)) {
				Iris.logger.warn("Duplicated fixed uniform supplied as inputs to the Custom uniform holder: " + name);
				return this;
			}
			inputVariables.put(name, uniform);
			return this;
		}

		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, FloatSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, value));
		}

		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, value::getAsInt));
		}

		@Override
		public Builder uniform1f(UniformUpdateFrequency updateFrequency, String name, DoubleSupplier value) {
			return this.put(name, new FloatCachedUniform(name, updateFrequency, () -> (float) value.getAsDouble()));
		}

		@Override
		public Builder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value) {
			return this.put(name, new IntCachedUniform(name, updateFrequency, value));
		}

		@Override
		public Builder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value) {
			return this.put(name, new BooleanCachedUniform(name, updateFrequency, value));
		}

		@Override
		public Builder uniform2f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2> value) {
			Vector2f held = new Vector2f();
			return this.put(name, new Float2VectorCachedUniform(name, updateFrequency, () -> {
				Vec2 vec = value.get();
				held.set(vec.x, vec.y);
				return held;
			}));
		}

		@Override
		public Builder uniform2i(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec2> value) {
			Vector2i held = new Vector2i();
			return this.put(name, new Int2VectorCachedUniform(name, updateFrequency, () ->{
				Vec2 vec = value.get();
				held.set((int)vec.x, (int)vec.y);
				return held;
			}));
		}

		@Override
		public Builder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<com.mojang.math.Vector3f> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () -> {
				com.mojang.math.Vector3f vec = value.get();
				held.set(vec.x(), vec.y(), vec.z());
				return held;
			}));
		}

		@Override
		public Builder uniformTruncated3f(UniformUpdateFrequency updateFrequency, String name, Supplier<com.mojang.math.Vector4f> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () -> {
				com.mojang.math.Vector4f vec = value.get();
				held.set(vec.x(), vec.y(), vec.z());
				return held;
			}));
		}

		@Override
		public UniformHolder uniform3d(UniformUpdateFrequency updateFrequency, String name, Supplier<Vec3> value) {
			Vector3f held = new Vector3f();
			return this.put(name, new Float3VectorCachedUniform(name, updateFrequency, () -> {
				Vec3 vec = value.get();
				held.set(vec.x(), vec.y(), vec.z());
				return held;
			}));
		}

		@Override
		public UniformHolder uniform4f(UniformUpdateFrequency updateFrequency, String name, Supplier<com.mojang.math.Vector4f> value) {
			Vector4f held = new Vector4f();
			return this.put(name, new Float4VectorCachedUniform(name, updateFrequency, () -> {
				com.mojang.math.Vector4f vec = value.get();
				held.set(vec.x(), vec.y(), vec.z(), vec.w());
				return held;
			}));
		}

		@Override
		public UniformHolder uniformMatrix(
				UniformUpdateFrequency updateFrequency, String name, Supplier<com.mojang.math.Matrix4f> value) {
			Matrix4f held = new Matrix4f();

			FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
			return this.put(name, new Float4MatrixCachedUniform(name, updateFrequency, () -> {
				value.get().store(buffer);
				held.set(buffer);
				return held;
			}));
		}

		@Override
		public UniformHolder uniformJomlMatrix(
				UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4f> value) {
			return this.put(name, new Float4MatrixCachedUniform(name, updateFrequency, value));
		}

		@Override
		public UniformHolder uniformMatrixFromArray(
				UniformUpdateFrequency updateFrequency, String name, Supplier<float[]> value) {
			Matrix4f held = new Matrix4f();

			return this.put(name, new Float4MatrixCachedUniform(name, updateFrequency, () -> {
				held.set(value.get());
				return held;
			}));
		}

		@Override
		public UniformHolder externallyManagedUniform(String name, UniformType type) {
			// TODO: what is this function for?
			return this;
		}

		public CustomUniformFixedInputUniformsHolder build() {
			return new CustomUniformFixedInputUniformsHolder(ImmutableMap.copyOf(this.inputVariables));
		}
	}
}
