package net.coderbot.iris.uniforms.custom;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.resolver.ExpressionResolver;
import kroppeb.stareval.token.ExpressionToken;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.uniform.LocationalUniformHolder;
import net.coderbot.iris.gl.uniform.Uniform;
import net.coderbot.iris.gl.uniform.UniformHolder;
import net.coderbot.iris.gl.uniform.UniformUpdateFrequency;
import net.coderbot.iris.parsing.IrisFunctions;
import net.coderbot.iris.parsing.IrisOptions;
import net.coderbot.iris.parsing.VectorType;
import net.coderbot.iris.uniforms.custom.cached.CachedUniform;

import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;


public class CustomUniforms extends Uniform implements FunctionContext {
	private final Map<String, CachedUniform> variables = new Object2ObjectLinkedOpenHashMap<>();
	private final CustomUniformFixedInputUniformsHolder inputHolder;
	private final ExpressionResolver resolver;
	
	
	public CustomUniforms(CustomUniformFixedInputUniformsHolder inputHolder) {
		super(-1);
		this.inputHolder = inputHolder;
		this.resolver = new ExpressionResolver(
				IrisFunctions.functions,
				(name) -> {
					Type type = this.inputHolder.getType(name);
					if (type != null)
						return type;
					CachedUniform uniform = this.variables.get(name);
					if (uniform != null)
						return uniform.getType();
					return null;
				},
				true);
	}
	
	public CachedUniform addVariable(Type type, String name, ExpressionToken expression) throws Exception {
		if (this.variables.containsKey(name))
			throw new Exception("Duplicated variable: " + name);
		if (this.inputHolder.containsKey(name))
			throw new Exception("Variable shadows: " + name);
		
		Expression expr = this.resolver.resolveExpression(type, expression);
		CachedUniform uniform = CachedUniform.forExpression(type, expr, this);
		this.variables.put(name, uniform);
		return uniform;
	}
	
	public CachedUniform addUniform(Type type, String name, ExpressionToken expression, int location) throws Exception {
		CachedUniform uniform = this.addVariable(type, name, expression);
		uniform.setLocation(location);
		return uniform;
	}
	
	@Override
	public void update() {
		this.inputHolder.updateAll();
		for (CachedUniform value : this.variables.values()) {
			value.update();
		}
	}
	
	@Override
	public Expression getVariable(String name) {
		// TODO: just add the function return as an argument
		final CachedUniform inputUniform = this.inputHolder.getUniform(name);
		if (inputUniform != null)
			return (context, functionReturn) -> inputUniform.writeTo(functionReturn);
		final CachedUniform customUniform = this.variables.get(name);
		if (customUniform != null)
			return (context, functionReturn) -> customUniform.writeTo(functionReturn);
		throw new RuntimeException("Unknown variable: " + name);
	}
	
	public interface Factory {
		void buildTo(
				LocationalUniformHolder targetHolder,
				Consumer<UniformHolder>... uniforms
		);
	}
	
	public static class Builder implements Factory {
		Map<String, Variable> variables = new Object2ObjectLinkedOpenHashMap<>();
		
		public void addVariable(String type, String name, String expression, boolean isUniform) {
			if (variables.containsKey(name)) {
				Iris.logger.warn("Ignoring duplicated custom uniform name: " + name);
				return;
			}
			
			Type parsedType = types.get(type);
			if (parsedType == null) {
				Iris.logger.warn("Ignoring invalid uniform type: " + type + " of " + name);
				return;
			}
			
			try{
				ExpressionToken ast = IrisOptions.parser.parse(expression).simplify();
				variables.put(name, new Variable(parsedType, name, ast, isUniform));
			}catch (Exception e){
				Iris.logger.warn("Failed to parse custom variable/uniform");
			}
		}
		
		public CustomUniforms build(
				CustomUniformFixedInputUniformsHolder inputHolder,
				LocationalUniformHolder targetHolder
		) {
			CustomUniforms customUniforms = new CustomUniforms(inputHolder);
			for (Variable variable : this.variables.values()) {
				try {
					if (variable.uniform) {
						OptionalInt location = targetHolder.location(variable.name);
						if (location.isPresent()) {
							customUniforms
									.addUniform(variable.type, variable.name, variable.expression, location.getAsInt());
						} else {
							customUniforms.addVariable(variable.type, variable.name, variable.expression);
						}
					} else {
						customUniforms.addVariable(variable.type, variable.name, variable.expression);
					}
					Iris.logger.info("Was able to resolve uniform " + variable.name + " = " + variable.expression);
				} catch (Exception e) {
					Iris.logger
							.warn("Failed to resolve uniform " + variable.name + ", reason: " + e
									.getMessage() + " ( = " + variable.expression + ")");
					Iris.logger.catching(e);
				}
			}
			return customUniforms;
		}
		
		
		@SafeVarargs
		public final CustomUniforms build(
				LocationalUniformHolder targetHolder,
				Consumer<UniformHolder>... uniforms
		) {
			CustomUniformFixedInputUniformsHolder.Builder inputs = new CustomUniformFixedInputUniformsHolder.Builder();
			for (Consumer<UniformHolder> uniform : uniforms) {
				uniform.accept(inputs);
			}
			return this.build(inputs.build(), targetHolder);
		}
		
		@SafeVarargs
		@Override
		public final void buildTo(LocationalUniformHolder targetHolder, Consumer<UniformHolder>... uniforms) {
			Iris.logger.info("Starting custom uniform parsing");
			CustomUniforms customUniforms = this.build(targetHolder, uniforms);
			targetHolder.addUniform(UniformUpdateFrequency.PER_FRAME, customUniforms);
		}
		
		private static class Variable {
			final public Type type;
			final public String name;
			final public ExpressionToken expression;
			final public boolean uniform;
			
			public Variable(Type type, String name, ExpressionToken expression, boolean uniform) {
				this.type = type;
				this.name = name;
				this.expression = expression;
				this.uniform = uniform;
			}
		}
		
		final private static Map<String, Type> types = new ImmutableMap.Builder<String, Type>()
				.put("bool", Type.Boolean)
				.put("float", Type.Float)
				.put("int", Type.Int)
				.put("vec2", VectorType.VEC2)
				.put("vec3", VectorType.VEC3)
				.put("vec4", VectorType.VEC4)
				.build();
		
		
	}
}
