package net.irisshaders.iris.parsing;

import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

abstract public class VectorType extends Type.ObjectType {
	public static final JOMLVector<Vector2f> VEC2 = new JOMLVector<>("vec2", Vector2f::new);
	public static final JOMLVector<Vector3f> VEC3 = new JOMLVector<>("vec3", Vector3f::new);
	public static final JOMLVector<Vector4f> VEC4 = new JOMLVector<>("vec4", Vector4f::new);
	public static final JOMLVector<Vector2i> I_VEC2 = new JOMLVector<>("ivec2", Vector2i::new);
	public static final JOMLVector<Vector3i> I_VEC3 = new JOMLVector<>("ivec3", Vector3i::new);
	public static final JOMLVector<Vector4i> I_VEC4 = new JOMLVector<>("ivec4", Vector4i::new);
	public static final VectorType B_VEC2 = new ArrayVector(Type.Boolean, 2);
	public static final VectorType B_VEC3 = new ArrayVector(Type.Boolean, 3);
	public static final VectorType B_VEC4 = new ArrayVector(Type.Boolean, 4);
	public static final ArrayVector[] AllArrayVectorTypes = Stream.of(Type.Int, Type.Boolean)
		.flatMap(type ->
			IntStream
				.rangeClosed(2, 4)
				.mapToObj(i -> new ArrayVector(type, i))
		).toArray(ArrayVector[]::new);
	public static final VectorType[] AllVectorTypes = Arrays.stream(Type.AllPrimitives)
		.flatMap(type ->
			IntStream
				.rangeClosed(2, 4)
				.mapToObj(i -> VectorType.of(type, i))
		).toArray(VectorType[]::new);

	public static VectorType of(Type.Primitive primitive, int size) {
		if (primitive.equals(Type.Float)) {
			return switch (size) {
				case 2 -> VEC2;
				case 3 -> VEC3;
				case 4 -> VEC4;
				default -> throw new IllegalArgumentException("not a valid vector");
			};
		} else {
			return new ArrayVector(primitive, size);
		}
	}

	public static class ArrayVector extends VectorType {
		private final Type inner;
		private final int size;

		public ArrayVector(Type inner, int size) {
			this.inner = inner;
			this.size = size;
		}

		public Object createObject() {
			return this.inner.createArray(this.size);
		}

		public void setValue(Object vector, int index, FunctionReturn functionReturn) {
			this.inner.setValueFromReturn(vector, index, functionReturn);
		}

		public void getValue(Object vector, int index, FunctionReturn functionReturn) {
			this.inner.getValueFromArray(vector, index, functionReturn);
		}

		public <T1, T2> void map(T1 item1, T2 item2, FunctionReturn functionReturn, IntObjectObjectObjectConsumer<T1, T2, FunctionReturn> mapper) {
			Object array = this.createObject();
			for (int i = 0; i < this.size; i++) {
				mapper.accept(i, item1, item2, functionReturn);
				this.setValue(array, i, functionReturn);
			}
			functionReturn.objectReturn = array;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof ArrayVector that)) return false;
			return size == that.size && inner.equals(that.inner);
		}

		@Override
		public int hashCode() {
			return Objects.hash(inner, size);
		}

		@Override
		public String toString() {
			String base = this.inner.equals(Type.Float) ? "" : this.inner.toString().substring(0, 1);
			return "__" + base + "vec" + this.size;
		}

		public interface IntObjectObjectObjectConsumer<TB, TC, TD> {
			void accept(int a, TB b, TC c, TD d);
		}
	}

	public static class JOMLVector<T> extends VectorType {
		private final String name;
		private final Supplier<T> supplier;

		public JOMLVector(String name, Supplier<T> supplier) {
			this.name = name;
			this.supplier = supplier;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public T create() {
			return this.supplier.get();
		}
	}
}
