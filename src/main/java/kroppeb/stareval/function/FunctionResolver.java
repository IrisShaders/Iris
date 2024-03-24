package kroppeb.stareval.function;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FunctionResolver {
	private final Map<String, Map<Type, List<TypedFunction>>> functions;
	// TODO: instead of a suplier we could use an int->function to make varargs
	private final Map<String, Map<Type, List<Supplier<? extends TypedFunction>>>> dynamicFunctions;

	public FunctionResolver(
		Map<String, Map<Type, List<TypedFunction>>> functions,
		Map<String, Map<Type, List<Supplier<? extends TypedFunction>>>> dynamicFunctions) {
		this.functions = functions;
		this.dynamicFunctions = dynamicFunctions;
	}

	public List<? extends TypedFunction> resolve(String name, Type returnType) {
		Map<Type, List<TypedFunction>> normal = this.functions.get(name);
		Map<Type, List<Supplier<? extends TypedFunction>>> dynamic = this.dynamicFunctions.get(name);
		List<? extends TypedFunction> u = null;

		if (normal == null && dynamic == null)
			throw new RuntimeException("No such function: " + name);
		if (normal != null)
			u = normal.get(returnType);
		if (dynamic != null) {
			List<Supplier<? extends TypedFunction>> p = dynamic.get(returnType);
			if (p != null) {
				List<? extends TypedFunction> uDynamic = p.stream().map(Supplier::get).collect(Collectors.toList());
				if (u == null) {
					u = uDynamic;
				} else {
					List<TypedFunction> newU = new ArrayList<>(u.size() + uDynamic.size());
					newU.addAll(u);
					newU.addAll(uDynamic);
					u = newU;
				}
			}

		}

		if (u == null)
			return Collections.emptyList();
		return u;
	}

	public void logAllFunctions() {
		final Set<String> names = new LinkedHashSet<>();
		names.addAll(this.functions.keySet());
		names.addAll(this.dynamicFunctions.keySet());
		for (final String name : names) {
			final Map<Type, List<TypedFunction>> overloads = new Object2ObjectLinkedOpenHashMap<>();
			Map<Type, List<TypedFunction>> normal = this.functions.get(name);
			if (normal != null)
				overloads.putAll(normal);

			Map<Type, List<Supplier<? extends TypedFunction>>> dynamic = this.dynamicFunctions.get(name);
			if (dynamic != null)
				overloads.putAll(
					dynamic
						.entrySet()
						.stream()
						.collect(Collectors.toMap(
								Map.Entry::getKey,
								entry -> entry.getValue()
									.stream()
									.map(Supplier::get)
									.collect(Collectors.toList())
							)
						)
				);

			for (Map.Entry<Type, List<TypedFunction>> returnTypeOverloads : overloads.entrySet()) {
				for (TypedFunction typedFunction : returnTypeOverloads.getValue()) {
					System.out.println(TypedFunction.format(typedFunction, name));
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	public static class Builder {
		private final Map<String, List<TypedFunction>> functions = new Object2ObjectLinkedOpenHashMap<>();
		private final Map<String, Map<Type, List<Supplier<? extends TypedFunction>>>> dynamicFunctions = new Object2ObjectLinkedOpenHashMap<>();

		public <T extends TypedFunction> void add(String name, T function) {
			this.addFunction(name, function);
		}

		public <T extends TypedFunction> void addDynamic(String name, Type returnType, Supplier<T> function) {
			this.addDynamicFunction(name, returnType, function);
		}

		public void addDynamicFunction(String name, Type returnType, Supplier<? extends TypedFunction> function) {
			this.dynamicFunctions
				.computeIfAbsent(name, (n) -> new Object2ObjectLinkedOpenHashMap<>())
				.computeIfAbsent(returnType, (n) -> new ObjectArrayList<>())
				.add(function);
		}

		public void addFunction(String name, TypedFunction function) {
			this.functions.computeIfAbsent(name, (n) -> new ObjectArrayList<>()).add(function);
		}

		public FunctionResolver build() {
			Map<String, Map<Type, List<TypedFunction>>> functions = new Object2ObjectLinkedOpenHashMap<>();
			for (Map.Entry<String, List<TypedFunction>> entry : this.functions.entrySet()) {
				Map<Type, List<TypedFunction>> typeMap = new Object2ObjectLinkedOpenHashMap<>();
				for (TypedFunction function : entry.getValue()) {
					typeMap.computeIfAbsent(function.getReturnType(), i -> new ObjectArrayList<>())
						.add(function);
				}
				functions.put(entry.getKey(), typeMap);
			}

			return new FunctionResolver(functions, this.dynamicFunctions);
		}
	}

}
