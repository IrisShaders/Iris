package net.coderbot.iris.gbuffer_overrides.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ProgramTable<T> {
	private final List<T> table = new ArrayList<>();

	public ProgramTable(BiFunction<RenderCondition, InputAvailability, T> constructor) {
		for (RenderCondition condition : RenderCondition.values()) {
			for (int packedAvailability = 0; packedAvailability < InputAvailability.NUM_VALUES; packedAvailability++) {
				InputAvailability availability = InputAvailability.unpack(packedAvailability);

				table.add(constructor.apply(condition, availability));
			}
		}
	}

	// TODO: Remove InputAvailability allocations?
	public T match(RenderCondition condition, InputAvailability availability) {
		int index = (condition.ordinal() * InputAvailability.NUM_VALUES) + availability.pack();

		return table.get(index);
	}

	public void forEach(Consumer<T> consumer) {
		table.forEach(consumer);
	}
}
