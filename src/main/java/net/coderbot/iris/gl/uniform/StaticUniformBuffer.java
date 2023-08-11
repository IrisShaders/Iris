package net.coderbot.iris.gl.uniform;

import it.unimi.dsi.fastutil.Pair;
import net.coderbot.iris.Iris;

import java.util.*;

public class StaticUniformBuffer implements LocationalUniformHolder {
	private Map<UniformUpdateFrequency, List<Uniform>> uniformList = new HashMap<>();
	private int size;

	public StaticUniformBuffer() {

	}

	private int align(int bufferSize, int alignment) {
		return (((bufferSize - 1) + alignment) & -alignment);
	}

	@Override
	public LocationalUniformHolder addUniform(UniformUpdateFrequency updateFrequency, Uniform uniform) {
		uniformList.computeIfAbsent(updateFrequency, uniformUpdateFrequency -> new ArrayList<>()).add(uniform);
		return this;
	}

	public void finish() {
		List<Pair<UniformUpdateFrequency, Uniform>> uniformOrder = new ArrayList<>();
		for (UniformUpdateFrequency frequency : uniformList.keySet()) {
			for (Uniform uniform : uniformList.get(frequency)) {
				uniformOrder.add(Pair.of(frequency, uniform));
			}
		}

		uniformOrder.sort(Comparator.comparing(Pair::first));

		for (Pair<UniformUpdateFrequency, Uniform> uniform : uniformOrder) {
			size = align(size, uniform.second().getAlignment());
			Iris.logger.warn("Uniform " + uniform.second().getType().name() + " added at " + size);
			size += uniform.second().getByteSize();
		}

		Iris.logger.warn("Final size: " + size);
	}

	@Override
	public OptionalInt location(String name, UniformType type) {
		return OptionalInt.of(0);
	}

	@Override
	public UniformHolder externallyManagedUniform(String name, UniformType type) {
		return this;
	}
}
