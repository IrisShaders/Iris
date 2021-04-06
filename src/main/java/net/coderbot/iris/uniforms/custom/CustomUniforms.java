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

import java.util.*;
import java.util.function.Consumer;


public class CustomUniforms extends Uniform implements FunctionContext {
	private final Map<String, CachedUniform> variables = new Object2ObjectLinkedOpenHashMap<>();
	private final CustomUniformFixedInputUniformsHolder inputHolder;
	private final List<CachedUniform> uniforms = new ArrayList<>();
	
	
	private CustomUniforms(CustomUniformFixedInputUniformsHolder inputHolder, Collection<Builder.Variable> variables) {
		super(-1);
		
		this.inputHolder = inputHolder;
		ExpressionResolver resolver = new ExpressionResolver(
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
		
		for (Builder.Variable variable : variables) {
			try {
				CachedUniform cachedUniform = variable.convert(resolver, this);
				this.addVariable(cachedUniform);
				if (variable.uniform) {
					this.uniforms.add(cachedUniform);
				}
				Iris.logger.info("Was able to resolve uniform " + variable.name + " = " + variable.expression);
			} catch (Exception e) {
				Iris.logger
						.warn("Failed to resolve uniform " + variable.name + ", reason: " + e
								.getMessage() + " ( = " + variable.expression + ")");
				Iris.logger.catching(e);
			}
		}
	}
	
	private void addVariable(CachedUniform uniform) throws Exception {
		String name = uniform.getName();
		if (this.variables.containsKey(name))
			throw new Exception("Duplicated variable: " + name);
		if (this.inputHolder.containsKey(name))
			throw new Exception("Variable shadows: " + name);
		
		this.variables.put(name, uniform);
	}
	
	public void assignTo(LocationalUniformHolder targetHolder){
		for (CachedUniform uniform : this.uniforms) {
			OptionalInt location = targetHolder.location(uniform.getName());
			if(location.isPresent()){
				uniform.setLocation(location.getAsInt());
			}
		}
	}
	
	@Override
	public void update() {
		this.inputHolder.updateAll();
		for (CachedUniform value : this.variables.values()) {
			value.update();
		}
	}
	
	@Override
	public boolean hasVariable(String name) {
		return this.inputHolder.containsKey(name) || this.variables.containsKey(name);
	}
	
	@Override
	public Expression getVariable(String name) {
		// TODO: Make the simplify just return these ones
		final CachedUniform inputUniform = this.inputHolder.getUniform(name);
		if (inputUniform != null)
			return inputUniform;
		final CachedUniform customUniform = this.variables.get(name);
		if (customUniform != null)
			return customUniform;
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
			final ExpressionResolver resolver = new ExpressionResolver(
					IrisFunctions.functions,
					(name) -> {
						Type type = inputHolder.getType(name);
						if (type != null)
							return type;
						Variable uniform = this.variables.get(name);
						if (uniform != null)
							return uniform.type;
						return null;
					},
					true);
			
			CustomUniforms customUniforms = new CustomUniforms(inputHolder, this.variables.values());
			customUniforms.assignTo(targetHolder);
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
			
			CachedUniform convert(ExpressionResolver resolver, FunctionContext context){
				return CachedUniform.forExpression(
						this.name,
						this.type,
						resolver.resolveExpression(this.type, this.expression),
						context
				);
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
