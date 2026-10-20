package net.irisshaders.iris.uniforms.buffer;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.gl.state.ValueUpdateNotifier;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.gl.uniform.FloatSupplier;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.MatrixType;
import net.irisshaders.iris.parsing.VectorType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.Std140Type;
import net.irisshaders.iris.uniforms.custom.cached.CachedUniform;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class UniformValues implements DynamicUniformHolder, AutoCloseable {
	private final Map<String, Update> updates = new LinkedHashMap<>();
	private final Set<String> external = new HashSet<>();
	private ShaderResourceLayout layout;
	private ByteBuffer data;

	public UniformValues(ShaderResourceLayout layout) {
		this.layout = layout;
		this.data = MemoryUtil.memAlloc(layout.uniformBufferSize());
		MemoryUtil.memSet(this.data, 0);
	}

	public ShaderResourceLayout getLayout() {
		return this.layout;
	}

	public boolean isRegistered(String name) {
		return this.updates.containsKey(name) || this.external.contains(name);
	}

	public Set<String> registeredUniforms() {
		var names = new HashSet<>(this.updates.keySet());
		names.addAll(this.external);
		return Set.copyOf(names);
	}

	private static ShaderResourceLayout.Uniform storageUniform(ShaderResourceLayout layout, String name) {
		return layout.uniforms().get(name);
	}

	protected int uniformSize(Std140Type type) {
		return type.size();
	}

	public void resolveLayout(ShaderResourceLayout resolved) {
		if (this.layout.uniforms().equals(resolved.uniforms()) && this.layout.uniformBufferSize() == resolved.uniformBufferSize()) {
			return;
		}

		var replacement = MemoryUtil.memAlloc(resolved.uniformBufferSize());
		try {
			MemoryUtil.memSet(replacement, 0);
			for (var name : resolved.uniforms().keySet()) {
				var uniform = storageUniform(resolved, name);
				var previous = storageUniform(this.layout, name);
				if (previous != null) {
					if (!previous.type().equals(uniform.type())) {
						throw new IllegalArgumentException("uniform type changed: " + name);
					}
					replacement.put(uniform.offset(), this.data, previous.offset(), this.uniformSize(uniform.type()));
				}
			}

			for (var name : this.updates.keySet()) {
				if (storageUniform(resolved, name) == null) {
					throw new IllegalArgumentException("registered uniform removed: " + name);
				}
			}
		} catch (RuntimeException | Error failure) {
			MemoryUtil.memFree(replacement);
			throw failure;
		}

		MemoryUtil.memFree(this.data);
		this.layout = resolved;
		this.data = replacement;
	}

	@Override
	public void close() {
		MemoryUtil.memFree(this.data);
		this.data = null;
	}

	public boolean bindCachedUniform(CachedUniform uniform) {
		String name = uniform.getName();

		if (storageUniform(this.layout, name) == null) {
			return false;
		}

		FunctionReturn value = new FunctionReturn();
		Type type = uniform.getType();

		if (type.equals(Type.Boolean)) {
			this.uniform1b(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return value.booleanReturn;
			});
		} else if (type.equals(Type.Int)) {
			this.uniform1i(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return value.intReturn;
			});
		} else if (type.equals(Type.Float)) {
			this.uniform1f(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return value.floatReturn;
			});
		} else if (type.equals(VectorType.VEC2)) {
			this.uniform2f(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Vector2f) value.objectReturn;
			});
		} else if (type.equals(VectorType.VEC3)) {
			this.uniform3f(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Vector3f) value.objectReturn;
			});
		} else if (type.equals(VectorType.VEC4)) {
			this.uniform4f(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Vector4f) value.objectReturn;
			});
		} else if (type.equals(VectorType.I_VEC2)) {
			this.uniform2i(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Vector2i) value.objectReturn;
			});
		} else if (type.equals(VectorType.I_VEC3)) {
			this.uniform3i(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Vector3i) value.objectReturn;
			});
		} else if (type.equals(VectorType.I_VEC4)) {
			this.uniform4i(name, () -> {
				uniform.writeTo(value);

				return (Vector4i) value.objectReturn;
			}, null);
		} else if (type.equals(MatrixType.MAT3)) {
			this.uniformMatrix3(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Matrix3fc) value.objectReturn;
			});
		} else if (type.equals(MatrixType.MAT4)) {
			this.uniformMatrix(UniformUpdateFrequency.CUSTOM, name, () -> {
				uniform.writeTo(value);

				return (Matrix4fc) value.objectReturn;
			});
		} else {
			throw new IllegalArgumentException("unsupported cached uniform type: " + name + " " + type);
		}

		return true;
	}

	public void update(long tick, long frame) {
		for (Update update : this.updates.values()) {
			boolean due = !update.initialized || switch (update.frequency) {
				case ONCE -> false;
				case PER_TICK -> tick != update.tick;
				case PER_FRAME -> frame != update.frame;
				case CUSTOM -> true;
			};

			if (due) {
				update.writer.run();
				update.initialized = true;
				update.tick = tick;
				update.frame = frame;
			}
		}
	}

	/** Borrowed view, valid until the layout is replaced or these values are closed. */
	public ByteBuffer data() {
		return this.data.slice(0, this.layout.uniformBufferSize()).asReadOnlyBuffer().order(ByteOrder.nativeOrder());
	}

	public void set1i(String name, int value) {
		var uniform = storageUniform(this.layout, name);
		if (uniform != null) {
			this.data.putInt(uniform.offset(), value);
		}
	}

	public void set1f(String name, float value) {
		var uniform = storageUniform(this.layout, name);
		if (uniform != null) {
			this.data.putFloat(uniform.offset(), value);
		}
	}

	public void set2i(String name, int x, int y) {
		var uniform = storageUniform(this.layout, name);
		if (uniform != null) {
			this.data.putInt(uniform.offset(), x);
			this.data.putInt(uniform.offset() + 4, y);
		}
	}

	public void set4i(String name, int x, int y, int z, int w) {
		var uniform = storageUniform(this.layout, name);
		if (uniform != null) {
			this.data.putInt(uniform.offset(), x);
			this.data.putInt(uniform.offset() + 4, y);
			this.data.putInt(uniform.offset() + 8, z);
			this.data.putInt(uniform.offset() + 12, w);
		}
	}

	public void set3f(String name, float x, float y, float z) {
		var uniform = storageUniform(this.layout, name);
		if (uniform != null) {
			this.data.putFloat(uniform.offset(), x);
			this.data.putFloat(uniform.offset() + 4, y);
			this.data.putFloat(uniform.offset() + 8, z);
		}
	}

	protected UniformValues register(UniformUpdateFrequency frequency, String name, String expected, IntConsumer writer) {
		var uniform = storageUniform(this.layout, name);

		if (uniform == null) {
			return this;
		}

		this.checkType(name, expected);

		this.updates.put(name, new Update(frequency, () -> writer.accept(storageUniform(this.layout, name).offset())));

		return this;
	}

	private void checkType(String name, String expected) {
		var type = storageUniform(this.layout, name).type();

		if (!type.dimensions().isEmpty() || !(type.glsl().equals(expected) || expected.equals("int") && type.glsl().equals("bool"))) {
			throw new IllegalArgumentException("uniform " + name + " expects " + type + ", received " + expected);
		}
	}

	@Override
	public UniformValues externallyManagedUniform(String name, UniformType type) {
		this.external.add(name);

		if (storageUniform(this.layout, name) != null) {
			this.checkType(name, switch (type) {
				case INT -> "int";
				case FLOAT -> "float";
				case MAT3 -> "mat3";
				case MAT4 -> "mat4";
				case VEC2 -> "vec2";
				case VEC2I -> "ivec2";
				case VEC3 -> "vec3";
				case VEC3I -> "ivec3";
				case VEC4 -> "vec4";
				case VEC4I -> "ivec4";
			});
		}

		return this;
	}

	@Override
	public UniformValues uniform1f(UniformUpdateFrequency frequency, String name, FloatSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, value.getAsFloat());
		});
	}

	@Override
	public UniformValues uniform1f(UniformUpdateFrequency frequency, String name, IntSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, value.getAsInt());
		});
	}

	@Override
	public UniformValues uniform1f(UniformUpdateFrequency frequency, String name, DoubleSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, (float) value.getAsDouble());
		});
	}

	@Override
	public UniformValues uniform1i(UniformUpdateFrequency frequency, String name, IntSupplier value) {
		return this.register(frequency, name, "int", offset -> {
			this.data.putInt(offset, value.getAsInt());
		});
	}

	@Override
	public UniformValues uniform1b(UniformUpdateFrequency frequency, String name, BooleanSupplier value) {
		return this.register(frequency, name, "int", offset -> {
			this.data.putInt(offset, value.getAsBoolean() ? 1 : 0);
		});
	}

	@Override
	public UniformValues uniform2f(UniformUpdateFrequency frequency, String name, Supplier<Vector2f> value) {
		return this.register(frequency, name, "vec2", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
		});
	}

	@Override
	public UniformValues uniform2i(UniformUpdateFrequency frequency, String name, Supplier<Vector2i> value) {
		return this.register(frequency, name, "ivec2", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
		});
	}

	@Override
	public UniformValues uniform3f(UniformUpdateFrequency frequency, String name, Supplier<Vector3f> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
		});
	}

	@Override
	public UniformValues uniform3i(UniformUpdateFrequency frequency, String name, Supplier<Vector3i> value) {
		return this.register(frequency, name, "ivec3", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
			this.data.putInt(offset + 8, vector.z);
		});
	}

	@Override
	public UniformValues uniform4f(UniformUpdateFrequency frequency, String name, Supplier<Vector4f> value) {
		return this.register(frequency, name, "vec4", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
			this.data.putFloat(offset + 12, vector.w);
		});
	}

	@Override
	public UniformValues uniformTruncated3f(UniformUpdateFrequency frequency, String name, Supplier<Vector4f> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
		});
	}

	@Override
	public UniformValues uniform3d(UniformUpdateFrequency frequency, String name, Supplier<Vector3d> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, (float) vector.x);
			this.data.putFloat(offset + 4, (float) vector.y);
			this.data.putFloat(offset + 8, (float) vector.z);
		});
	}

	@Override
	public UniformValues uniform4fArray(UniformUpdateFrequency frequency, String name, Supplier<float[]> value) {
		return this.register(frequency, name, "vec4", offset -> {
			float[] vector = value.get();

			if (vector.length < 4) {
				throw new IllegalArgumentException("expected four components");
			}

			for (int i = 0; i < 4; i++) {
				this.data.putFloat(offset + i * 4, vector[i]);
			}
		});
	}

	@Override
	public UniformValues uniformMatrix(UniformUpdateFrequency frequency, String name, Supplier<Matrix4fc> value) {
		return this.register(frequency, name, "mat4", offset -> {
			var matrix = value.get();

			for (int column = 0; column < 4; column++) {
				for (int row = 0; row < 4; row++) {
					this.data.putFloat(offset + column * 16 + row * 4, matrix.get(column, row));
				}
			}
		});
	}

	@Override
	public UniformValues uniformMatrixFromArray(UniformUpdateFrequency frequency, String name, Supplier<float[]> value) {
		return this.register(frequency, name, "mat4", offset -> {
			float[] matrix = value.get();

			if (matrix.length < 16) {
				throw new IllegalArgumentException("expected sixteen matrix components");
			}

			for (int i = 0; i < 16; i++) {
				this.data.putFloat(offset + i * 4, matrix[i]);
			}
		});
	}

	@Override
	public UniformValues uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1i(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform2f(String name, Supplier<Vector2f> value, ValueUpdateNotifier notifier) {
		return this.uniform2f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		return this.uniform2i(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform3f(String name, Supplier<Vector3f> value, ValueUpdateNotifier notifier) {
		return this.uniform3f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		return this.uniform4f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform4fArray(String name, Supplier<float[]> value, ValueUpdateNotifier notifier) {
		return this.uniform4fArray(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniformMatrix(String name, Supplier<Matrix4fc> value, ValueUpdateNotifier notifier) {
		return this.uniformMatrix(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniform4i(String name, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		return this.register(UniformUpdateFrequency.CUSTOM, name, "ivec4", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
			this.data.putInt(offset + 8, vector.z);
			this.data.putInt(offset + 12, vector.w);
		});
	}

	@Override
	public UniformValues uniformMatrix3(String name, Supplier<Matrix3fc> value, ValueUpdateNotifier notifier) {
		return this.uniformMatrix3(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public UniformValues uniformMatrix3(UniformUpdateFrequency frequency, String name, Supplier<Matrix3fc> value) {
		return this.register(frequency, name, "mat3", offset -> {
			var matrix = value.get();

			for (int column = 0; column < 3; column++) {
				for (int row = 0; row < 3; row++) {
					this.data.putFloat(offset + column * 16 + row * 4, matrix.get(column, row));
				}
			}
		});
	}

	public static class Update {
		private final UniformUpdateFrequency frequency;
		private final Runnable writer;
		private boolean initialized;
		private long tick;
		private long frame;

		private Update(UniformUpdateFrequency frequency, Runnable writer) {
			this.frequency = frequency;
			this.writer = writer;
		}
	}
}
