package kroppeb.stareval.function;

import kroppeb.stareval.expression.ConstantExpression;

abstract public class Type {
	
	public abstract ConstantExpression createConstant(FunctionReturn functionReturn);
	
	abstract public static class Primitive extends Type {
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
	
	public static Primitive[] AllPrimitives = new Primitive[]{Type.Boolean, Type.Int, Type.Float};
	
	public abstract Object createArray(int length);
	
	public abstract void setValueFromReturn(Object array, int index, FunctionReturn value);
	
	public abstract void getValueFromArray(Object array, int index, FunctionReturn value);
	
	public abstract String toString();
}
