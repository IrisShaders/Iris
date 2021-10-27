package net.coderbot.iris.uniforms.custom;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.*;
import kroppeb.stareval.element.ExpressionElement;
import kroppeb.stareval.expression.Expression;
import kroppeb.stareval.expression.VariableExpression;
import kroppeb.stareval.function.FunctionContext;
import kroppeb.stareval.function.FunctionReturn;
import kroppeb.stareval.function.Type;
import kroppeb.stareval.parser.Parser;
import kroppeb.stareval.resolver.ExpressionResolver;
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
import java.util.stream.Collectors;


public class CustomUniforms extends Uniform implements FunctionContext {
	private final Map<String, CachedUniform> variables = new Object2ObjectLinkedOpenHashMap<>();
	private final Map<String, Expression> variablesExpressions = new Object2ObjectLinkedOpenHashMap<>();
	private final CustomUniformFixedInputUniformsHolder inputHolder;
	private final List<CachedUniform> uniforms = new ArrayList<>();
private final List<CachedUniform> uniformOrder;

	private CustomUniforms(CustomUniformFixedInputUniformsHolder inputHolder, Map<String, Builder.Variable> variables) {
		super(-1);

		this.inputHolder = inputHolder;
		ExpressionResolver resolver = new ExpressionResolver(
				IrisFunctions.functions,
				(name) -> {
					Type type = this.inputHolder.getType(name);
					if (type != null)
						return type;
					Builder.Variable variable = variables.get(name);
					if (variable != null)
						return variable.type;
					return null;
				},
				true);

		for (Builder.Variable variable : variables.values()) {
			try {
				Expression expression = resolver.resolveExpression(variable.type, variable.expression);
				CachedUniform cachedUniform = CachedUniform
						.forExpression(variable.name, variable.type, expression, this);
				this.addVariable(expression, cachedUniform);
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

		{
			// toposort

			Map<CachedUniform, List<CachedUniform>> dependsOn = new Object2ObjectOpenHashMap<>();
			Map<CachedUniform, List<CachedUniform>> requiredBy = new Object2ObjectOpenHashMap<>();
			Object2IntMap<CachedUniform> dependsOnCount = new Object2IntOpenHashMap<>();

			for (CachedUniform input : this.inputHolder.getAll()) {
				requiredBy.put(input, new ObjectArrayList<>());
			}

			for (CachedUniform input : this.variables.values()) {
				requiredBy.put(input, new ObjectArrayList<>());
			}

			FunctionReturn functionReturn = new FunctionReturn();
			Set<VariableExpression> requires = new ObjectOpenHashBigSet<>();
			for (Map.Entry<String, Expression> entry : this.variablesExpressions.entrySet()) {
				requires.clear();
				entry.getValue().listVariables(requires);
				if (requires.isEmpty()) {
					continue;
				}
				List<CachedUniform> dependencies = requires.stream()
						.map(v -> (CachedUniform) v.partialEval(this, functionReturn))
						.collect(Collectors.toList());

				CachedUniform uniform = this.variables.get(entry.getKey());

				dependsOn.put(uniform, dependencies);
				dependsOnCount.put(uniform, dependencies.size());

				for (CachedUniform dependency : dependencies) {
					requiredBy.get(dependency).add(uniform);
				}
			}

			// actual toposort:
			List<CachedUniform> ordered = new ObjectArrayList<>();
			List<CachedUniform> free = new ObjectArrayList<>();

			// init
			for (CachedUniform entry : requiredBy.keySet()) {
				if (!dependsOnCount.containsKey(entry)) {
					free.add(entry);
				}
			}

			while (!free.isEmpty()) {
				CachedUniform pop = free.remove(free.size() - 1);
				ordered.add(pop);
				for (CachedUniform dependent : requiredBy.get(pop)) {
					int count = dependsOnCount.mergeInt(dependent, -1, Integer::sum);
					assert count >= 0;
					if (count == 0) {
						free.add(dependent);
						dependsOnCount.removeInt(dependent);
					}
				}
			}

			if (!dependsOnCount.isEmpty()) {
				throw new IllegalStateException("Circular reference detected");
			}
			this.uniformOrder = ordered;
		}
	}
	
	private void addVariable(Expression expression, CachedUniform uniform) throws Exception {
		String name = uniform.getName();
		if (this.variables.containsKey(name))
			throw new Exception("Duplicated variable: " + name);
		if (this.inputHolder.containsKey(name))
			throw new Exception("Variable shadows build in uniform: " + name);

		this.variables.put(name, uniform);
		this.variablesExpressions.put(name, expression);
	}
	
	public void assignTo(LocationalUniformHolder targetHolder) {
		for (CachedUniform uniform : this.uniforms) {
			OptionalInt location = targetHolder.location(uniform.getName(), Type.convert(uniform.getType()));
			if (location.isPresent()) {
				uniform.setLocation(location.getAsInt());
			}
		}
	}

	@Override
	public void update() {
		for (CachedUniform value : this.uniformOrder) {
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

			try {
				ExpressionElement ast = Parser.parse(expression, IrisOptions.options);
				variables.put(name, new Variable(parsedType, name, ast, isUniform));
			} catch (Exception e) {
				Iris.logger.warn("Failed to parse custom variable/uniform");
			}
		}

		public CustomUniforms build(
				CustomUniformFixedInputUniformsHolder inputHolder,
				LocationalUniformHolder targetHolder
		) {
			CustomUniforms customUniforms = new CustomUniforms(inputHolder, this.variables);
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
			final public ExpressionElement expression;
			final public boolean uniform;

			public Variable(Type type, String name, ExpressionElement expression, boolean uniform) {
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

	static class DependencyTree {
		final private Map<CachedUniform, Set<CachedUniform>> dependsOn = new Object2ObjectOpenHashMap<>();
		final private Map<CachedUniform, Set<CachedUniform>> requiredBy = new Object2ObjectOpenHashMap<>();
		final private Map<String, CachedUniform> inputs = new Object2ObjectOpenHashMap<>();

		final private FunctionContext functionContext = new FunctionContext() {
			@Override
			public Expression getVariable(String name) {
				return inputs.get(name);
			}

			@Override
			public boolean hasVariable(String name) {
				return inputs.containsKey(name);
			}
		};

		void addInputs(Collection<CachedUniform> variables) {
			for (CachedUniform uniform : variables) {
				requiredBy.put(uniform, new ObjectOpenHashBigSet<>());
				inputs.put(uniform.getName(), uniform);
			}
		}
	}
}
