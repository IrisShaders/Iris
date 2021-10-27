package net.coderbot.iris.uniforms.custom.cached;

import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.VectorType;
import net.coderbot.iris.vendored.joml.Vector2f;
import net.coderbot.iris.vendored.joml.Vector3f;
import net.coderbot.iris.vendored.joml.Vector4f;

public abstract class CachedUniform implements VariableExpression {
	private final String name;
	private final UniformUpdateFrequency updateFrequency;
	private int location = -1;
	
	public CachedUniform(String name, UniformUpdateFrequency updateFrequency) {
		this.name = name;
		this.updateFrequency = updateFrequency;
	}
	
	public void setLocation(int location) {
		this.location = location;
	}
	
	public int getLocation() {
		return location;
	}
	
	public void update() {
		if (doUpdate() && this.location != -1) {
			this.push();
		}
	}
	
	protected abstract boolean doUpdate();
	
	protected abstract void push();
	
	@Override
	public void evaluateTo(FunctionContext context, FunctionReturn functionReturn) {
		this.writeTo(functionReturn);
	}
	
	public abstract void writeTo(FunctionReturn functionReturn);
	
	public abstract Type getType();
	
	static public CachedUniform forExpression(String name, Type type, Expression expression, FunctionContext context){
		final FunctionReturn held = new FunctionReturn();
		if(type.equals(Type.Boolean)){
			return new BooleanCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return held.booleanReturn;
			});
		}else if(type.equals(Type.Int)){
			return new IntCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return held.intReturn;
			});
		}else if(type.equals(Type.Float)){
			return new FloatCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return held.floatReturn;
			});
		}else if(type.equals(VectorType.VEC2)){
			return new Float2VectorCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return (Vector2f) held.objectReturn;
			});
		}else if(type.equals(VectorType.VEC3)){
			return new Float3VectorCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return (Vector3f) held.objectReturn;
			});
		}else if(type.equals(VectorType.VEC4)){
			return new Float4VectorCachedUniform(name, UniformUpdateFrequency.PER_FRAME,() -> {
				expression.evaluateTo(context,held);
				return (Vector4f) held.objectReturn;
			});
		}else{
			throw new IllegalArgumentException("Custom uniforms of type: " + type + " are currently not supported");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public UniformUpdateFrequency getUpdateFrequency() {
		return updateFrequency;
	}
}
