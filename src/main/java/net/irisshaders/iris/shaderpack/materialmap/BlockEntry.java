package net.irisshaders.iris.shaderpack.materialmap;

import net.irisshaders.iris.Iris;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public record BlockEntry(NamespacedId id, Map<String, String> propertyPredicates) implements Entry {

	/**
	 * Parses a block ID entry.
	 *
	 * @param entry The string representation of the entry. Must not be empty.
	 */
	@NotNull
	public static Entry parse(@NotNull String entry) {
		if (entry.isEmpty()) {
			throw new IllegalArgumentException("Called BlockEntry::parse with an empty string");
		}

		if (entry.startsWith("%")) {
			entry = entry.replace("%", "");
			// It's a tag!
			String[] splitStates = entry.split(":");

			if (splitStates.length == 1) {
				return new TagEntry(new NamespacedId("minecraft", entry), Map.of());
			} else if (splitStates.length == 2 && !splitStates[1].contains("=")) {
				return new TagEntry(new NamespacedId(splitStates[0], splitStates[1]), Map.of());
			} else {
				// Complex case: One or more states involved...
				int statesStart;
				NamespacedId id;

				if (splitStates[1].contains("=")) {
					// We have an entry of the form "tall_grass:half=upper"
					statesStart = 1;
					id = new NamespacedId("minecraft", splitStates[0]);
				} else {
					// We have an entry of the form "minecraft:tall_grass:half=upper"
					statesStart = 2;
					id = new NamespacedId(splitStates[0], splitStates[1]);
				}

				Map<String, String> map = new HashMap<>();

				for (int index = statesStart; index < splitStates.length; index++) {
					String[] propertyParts = splitStates[index].split("=");

					if (propertyParts.length != 2) {
						Iris.logger.warn("Warning: the block ID map entry \"" + entry + "\" could not be fully parsed:");
						Iris.logger.warn("- Block state property filters must be of the form \"key=value\", but "
							+ splitStates[index] + " is not of that form!");

						continue;
					}

					map.put(propertyParts[0], propertyParts[1]);
				}

				return new TagEntry(id, map);
			}
		}

		// We can assume that this array is of at least array length because the input string is non-empty.
		String[] splitStates = entry.split(":");

		// Trivial case: no states, no namespace
		if (splitStates.length == 1) {
			return new BlockEntry(new NamespacedId("minecraft", entry), Collections.emptyMap());
		}

		// Less trivial case: no states involved, just a namespace
		//
		// The first term MUST be a valid ResourceLocation component without an equals sign
		// The second term, if it does not contain an equals sign, must be a valid ResourceLocation component.
		if (splitStates.length == 2 && !splitStates[1].contains("=")) {
			return new BlockEntry(new NamespacedId(splitStates[0], splitStates[1]), Collections.emptyMap());
		}

		// Complex case: One or more states involved...
		int statesStart;
		NamespacedId id;

		if (splitStates[1].contains("=")) {
			// We have an entry of the form "tall_grass:half=upper"
			statesStart = 1;
			id = new NamespacedId("minecraft", splitStates[0]);
		} else {
			// We have an entry of the form "minecraft:tall_grass:half=upper"
			statesStart = 2;
			id = new NamespacedId(splitStates[0], splitStates[1]);
		}

		// We must parse each property key=value pair from the state entry.
		//
		// These pairs act as a filter on the block states. Thus, the shader pack does not have to specify all the
		// individual block properties itself; rather, it only specifies the parts of the block state that it wishes
		// to filter in/out.
		//
		// For example, some shader packs may make it so that hanging lantern blocks wave. They will put something of
		// the form "lantern:hanging=false" in the ID map as a result. Note, however, that there are also waterlogged
		// hanging lanterns, which would have "lantern:hanging=false:waterlogged=true". We must make sure that when the
		// shader pack author writes "lantern:hanging=false", that we do not just match that individual state, but that
		// we also match the waterlogged variant too.
		Map<String, String> map = new HashMap<>();

		for (int index = statesStart; index < splitStates.length; index++) {
			// Split "key=value" into the key and value
			String[] propertyParts = splitStates[index].split("=");

			if (propertyParts.length != 2) {
				Iris.logger.warn("Warning: the block ID map entry \"" + entry + "\" could not be fully parsed:");
				Iris.logger.warn("- Block state property filters must be of the form \"key=value\", but "
					+ splitStates[index] + " is not of that form!");

				// Continue and ignore the invalid entry.
				continue;
			}

			map.put(propertyParts[0], propertyParts[1]);
		}

		return new BlockEntry(id, map);
	}


}
