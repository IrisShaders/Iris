package net.irisshaders.iris.uniforms.custom.cached;

import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.parsing.VectorType;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public abstract class CachedUniform implements VariableExpression {
	private final String name;
	private final UniformUpdateFrequency updateFrequency;
	private boolean changed = true;

	public CachedUniform(String name, UniformUpdateFrequency updateFrequency) {
		this.name = name;
		this.updateFrequency = updateFrequency;
	}

	static public CachedUniform forExpression(String name, Type type, Expression expression, FunctionContext context) {
		final FunctionReturn held = new FunctionReturn();
		final UniformUpdateFrequency frequency = UniformUpdateFrequency.CUSTOM;
		if (type.equals(Type.Boolean)) {
			return new BooleanCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return held.booleanReturn;
			});
		} else if (type.equals(Type.Int)) {
			return new IntCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return held.intReturn;
			});
		} else if (type.equals(Type.Float)) {
			return new FloatCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return held.floatReturn;
			});
		} else if (type.equals(VectorType.VEC2)) {
			return new Float2VectorCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return (Vector2f) held.objectReturn;
			});
		} else if (type.equals(VectorType.VEC3)) {
			return new Float3VectorCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return (Vector3f) held.objectReturn;
			});
		} else if (type.equals(VectorType.VEC4)) {
			return new Float4VectorCachedUniform(name, frequency, () -> {
				expression.evaluateTo(context, held);
				return (Vector4f) held.objectReturn;
			});
		} else {
			throw new IllegalArgumentException("Custom uniforms of type: " + type + " are currently not supported");
		}
	}

	public void markUnchanged() {
		this.changed = false;
	}

	public void update() {
		doUpdate();
		// TODO: Works around a logic error / architectural flaw - there's no way to
		//       know when a uniform has been uploaded to all programs, so we can never
		//       safely change this to false with the current design.
		this.changed = true;
	}

	protected abstract boolean doUpdate();

	public abstract void push(int location);

	public void pushIfChanged(int location) {
		if (this.changed)
			push(location);
	}

	@Override
	public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
		this.writeTo(functionReturn);
	}

	public abstract void writeTo(FunctionReturn functionReturn);

	public abstract Type getType();

	public String getName() {
		return name;
	}

	public UniformUpdateFrequency getUpdateFrequency() {
		return updateFrequency;
	}
}
