package net.irisshaders.iris.uniforms.buffer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
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

public final class BufferUniformHolder implements DynamicUniformHolder {
	private final Map<String, Update> updates = new LinkedHashMap<>();
	private final Set<String> external = new HashSet<>();
	private ShaderResourceLayout layout;
	private ByteBuffer data;
	private FrameUniforms frameUniforms;

	public BufferUniformHolder(ShaderResourceLayout layout) {
		this.layout = layout;
		this.data = ByteBuffer.allocateDirect(layout.uniformBufferSize()).order(ByteOrder.nativeOrder());
	}

	public boolean isRegistered(String name) {
		return this.updates.containsKey(name) || this.external.contains(
			name) || this.frameUniforms != null && this.frameUniforms.getLayout().uniforms().containsKey(name);
	}

	public void useFrameUniforms(FrameUniforms uniforms) {
		this.frameUniforms = uniforms;
	}

	public FrameUniforms frameUniforms() {
		return this.frameUniforms;
	}

	public Set<String> registeredUniforms() {
		var names = new HashSet<>(this.updates.keySet());
		names.addAll(this.external);

		if (this.frameUniforms != null) {
			names.addAll(this.frameUniforms.getLayout().uniforms().keySet());
		}

		return Set.copyOf(names);
	}

	public Set<String> dynamicUniforms() {
		return Set.copyOf(this.updates.keySet());
	}

	public boolean isDynamic(String name) {
		return this.updates.containsKey(name);
	}

	public ShaderResourceLayout frameLayout() {
		return this.frameUniforms == null ? null : this.frameUniforms.getLayout();
	}

	public void resolveLayout(ShaderResourceLayout resolved) {
		if (this.layout.uniforms().equals(resolved.uniforms()) && this.layout.uniformBufferSize() == resolved.uniformBufferSize()) {
			return;
		}

		var replacement = ByteBuffer.allocateDirect(resolved.uniformBufferSize()).order(ByteOrder.nativeOrder());
		resolved.uniforms().forEach((name, uniform) -> {
			var previous = this.layout.uniforms().get(name);

			if (previous != null) {
				if (!previous.type().equals(uniform.type())) {
					throw new IllegalArgumentException("uniform type changed: " + name);
				}

				replacement.put(uniform.offset(), this.data, previous.offset(), uniform.type().size());
			}
		});

		for (var name : this.updates.keySet()) {
			if (!resolved.uniforms().containsKey(name)) {
				throw new IllegalArgumentException("registered uniform removed: " + name);
			}
		}

		this.layout = resolved;
		this.data = replacement;
	}

