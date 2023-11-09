package kroppeb.stareval.function;

import kroppeb.stareval.expression.ConstantExpression;
import kroppeb.stareval.function.TypedFunction.Parameter;
import net.coderbot.iris.gl.uniform.UniformType;
import net.coderbot.iris.parsing.MatrixType;
import net.coderbot.iris.parsing.VectorType;

public abstract class Type {
	public abstract ConstantExpression createConstant(FunctionReturn functionReturn);

	public abstract static class Primitive extends Type {
	}

	public static class ObjectType extends Type {
		@Override
		public ConstantExpression createConstant(FunctionReturn functionReturn) {
			Object object = functionReturn.objectReturn;
			return new ConstantExpression(this) {
				@Override
				public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
					functionReturn.objectReturn = object;
				}
			};
		}

		@Override
		public Object createArray(int length) {
			return new Object[length];
		}

		@Override
		public void setValueFromReturn(Object array, int index, FunctionReturn value) {
			Object[] arr = (Object[]) array;
			arr[index] = value.objectReturn;
		}

		@Override
		public void getValueFromArray(Object array, int index, FunctionReturn value) {
			Object[] arr = (Object[]) array;
			value.objectReturn = arr[index];
		}

		@Override
		public String toString() {
			return "Object";
		}
	}

	public static class Boolean extends Primitive {
		@Override
		public ConstantExpression createConstant(FunctionReturn functionReturn) {
			boolean value = functionReturn.booleanReturn;
			return new ConstantExpression(this) {
				@Override
				public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
					functionReturn.booleanReturn = value;
				}
			};
		}

		@Override
		public Object createArray(int length) {
			return new boolean[length];
		}

		@Override
		public void setValueFromReturn(Object array, int index, FunctionReturn value) {
			boolean[] arr = (boolean[]) array;
			arr[index] = value.booleanReturn;
		}

		@Override
		public void getValueFromArray(Object array, int index, FunctionReturn value) {
			boolean[] arr = (boolean[]) array;
			value.booleanReturn = arr[index];
		}

		@Override
		public String toString() {
			return "bool";
		}
	}

	public static class Int extends Primitive {
		@Override
		public ConstantExpression createConstant(FunctionReturn functionReturn) {
			int value = functionReturn.intReturn;
			return new ConstantExpression(this) {
				@Override
				public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
					functionReturn.intReturn = value;
				}
			};
		}

		@Override
		public Object createArray(int length) {
			return new int[length];
		}

		@Override
		public void setValueFromReturn(Object array, int index, FunctionReturn value) {
			int[] arr = (int[]) array;
			arr[index] = value.intReturn;
		}

		@Override
		public void getValueFromArray(Object array, int index, FunctionReturn value) {
			int[] arr = (int[]) array;
			value.intReturn = arr[index];
		}

		@Override
		public String toString() {
			return "int";
		}
	}

	public static class Float extends Primitive {
		@Override
		public ConstantExpression createConstant(FunctionReturn functionReturn) {
			float value = functionReturn.floatReturn;
			return new ConstantExpression(this) {
				@Override
				public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
					functionReturn.floatReturn = value;
				}
			};
		}

		@Override
		public Object createArray(int length) {
			return new float[length];
		}

		@Override
		public void setValueFromReturn(Object array, int index, FunctionReturn value) {
			float[] arr = (float[]) array;
			arr[index] = value.floatReturn;
		}

		@Override
		public void getValueFromArray(Object array, int index, FunctionReturn value) {
			float[] arr = (float[]) array;
			value.floatReturn = arr[index];
		}

		@Override
		public String toString() {
			return "float";
		}
	}

	public static Boolean Boolean = new Boolean();
	public static Int Int = new Int();
	public static Float Float = new Float();

	public static Parameter BooleanParameter = new Parameter(Boolean);
	public static Parameter IntParameter = new Parameter(Int);
	public static Parameter FloatParameter = new Parameter(Float);

	public static Primitive[] AllPrimitives = {Type.Boolean, Type.Int, Type.Float};

	public abstract Object createArray(int length);

	public abstract void setValueFromReturn(Object array, int index, FunctionReturn value);

	public abstract void getValueFromArray(Object array, int index, FunctionReturn value);

	public abstract String toString();

	@Deprecated
	public static UniformType convert(Type type) {
		if (type == Type.Int || type == Type.Boolean) return UniformType.INT;
		else if (type == Type.Float) return UniformType.FLOAT;
		else if (type == VectorType.VEC2) return UniformType.VEC2;
		else if (type == VectorType.VEC3) return UniformType.VEC3;
		else if (type == VectorType.VEC4) return UniformType.VEC4;
		else if (type == VectorType.I_VEC2) return UniformType.VEC2I;
		else if (type == VectorType.I_VEC3) return UniformType.VEC3I;
		else if (type == MatrixType.MAT4) return UniformType.MAT4;
		else throw new IllegalArgumentException("Unsupported custom uniform type: " + type);
	}
}