	public boolean bindCachedUniform(CachedUniform uniform) {
		String name = uniform.getName();

		if (!this.layout.uniforms().containsKey(name)) {
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

	public ByteBuffer data() {
		return this.data.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
	}

	public GpuBufferSlice upload(CommandEncoder encoder, int uniformOffsetAlignment) {
		if (this.data.capacity() == 0) {
			throw new IllegalStateException("this program has no loose uniforms");
		}

		if (uniformOffsetAlignment <= 0) {
			throw new IllegalArgumentException("invalid uniform offset alignment");
		}

		return encoder.transientMemory().uploadGpu(this.data(), uniformOffsetAlignment, GpuBuffer.USAGE_UNIFORM);
	}

	private BufferUniformHolder register(UniformUpdateFrequency frequency, String name, String expected, IntConsumer writer) {
		var uniform = this.layout.uniforms().get(name);

		if (uniform == null) {
			return this;
		}

		this.checkType(name, expected);

		if (this.frameUniforms != null && frequency != UniformUpdateFrequency.CUSTOM && this.frameUniforms.getLayout().uniforms()
			.containsKey(name)) {
			return this;
		}

		this.updates.put(name, new Update(frequency, () -> writer.accept(this.layout.uniforms().get(name).offset())));

		return this;
	}

	private void checkType(String name, String expected) {
		var type = this.layout.uniforms().get(name).type();

		if (!type.dimensions().isEmpty() || !(type.glsl().equals(expected) || expected.equals("int") && type.glsl().equals("bool"))) {
			throw new IllegalArgumentException("uniform " + name + " expects " + type + ", received " + expected);
		}
	}

	@Override
	public BufferUniformHolder externallyManagedUniform(String name, UniformType type) {
		this.external.add(name);

		if (this.layout.uniforms().containsKey(name)) {
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
	public BufferUniformHolder uniform1f(UniformUpdateFrequency frequency, String name, FloatSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, value.getAsFloat());
		});
	}

	@Override
	public BufferUniformHolder uniform1f(UniformUpdateFrequency frequency, String name, IntSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, value.getAsInt());
		});
	}

	@Override
	public BufferUniformHolder uniform1f(UniformUpdateFrequency frequency, String name, DoubleSupplier value) {
		return this.register(frequency, name, "float", offset -> {
			this.data.putFloat(offset, (float) value.getAsDouble());
		});
	}

	@Override
	public BufferUniformHolder uniform1i(UniformUpdateFrequency frequency, String name, IntSupplier value) {
		return this.register(frequency, name, "int", offset -> {
			this.data.putInt(offset, value.getAsInt());
		});
	}

	@Override
	public BufferUniformHolder uniform1b(UniformUpdateFrequency frequency, String name, BooleanSupplier value) {
		return this.register(frequency, name, "int", offset -> {
			this.data.putInt(offset, value.getAsBoolean() ? 1 : 0);
		});
	}

	@Override
	public BufferUniformHolder uniform2f(UniformUpdateFrequency frequency, String name, Supplier<Vector2f> value) {
		return this.register(frequency, name, "vec2", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
		});
	}

	@Override
	public BufferUniformHolder uniform2i(UniformUpdateFrequency frequency, String name, Supplier<Vector2i> value) {
		return this.register(frequency, name, "ivec2", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
		});
	}

	@Override
	public BufferUniformHolder uniform3f(UniformUpdateFrequency frequency, String name, Supplier<Vector3f> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
		});
	}

	@Override
	public BufferUniformHolder uniform3i(UniformUpdateFrequency frequency, String name, Supplier<Vector3i> value) {
		return this.register(frequency, name, "ivec3", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
			this.data.putInt(offset + 8, vector.z);
		});
	}

	@Override
	public BufferUniformHolder uniform4f(UniformUpdateFrequency frequency, String name, Supplier<Vector4f> value) {
		return this.register(frequency, name, "vec4", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
			this.data.putFloat(offset + 12, vector.w);
		});
	}

	@Override
	public BufferUniformHolder uniformTruncated3f(UniformUpdateFrequency frequency, String name, Supplier<Vector4f> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, vector.x);
			this.data.putFloat(offset + 4, vector.y);
			this.data.putFloat(offset + 8, vector.z);
		});
	}

	@Override
	public BufferUniformHolder uniform3d(UniformUpdateFrequency frequency, String name, Supplier<Vector3d> value) {
		return this.register(frequency, name, "vec3", offset -> {
			var vector = value.get();
			this.data.putFloat(offset, (float) vector.x);
			this.data.putFloat(offset + 4, (float) vector.y);
			this.data.putFloat(offset + 8, (float) vector.z);
		});
	}

	@Override
	public BufferUniformHolder uniform4fArray(UniformUpdateFrequency frequency, String name, Supplier<float[]> value) {
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
	public BufferUniformHolder uniformMatrix(UniformUpdateFrequency frequency, String name, Supplier<Matrix4fc> value) {
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
	public BufferUniformHolder uniformMatrixFromArray(UniformUpdateFrequency frequency, String name, Supplier<float[]> value) {
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
	public BufferUniformHolder uniform1f(String name, FloatSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform1f(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform1f(String name, DoubleSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform1i(String name, IntSupplier value, ValueUpdateNotifier notifier) {
		return this.uniform1i(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform2f(String name, Supplier<Vector2f> value, ValueUpdateNotifier notifier) {
		return this.uniform2f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform2i(String name, Supplier<Vector2i> value, ValueUpdateNotifier notifier) {
		return this.uniform2i(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform3f(String name, Supplier<Vector3f> value, ValueUpdateNotifier notifier) {
		return this.uniform3f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform4f(String name, Supplier<Vector4f> value, ValueUpdateNotifier notifier) {
		return this.uniform4f(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform4fArray(String name, Supplier<float[]> value, ValueUpdateNotifier notifier) {
		return this.uniform4fArray(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniformMatrix(String name, Supplier<Matrix4fc> value, ValueUpdateNotifier notifier) {
		return this.uniformMatrix(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniform4i(String name, Supplier<Vector4i> value, ValueUpdateNotifier notifier) {
		return this.register(UniformUpdateFrequency.CUSTOM, name, "ivec4", offset -> {
			var vector = value.get();
			this.data.putInt(offset, vector.x);
			this.data.putInt(offset + 4, vector.y);
			this.data.putInt(offset + 8, vector.z);
			this.data.putInt(offset + 12, vector.w);
		});
	}

	@Override
	public BufferUniformHolder uniformMatrix3(String name, Supplier<Matrix3fc> value, ValueUpdateNotifier notifier) {
		return this.uniformMatrix3(UniformUpdateFrequency.CUSTOM, name, value);
	}

	@Override
	public BufferUniformHolder uniformMatrix3(UniformUpdateFrequency frequency, String name, Supplier<Matrix3fc> value) {
		return this.register(frequency, name, "mat3", offset -> {
			var matrix = value.get();

			for (int column = 0; column < 3; column++) {
				for (int row = 0; row < 3; row++) {
					this.data.putFloat(offset + column * 16 + row * 4, matrix.get(column, row));
				}
			}
		});
	}

	private static final class Update {
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
